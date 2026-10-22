package com.ramzi.cashflow.presentation.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.repository.ConfigRepository
import com.ramzi.cashflow.domain.repository.DetectedTxRepository
import com.ramzi.cashflow.domain.repository.TxRepository
import com.ramzi.cashflow.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val userRepository: UserRepository,
    private val txRepository: TxRepository,
    private val detectedTxRepository: DetectedTxRepository,   // ✅ BARU
    private val configRepository: ConfigRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeUser()
        observeTransactions()
        observePendingImports()   // ✅ BARU
    }

    /* ============================================================
       OBSERVE USER
       ============================================================ */
    private fun observeUser() {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            _state.update { it.copy(loading = false) }
            return
        }
        viewModelScope.launch {
            userRepository.observeById(uid).collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            user = user,
                            planConfig = com.ramzi.cashflow.domain.model.PlanConfigs.of(user.plan)
                        )
                    }
                } else {
                    _state.update { it.copy(loading = false) }
                }
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
                val income = txs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
                val expense = txs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                val balance = income - expense

                _state.update {
                    it.copy(
                        loading = false,
                        totalIncome = income,
                        totalExpense = expense,
                        balance = balance,
                        txCount = txs.size,
                        recentTransactions = txs
                            .sortedByDescending { tx -> tx.date }
                            .take(5),
                        chartData = buildChartData(txs)
                    )
                }
            }
        }
    }

    /* ============================================================
       OBSERVE PENDING IMPORTS (dari notifikasi BCA) — ✅ BARU
       ============================================================ */
    private fun observePendingImports() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            detectedTxRepository.observePending(uid).collect { list ->
                _state.update { it.copy(pendingImportsCount = list.size) }
            }
        }
    }

    /* ============================================================
       BUILD CHART DATA (7 hari terakhir)
       ============================================================ */
    private fun buildChartData(txs: List<Transaction>): List<ChartPoint> {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelSdf = SimpleDateFormat("EEE", Locale("id", "ID"))

        val result = mutableListOf<ChartPoint>()
        for (i in 6 downTo 0) {
            val d = cal.clone() as Calendar
            d.add(Calendar.DAY_OF_YEAR, -i)
            val key = sdf.format(d.time)
            val label = labelSdf.format(d.time).take(3)

            val dayIncome = txs
                .filter { it.type == TxType.INCOME && it.date == key }
                .sumOf { it.amount }
            val dayExpense = txs
                .filter { it.type == TxType.EXPENSE && it.date == key }
                .sumOf { it.amount }

            result.add(
                ChartPoint(
                    label = label,
                    income = dayIncome,
                    expense = dayExpense
                )
            )
        }
        return result
    }

    /* ============================================================
       TOGGLE BALANCE VISIBILITY
       ============================================================ */
    fun toggleBalanceVisibility() {
        _state.update { it.copy(balanceHidden = !it.balanceHidden) }
    }

    /* ============================================================
       REFRESH
       ============================================================ */
    fun refresh() {
        observeUser()
        observeTransactions()
        observePendingImports()
    }
}