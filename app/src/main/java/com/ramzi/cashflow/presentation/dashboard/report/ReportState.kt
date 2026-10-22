package com.ramzi.cashflow.presentation.dashboard.report

import androidx.compose.ui.graphics.Color
import com.ramzi.cashflow.domain.model.Transaction

data class ReportState(
    val loading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),

    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,

    val categoryBreakdown: List<CategorySlice> = emptyList(),
    val topCategory: String = "-",

    // ---------- Interaksi ----------
    val selectedCategory: String? = null,

    // ⭐ BARU — display currency
    val currencyCode: String = "IDR"
) {
    val hasData: Boolean get() = transactions.isNotEmpty()
}

data class CategorySlice(
    val category: String,
    val amount: Long,
    val percentage: Float,
    val color: Color
)