package com.ramzi.cashflow.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.Workspace
import com.ramzi.cashflow.domain.repository.WorkspaceRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkspaceRepositoryImpl(
    private val firestore: FirebaseFirestore
) : WorkspaceRepository {

    private val col = firestore.collection("workspaces")

    private fun mapDoc(id: String, data: Map<String, Any?>): Workspace {
        return Workspace(
            id = id,
            ownerId = data["ownerId"] as? String ?: "",
            name = data["name"] as? String ?: "",
            date = (data["date"] as? Number)?.toLong() ?: 0L,
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp
        )
    }

    override fun observeAll(ownerId: String): Flow<List<Workspace>> = callbackFlow {
        val listener = col.whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { mapDoc(it.id, it.data ?: emptyMap()) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getAll(ownerId: String): List<Workspace> {
        val snap = col.whereEqualTo("ownerId", ownerId).get().await()
        return snap.documents.map { mapDoc(it.id, it.data ?: emptyMap()) }
    }

    override suspend fun create(ownerId: String, name: String) {
        val payload = mapOf(
            "ownerId" to ownerId,
            "name" to name.trim(),
            "date" to System.currentTimeMillis(),
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        col.add(payload).await()
    }

    override suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}
