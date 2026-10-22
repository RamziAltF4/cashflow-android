package com.ramzi.cashflow.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.ActivityLog
import com.ramzi.cashflow.domain.repository.ActivityLogRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ActivityLogRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ActivityLogRepository {

    private val col = firestore.collection("activity_logs")

    /* ------------------------------------------------------------
       MAPPER
       ------------------------------------------------------------ */
    private fun mapDoc(id: String, data: Map<String, Any?>): ActivityLog {
        return ActivityLog(
            id = id,
            userId = data["userId"] as? String ?: "",
            action = data["action"] as? String ?: "",
            detail = data["detail"] as? String ?: "",
            date = (data["date"] as? Number)?.toLong() ?: 0L,
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp
        )
    }

    /* ------------------------------------------------------------
       OBSERVE — realtime, anti-crash
       ------------------------------------------------------------ */
    override fun observeRecent(
        userId: String,
        limit: Long
    ): Flow<List<ActivityLog>> = callbackFlow {

        // ✅ Guard: userId kosong → langsung kirim empty
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = try {
            col.whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        // ✅ JANGAN crash — log + kirim empty
                        Log.w(
                            "ActivityLogRepo",
                            "Snapshot error (butuh composite index?): ${err.message}"
                        )
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snap?.documents
                        ?.map { mapDoc(it.id, it.data ?: emptyMap()) }
                        ?: emptyList()
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.w("ActivityLogRepo", "Failed to attach listener", e)
            trySend(emptyList())
            null
        }

        awaitClose { listener?.remove() }
    }

    /* ------------------------------------------------------------
       GET RECENT — one-shot, anti-crash
       ------------------------------------------------------------ */
    override suspend fun getRecent(
        userId: String,
        limit: Long
    ): List<ActivityLog> {
        if (userId.isBlank()) return emptyList()

        return try {
            val snap = col.whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snap.documents.map { mapDoc(it.id, it.data ?: emptyMap()) }
        } catch (e: Exception) {
            // ✅ Tangkap semua error termasuk FAILED_PRECONDITION
            Log.w(
                "ActivityLogRepo",
                "getRecent failed (butuh composite index?): ${e.message}"
            )
            emptyList()
        }
    }

    /* ------------------------------------------------------------
       LOG — tulis activity baru
       ------------------------------------------------------------ */
    override suspend fun log(userId: String, action: String, detail: String) {
        if (userId.isBlank()) return
        try {
            val payload = mapOf(
                "userId" to userId,
                "action" to action,
                "detail" to detail,
                "date" to System.currentTimeMillis(),
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            col.add(payload).await()
        } catch (e: Exception) {
            Log.w("ActivityLogRepo", "log failed", e)
        }
    }
}