package com.ramzi.cashflow.presentation.dashboard.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.domain.repository.TxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val txRepository: TxRepository,
    private val firebaseAuth: FirebaseAuth,
    private val currencyPreference: CurrencyPreference   // ⭐ BARU
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private val chartPalette = listOf(
        androidx.compose.ui.graphics.Color(0xFF6366F1),
        androidx.compose.ui.graphics.Color(0xFF8B5CF6),
        androidx.compose.ui.graphics.Color(0xFFEC4899),
        androidx.compose.ui.graphics.Color(0xFFF43F5E),
        androidx.compose.ui.graphics.Color(0xFFF59E0B),
        androidx.compose.ui.graphics.Color(0xFF10B981),
        androidx.compose.ui.graphics.Color(0xFF06B6D4),
        androidx.compose.ui.graphics.Color(0xFF64748B)
    )

    init {
        observeCurrency()       // ⭐ BARU
        observeTransactions()
    }

    /* ============================================================
       ⭐ OBSERVE CURRENCY
       ============================================================ */
    private fun observeCurrency() {
        viewModelScope.launch {
            currencyPreference.currencyCode.collect { code ->
                _state.update { it.copy(currencyCode = code) }
            }
        }
    }

    /* ============================================================
       OBSERVE TRANSACTIONS
       ============================================================ */
    private fun observeTransactions() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            txRepository.observeByUser(uid).collect { txs ->
                val income = txs.filter { it.type.id == "income" }.sumOf { it.amount }
                val expense = txs.filter { it.type.id == "expense" }.sumOf { it.amount }

                val grouped = txs
                    .filter { it.type.id == "expense" }
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }

                val slices = grouped.mapIndexed { idx, (cat, amt) ->
                    CategorySlice(
                        category = cat,
                        amount = amt,
                        percentage = if (expense > 0) (amt.toFloat() / expense * 100f) else 0f,
                        color = chartPalette[idx % chartPalette.size]
                    )
                }

                val top = slices.firstOrNull()?.category ?: "-"

                _state.update {
                    it.copy(
                        loading = false,
                        transactions = txs,
                        totalIncome = income,
                        totalExpense = expense,
                        balance = income - expense,
                        categoryBreakdown = slices,
                        topCategory = top
                    )
                }
            }
        }
    }
}