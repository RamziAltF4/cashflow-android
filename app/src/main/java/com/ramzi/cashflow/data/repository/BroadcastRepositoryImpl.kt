package com.ramzi.cashflow.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ramzi.cashflow.domain.model.Broadcast
import com.ramzi.cashflow.domain.model.BroadcastTarget
import com.ramzi.cashflow.domain.model.BroadcastType
import com.ramzi.cashflow.domain.repository.BroadcastRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BroadcastRepositoryImpl(
    private val firestore: FirebaseFirestore
) : BroadcastRepository {

    override fun observeLatest(): Flow<List<Broadcast>> = callbackFlow {
        val query = firestore.collection("broadcasts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)

        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w("BroadcastRepository", "Snapshot error: ${err.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { doc ->
                Broadcast(
                    id = doc.id,
                    title = doc.getString("title").orEmpty(),
                    message = doc.getString("message").orEmpty(),
                    type = BroadcastType.fromId(doc.getString("type")),
                    target = BroadcastTarget.fromId(doc.getString("target")),
                    createdAt = doc.getTimestamp("createdAt")
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }
}