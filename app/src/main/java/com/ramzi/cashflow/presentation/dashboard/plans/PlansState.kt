package com.ramzi.cashflow.presentation.dashboard.plans

import com.ramzi.cashflow.domain.model.BankConfig
import com.ramzi.cashflow.domain.model.BillingMode
import com.ramzi.cashflow.domain.model.Plan
import com.ramzi.cashflow.domain.model.PlanConfig

data class PlansState(
    val loading: Boolean = true,
    val currentPlan: Plan = Plan.FREE,
    val billingMode: BillingMode = BillingMode.MONTHLY,
    val plans: List<PlanConfig> = emptyList(),

    // Dialog upgrade
    val showUpgradeDialog: Boolean = false,
    val selectedPlan: PlanConfig? = null,

    // Submit state
    val submitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false,
    val hasPendingRequest: Boolean = false,

    // Rekening bank dari admin (Firestore: config/bank)
    val bankConfig: BankConfig = BankConfig.EMPTY
)