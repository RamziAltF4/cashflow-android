package com.ramzi.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.Customer
import com.ramzi.cashflow.domain.model.CustomerType
import com.ramzi.cashflow.domain.repository.CustomerRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CustomerRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CustomerRepository {

    private val col = firestore.collection("customers")

    private fun mapDoc(id: String, data: Map<String, Any?>): Customer {
        return Customer(
            id = id,
            userId = data["userId"] as? String ?: "",
            name = data["name"] as? String ?: "",
            phone = data["phone"] as? String ?: "",
            type = CustomerType.fromId(data["type"] as? String),
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp
        )
    }

    override fun observeAll(userId: String): Flow<List<Customer>> = callbackFlow {
        val listener = col.whereEqualTo("userId", userId)
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

    override suspend fun getAll(userId: String): List<Customer> {
        val snap = col.whereEqualTo("userId", userId).get().await()
        return snap.documents.map { mapDoc(it.id, it.data ?: emptyMap()) }
    }

    override suspend fun create(customer: Customer) {
        val payload = mapOf(
            "userId" to customer.userId,
            "name" to customer.name,
            "phone" to customer.phone,
            "type" to customer.type.id,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        col.add(payload).await()
    }

    override suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}