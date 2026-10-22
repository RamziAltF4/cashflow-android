package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {

    /** Observe log user (limit 20 terbaru). */
    fun observeRecent(userId: String, limit: Long = 20): Flow<List<ActivityLog>>

    suspend fun getRecent(userId: String, limit: Long = 20): List<ActivityLog>

    /** Log aktivitas baru (dipanggil dari fitur-fitur business). */
    suspend fun log(userId: String, action: String, detail: String)
}