package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.UpgradeRequest

interface PlanRepository {

    /**
     * Kirim request upgrade ke Firestore `requests/{auto}`.
     * @param request data request (userId, plan, amount, proof base64, dll)
     * @return id dokumen yang dibuat
     */
    suspend fun submitUpgradeRequest(request: UpgradeRequest): String

    /**
     * Cek apakah user masih punya request pending.
     */
    suspend fun hasPendingRequest(userId: String): Boolean
}