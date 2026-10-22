package com.ramzi.cashflow.presentation.broadcast

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.ramzi.cashflow.domain.model.Broadcast
import com.ramzi.cashflow.domain.model.BroadcastTarget
import com.ramzi.cashflow.domain.model.BroadcastType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object BroadcastListener {

    private const val TAG = "BroadcastListener"
    private const val COLLECTION = "broadcasts"

    fun observe(): Flow<List<Broadcast>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)

        val registration = query.addSnapshotListener(
            MetadataChanges.INCLUDE   // ← auto refresh meskipun dari cache
        ) { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Snapshot error", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val fromCache = snapshot.metadata.isFromCache
            Log.d(TAG, "Snapshot: ${snapshot.size()} docs, fromCache=$fromCache")

            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    val title = doc.getString("title").orEmpty()
                    val message = doc.getString("message").orEmpty()
                    val type = BroadcastType.fromId(doc.getString("type"))
                    val target = BroadcastTarget.fromId(doc.getString("target"))
                    val createdAt = doc.getTimestamp("createdAt")

                    Log.d(TAG, "  doc=${doc.id} target=${target.id} title='$title'")

                    if (title.isBlank() && message.isBlank()) null
                    else Broadcast(
                        id = doc.id,
                        title = title,
                        message = message,
                        type = type,
                        target = target,
                        createdAt = createdAt
                    )
                } catch (t: Throwable) {
                    Log.w(TAG, "Malformed doc ${doc.id}", t)
                    null
                }
            }

            trySend(items)
        }

        awaitClose {
            Log.d(TAG, "Listener removed")
            registration.remove()
        }
    }
}