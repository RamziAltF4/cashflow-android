package com.ramzi.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.Recurring
import com.ramzi.cashflow.domain.model.RecurringFrequency
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.repository.RecurringRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecurringRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RecurringRepository {

    private val col = firestore.collection("recurring")
    private val txCol = firestore.collection("transactions")

    /* ------------------------------------------------------------
       MAPPER
       ------------------------------------------------------------ */
    private fun mapDoc(id: String, data: Map<String, Any?>): Recurring {
        return Recurring(
            id = id,
            userId = data["userId"] as? String ?: "",
            desc = data["desc"] as? String ?: "",
            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
            type = TxType.fromId(data["type"] as? String),
            category = data["category"] as? String ?: "",
            frequency = RecurringFrequency.fromId(data["frequency"] as? String),
            startDate = data["startDate"] as? String ?: "",
            endDate = data["endDate"] as? String,
            nextRun = data["nextRun"] as? String ?: "",
            lastRun = data["lastRun"] as? String,
            active = data["active"] as? Boolean ?: true,
            note = data["note"] as? String ?: "",
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
            updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp
        )
    }

    private fun mapToMap(r: Recurring): Map<String, Any?> = mapOf(
        "userId" to r.userId,
        "desc" to r.desc,
        "amount" to r.amount,
        "type" to r.type.id,
        "category" to r.category,
        "frequency" to r.frequency.id,
        "startDate" to r.startDate,
        "endDate" to r.endDate,
        "nextRun" to r.nextRun,
        "lastRun" to r.lastRun,
        "active" to r.active,
        "note" to r.note
    )

    /* ------------------------------------------------------------
       OBSERVE / GET
       ------------------------------------------------------------ */
    override fun observeAll(userId: String): Flow<List<Recurring>> = callbackFlow {
        val listener = col.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.map { mapDoc(it.id, it.data ?: emptyMap()) }
                    ?.sortedBy { it.nextRun }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getAll(userId: String): List<Recurring> {
        val snap = col.whereEqualTo("userId", userId).get().await()
        return snap.documents
            .map { mapDoc(it.id, it.data ?: emptyMap()) }
            .sortedBy { it.nextRun }
    }

    /* ------------------------------------------------------------
       CRUD
       ------------------------------------------------------------ */
    override suspend fun create(recurring: Recurring) {
        val ref = col.document()
        val payload = mapToMap(recurring).toMutableMap()
        payload["createdAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
        ref.set(payload).await()
    }

    override suspend fun update(recurring: Recurring) {
        val payload = mapToMap(recurring).toMutableMap()
        payload["updatedAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
        col.document(recurring.id).update(payload).await()
    }

    override suspend fun toggleActive(id: String, active: Boolean) {
        col.document(id).update(
            mapOf(
                "active" to active,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
    }

    override suspend fun delete(id: String) {
        col.document(id).delete().await()
    }

    /* ------------------------------------------------------------
       PROCESS DUE
       ------------------------------------------------------------ */
    override suspend fun processDue(
        userId: String,
        today: String
    ): RecurringRepository.ProcessResult {

        val all = getAll(userId)
        val due = all.filter { it.active && it.nextRun.isNotBlank() && it.nextRun <= today }

        if (due.isEmpty()) return RecurringRepository.ProcessResult()

        var created = 0
        val names = mutableListOf<String>()
        val txs = mutableListOf<Transaction>()

        for (r in due) {
            try {
                // 1) Buat transaksi baru
                val txRef = txCol.document()
                val txPayload = mapOf(
                    "userId" to userId,
                    "type" to r.type.id,
                    "desc" to "${r.desc} (auto)",
                    "amount" to r.amount,
                    "category" to r.category,
                    "date" to today,
                    "note" to "Auto dari transaksi berulang · ${r.frequency.label}",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                txRef.set(txPayload).await()

                txs.add(
                    Transaction(
                        id = txRef.id,
                        userId = userId,
                        type = r.type,
                        desc = "${r.desc} (auto)",
                        amount = r.amount,
                        category = r.category,
                        date = today,
                        note = "Auto dari transaksi berulang · ${r.frequency.label}"
                    )
                )

                // 2) Hitung nextRun
                var next = nextRunDate(r.nextRun, r.frequency)
                var guard = 0
                while (next <= today && guard < 500) {
                    next = nextRunDate(next, r.frequency)
                    guard++
                }

                // 3) Cek endDate
                val ended = !r.endDate.isNullOrBlank() && next > r.endDate!!

                col.document(r.id).update(
                    mapOf(
                        "nextRun" to next,
                        "lastRun" to today,
                        "active" to if (ended) false else r.active,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()

                created++
                names.add(r.desc)
            } catch (e: Exception) {
                android.util.Log.w("RecurringRepo", "processDue error on ${r.id}", e)
            }
        }

        return RecurringRepository.ProcessResult(
            createdCount = created,
            createdDesc = names,
            createdTxs = txs
        )
    }

    /* ------------------------------------------------------------
       DATE HELPER
       ------------------------------------------------------------ */
    private fun nextRunDate(fromDate: String, freq: RecurringFrequency): String {
        val parts = fromDate.split("-").map { it.toInt() }
        val cal = java.util.Calendar.getInstance()
        cal.set(parts[0], parts[1] - 1, parts[2])

        when (freq) {
            RecurringFrequency.DAILY -> cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            RecurringFrequency.WEEKLY -> cal.add(java.util.Calendar.DAY_OF_MONTH, 7)
            RecurringFrequency.MONTHLY -> cal.add(java.util.Calendar.MONTH, 1)
            RecurringFrequency.YEARLY -> cal.add(java.util.Calendar.YEAR, 1)
        }

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(cal.time)
    }
}