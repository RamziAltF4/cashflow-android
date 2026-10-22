package com.ramzi.cashflow.presentation.dashboard.home

import com.ramzi.cashflow.domain.model.PlanConfig
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.User

data class HomeState(
    val loading: Boolean = true,
    val user: User? = null,
    val planConfig: PlanConfig? = null,

    // Statistik
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val txCount: Int = 0,

    // Visibility toggle
    val balanceHidden: Boolean = false,

    // Recent transactions (5 terbaru)
    val recentTransactions: List<Transaction> = emptyList(),

    // Chart data 7 hari terakhir
    val chartData: List<ChartPoint> = emptyList(),

    // ✅ Pending imports (dari notifikasi BCA)
    val pendingImportsCount: Int = 0,

    // Error
    val error: String? = null
) {
    val usageLabel: String
        get() {
            val limit = planConfig?.limit ?: 10
            return if (limit == Int.MAX_VALUE) "$txCount / ∞" else "$txCount / $limit"
        }

    val limitProgress: Float
        get() {
            val limit = planConfig?.limit ?: 10
            if (limit == Int.MAX_VALUE || limit <= 0) return 1f
            return (txCount.toFloat() / limit).coerceIn(0f, 1f)
        }

    val showLimitWarning: Boolean
        get() {
            val limit = planConfig?.limit ?: 10
            if (limit == Int.MAX_VALUE) return false
            return txCount >= limit * 0.8
        }
}

data class ChartPoint(
    val label: String,
    val income: Long,
    val expense: Long
)