package com.ramzi.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.Invoice
import com.ramzi.cashflow.domain.model.InvoiceStatus
import com.ramzi.cashflow.domain.repository.InvoiceRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InvoiceRepositoryImpl(
    private val firestore: FirebaseFirestore
) : InvoiceRepository {

    private val col = firestore.collection("invoices")

    private fun mapDoc(id: String, data: Map<String, Any?>): Invoice {
        return Invoice(
            id = id,
            userId = data["userId"] as? String ?: "",
            invoiceNumber = data["invoiceNumber"] as? String ?: "",
            clientName = data["clientName"] as? String ?: "",
            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
            status = InvoiceStatus.fromId(data["status"] as? String),
            date = (data["date"] as? Number)?.toLong() ?: 0L,
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp
        )
    }

    override fun observeAll(userId: String): Flow<List<Invoice>> = callbackFlow {
        val listener = col.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { mapDoc(it.id, it.data ?: emptyMap()) }
                    ?.sortedByDescending { it.date } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getAll(userId: String): List<Invoice> {
        val snap = col.whereEqualTo("userId", userId).get().await()
        return snap.documents.map { mapDoc(it.id, it.data ?: emptyMap()) }
            .sortedByDescending { it.date }
    }

    override suspend fun create(invoice: Invoice) {
        val payload = mapOf(
            "userId" to invoice.userId,
            "invoiceNumber" to invoice.invoiceNumber,
            "clientName" to invoice.clientName,
            "amount" to invoice.amount,
            "status" to invoice.status.id,
            "date" to if (invoice.date > 0) invoice.date else System.currentTimeMillis(),
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        col.add(payload).await()
    }

    override suspend fun update(invoice: Invoice) {
        val payload = mapOf(
            "invoiceNumber" to invoice.invoiceNumber,
            "clientName" to invoice.clientName,
            "amount" to invoice.amount,
            "status" to invoice.status.id
        )
        col.document(invoice.id).update(payload).await()
    }

    override suspend fun updateStatus(id: String, status: String) {
        col.document(id).update("status", status).await()
    }

    override suspend fun batchUpdateStatus(ids: List<String>, status: String) {
        if (ids.isEmpty()) return
        ids.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { id ->
                val ref = col.document(id)
                batch.update(ref, "status", status)
            }
            batch.commit().await()
        }
    }

    override suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}