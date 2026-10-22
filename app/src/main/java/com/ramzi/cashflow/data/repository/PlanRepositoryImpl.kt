package com.ramzi.cashflow.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.UpgradeRequest
import com.ramzi.cashflow.domain.repository.PlanRepository
import kotlinx.coroutines.tasks.await

class PlanRepositoryImpl(
    private val firestore: FirebaseFirestore
) : PlanRepository {

    private val col = firestore.collection("requests")

    override suspend fun submitUpgradeRequest(request: UpgradeRequest): String {
        val data = mapOf(
            "userId" to request.userId,
            "currentPlan" to request.currentPlan.id,
            "requestedPlan" to request.requestedPlan.id,
            "amount" to request.amount,
            "billing" to request.billing,
            "proof" to request.proof,               // base64 string
            "note" to request.note,
            "date" to System.currentTimeMillis(),
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        return col.add(data).await().id
    }

    override suspend fun hasPendingRequest(userId: String): Boolean {
        val snap = col
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "pending")
            .limit(1)
            .get()
            .await()
        return !snap.isEmpty
    }
}