package com.ramzi.cashflow.presentation.dashboard.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramzi.cashflow.domain.model.BillingMode
import com.ramzi.cashflow.domain.model.PlanConfig
import com.ramzi.cashflow.domain.model.PlanConfigs
import com.ramzi.cashflow.domain.model.UpgradeRequest
import com.ramzi.cashflow.domain.repository.ConfigRepository
import com.ramzi.cashflow.domain.repository.PlanRepository
import com.ramzi.cashflow.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlansViewModel(
    private val userRepository: UserRepository,
    private val planRepository: PlanRepository,
    private val configRepository: ConfigRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(PlansState())
    val state: StateFlow<PlansState> = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                plans = PlanConfigs.ordered,
                loading = false
            )
        }
        observeUser()
        checkPendingRequest()
        loadBankConfig()
    }

    private fun observeUser() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.observeById(uid).collect { user ->
                if (user != null) {
                    _state.update { it.copy(currentPlan = user.plan) }
                }
            }
        }
    }

    private fun checkPendingRequest() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val pending = planRepository.hasPendingRequest(uid)
                _state.update { it.copy(hasPendingRequest = pending) }
            } catch (_: Throwable) { }
        }
    }

    private fun loadBankConfig() {
        viewModelScope.launch {
            val bank = configRepository.getBankConfig()
            _state.update { it.copy(bankConfig = bank) }
        }
    }

    fun refreshBankConfig() = loadBankConfig()

    fun setBillingMode(mode: BillingMode) =
        _state.update { it.copy(billingMode = mode) }

    fun openUpgradeDialog(plan: PlanConfig) {
        if (_state.value.hasPendingRequest) {
            _state.update {
                it.copy(
                    submitError = "Kamu masih punya request yang menunggu verifikasi admin."
                )
            }
            return
        }
        _state.update {
            it.copy(showUpgradeDialog = true, selectedPlan = plan, submitError = null)
        }
    }

    fun closeUpgradeDialog() {
        if (_state.value.submitting) return
        _state.update {
            it.copy(
                showUpgradeDialog = false,
                selectedPlan = null,
                submitError = null,
                submitSuccess = false
            )
        }
    }

    fun dismissError() = _state.update { it.copy(submitError = null) }

    fun submitUpgradeRequest(proofBase64: String, note: String) {
        val uid = firebaseAuth.currentUser?.uid
        val selectedPlan = _state.value.selectedPlan ?: return
        val billing = _state.value.billingMode

        if (uid == null) {
            _state.update { it.copy(submitError = "Sesi tidak valid.") }
            return
        }

        val amount = selectedPlan.priceFor(billing)

        _state.update { it.copy(submitting = true, submitError = null) }

        viewModelScope.launch {
            try {
                val userProfile = userRepository.getById(uid)
                val currentPlan = userProfile?.plan
                    ?: com.ramzi.cashflow.domain.model.Plan.FREE

                val request = UpgradeRequest(
                    userId = uid,
                    currentPlan = currentPlan,
                    requestedPlan = selectedPlan.plan,
                    amount = amount,
                    billing = billing.id,
                    proof = proofBase64,
                    note = note.ifBlank { "Request dari Android." },
                    date = System.currentTimeMillis()
                )

                planRepository.submitUpgradeRequest(request)

                _state.update {
                    it.copy(
                        submitting = false,
                        submitSuccess = true,
                        showUpgradeDialog = false,
                        selectedPlan = null,
                        hasPendingRequest = true
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        submitting = false,
                        submitError = e.message ?: "Gagal mengirim request."
                    )
                }
            }
        }
    }
}