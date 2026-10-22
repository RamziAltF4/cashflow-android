package com.ramzi.cashflow.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.repository.TxRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TxRepositoryImpl(
    private val firestore: FirebaseFirestore
) : TxRepository {

    private val col = firestore.collection("transactions")

    override fun observeByUser(userId: String): Flow<List<Transaction>> = callbackFlow {
        val query = col.whereEqualTo("userId", userId)
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w("TxRepository", "Snapshot error: ${err.message}")
                // Kirim list kosong, JANGAN close — biar flow tetap hidup kalau auth kembali
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { doc ->
                Transaction(
                    id = doc.id,
                    userId = doc.getString("userId").orEmpty(),
                    type = TxType.fromId(doc.getString("type")),
                    desc = doc.getString("desc").orEmpty(),
                    amount = doc.getLong("amount") ?: 0L,
                    category = doc.getString("category").orEmpty(),
                    date = doc.getString("date").orEmpty(),
                    note = doc.getString("note").orEmpty(),
                    createdAt = doc.getTimestamp("createdAt"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun add(tx: Transaction): String {
        val data = mapOf<String, Any?>(
            "userId" to tx.userId,
            "type" to tx.type.id,
            "desc" to tx.desc,
            "amount" to tx.amount,
            "category" to tx.category,
            "date" to tx.date,
            "note" to tx.note,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return col.add(data).await().id
    }

    override suspend fun update(txId: String, tx: Transaction) {
        val data = mapOf<String, Any?>(
            "type" to tx.type.id,
            "desc" to tx.desc,
            "amount" to tx.amount,
            "category" to tx.category,
            "date" to tx.date,
            "note" to tx.note,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        col.document(txId).update(data).await()
    }

    override suspend fun delete(txId: String) {
        col.document(txId).delete().await()
    }

    override suspend fun deleteAllForUser(userId: String) {
        val snap = col.whereEqualTo("userId", userId).get().await()
        val batch = firestore.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    override suspend fun getById(txId: String): Transaction? {
        val doc = col.document(txId).get().await()
        if (!doc.exists()) return null
        return Transaction(
            id = doc.id,
            userId = doc.getString("userId").orEmpty(),
            type = TxType.fromId(doc.getString("type")),
            desc = doc.getString("desc").orEmpty(),
            amount = doc.getLong("amount") ?: 0L,
            category = doc.getString("category").orEmpty(),
            date = doc.getString("date").orEmpty(),
            note = doc.getString("note").orEmpty(),
            createdAt = doc.getTimestamp("createdAt"),
            updatedAt = doc.getTimestamp("updatedAt")
        )
    }
}