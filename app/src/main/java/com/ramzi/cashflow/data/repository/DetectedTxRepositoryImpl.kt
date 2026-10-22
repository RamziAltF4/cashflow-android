package com.ramzi.cashflow.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.DetectedStatus
import com.ramzi.cashflow.domain.model.DetectedTx
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.repository.DetectedTxRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DetectedTxRepositoryImpl(
    private val firestore: FirebaseFirestore
) : DetectedTxRepository {

    private val col = firestore.collection("detected_txs")

    /* ------------------------------------------------------------
       MAPPER
       ------------------------------------------------------------ */
    private fun mapDoc(id: String, data: Map<String, Any?>): DetectedTx {
        return DetectedTx(
            id = id,
            userId = data["userId"] as? String ?: "",
            bank = data["bank"] as? String ?: "BCA",
            type = TxType.fromId(data["type"] as? String),
            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
            merchant = data["merchant"] as? String ?: "",
            categoryFromBank = data["categoryFromBank"] as? String ?: "",
            categoryMapped = data["categoryMapped"] as? String ?: "",
            rawText = data["rawText"] as? String ?: "",
            notifTimestamp = (data["notifTimestamp"] as? Number)?.toLong() ?: 0L,
            date = data["date"] as? String ?: "",
            status = DetectedStatus.fromId(data["status"] as? String),
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
            fingerprint = data["fingerprint"] as? String ?: ""
        )
    }

    private fun mapToMap(tx: DetectedTx): Map<String, Any?> = mapOf(
        "userId" to tx.userId,
        "bank" to tx.bank,
        "type" to tx.type.id,
        "amount" to tx.amount,
        "merchant" to tx.merchant,
        "categoryFromBank" to tx.categoryFromBank,
        "categoryMapped" to tx.categoryMapped,
        "rawText" to tx.rawText,
        "notifTimestamp" to tx.notifTimestamp,
        "date" to tx.date,
        "status" to tx.status.id,
        "fingerprint" to tx.fingerprint
    )

    /* ------------------------------------------------------------
       OBSERVE PENDING
       ------------------------------------------------------------ */
    override fun observePending(userId: String): Flow<List<DetectedTx>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = try {
            col.whereEqualTo("userId", userId)
                .whereEqualTo("status", DetectedStatus.PENDING.id)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.w("DetectedTxRepo", "Snapshot error: ${err.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snap?.documents
                        ?.map { mapDoc(it.id, it.data ?: emptyMap()) }
                        ?.sortedByDescending { it.notifTimestamp }
                        ?: emptyList()
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "Failed to attach listener", e)
            trySend(emptyList())
            null
        }

        awaitClose { listener?.remove() }
    }

    /* ------------------------------------------------------------
       GET
       ------------------------------------------------------------ */
    override suspend fun getPending(userId: String): List<DetectedTx> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snap = col.whereEqualTo("userId", userId)
                .whereEqualTo("status", DetectedStatus.PENDING.id)
                .get()
                .await()
            snap.documents
                .map { mapDoc(it.id, it.data ?: emptyMap()) }
                .sortedByDescending { it.notifTimestamp }
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "getPending failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getAll(userId: String): List<DetectedTx> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snap = col.whereEqualTo("userId", userId).get().await()
            snap.documents
                .map { mapDoc(it.id, it.data ?: emptyMap()) }
                .sortedByDescending { it.notifTimestamp }
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "getAll failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getPendingBefore(userId: String, today: String): List<DetectedTx> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snap = col.whereEqualTo("userId", userId)
                .whereEqualTo("status", DetectedStatus.PENDING.id)
                .get()
                .await()
            snap.documents
                .map { mapDoc(it.id, it.data ?: emptyMap()) }
                .filter { it.date.isNotBlank() && it.date < today }
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "getPendingBefore failed: ${e.message}")
            emptyList()
        }
    }

    /* ------------------------------------------------------------
       ⭐ ADD — anti-duplikat (REVISI)

       Cek 2 lapis:
         1. fingerprint  — hash(amount + type + merchant + date)
         2. notifTimestamp + amount — pengaman kalau fingerprint beda

       Tidak ada window waktu → replay notif yang sama akan selalu di-skip.
       ------------------------------------------------------------ */
    override suspend fun addIfNotDuplicate(tx: DetectedTx): Boolean {
        return try {
            // Lapis 1: cek by fingerprint
            val snap = col.whereEqualTo("userId", tx.userId)
                .whereEqualTo("fingerprint", tx.fingerprint)
                .get()
                .await()

            if (snap.documents.isNotEmpty()) {
                Log.d("DetectedTxRepo", "Duplicate skipped (fingerprint): ${tx.fingerprint}")
                return false
            }

            // Lapis 2: cek by (notifTimestamp + amount) — pengaman
            if (tx.notifTimestamp > 0L) {
                val snap2 = col.whereEqualTo("userId", tx.userId)
                    .whereEqualTo("notifTimestamp", tx.notifTimestamp)
                    .whereEqualTo("amount", tx.amount)
                    .get()
                    .await()

                if (snap2.documents.isNotEmpty()) {
                    Log.d(
                        "DetectedTxRepo",
                        "Duplicate skipped (timestamp+amount): ${tx.notifTimestamp}/${tx.amount}"
                    )
                    return false
                }
            }

            // Insert
            val payload = mapToMap(tx).toMutableMap()
            payload["createdAt"] = FieldValue.serverTimestamp()
            col.add(payload).await()
            true
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "addIfNotDuplicate FAILED: ${e.message}", e)
            false
        }
    }

    /* ------------------------------------------------------------
       UPDATE / DELETE
       ------------------------------------------------------------ */
    override suspend fun updateStatus(id: String, status: String) {
        try {
            col.document(id).update("status", status).await()
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "updateStatus failed", e)
        }
    }

    override suspend fun delete(id: String) {
        try {
            col.document(id).delete().await()
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "delete failed", e)
        }
    }

    override suspend fun cleanupDone(userId: String) {
        try {
            val snap = col.whereEqualTo("userId", userId)
                .whereNotEqualTo("status", DetectedStatus.PENDING.id)
                .get()
                .await()
            snap.documents.forEach { it.reference.delete().await() }
        } catch (e: Exception) {
            Log.w("DetectedTxRepo", "cleanupDone failed", e)
        }
    }
}