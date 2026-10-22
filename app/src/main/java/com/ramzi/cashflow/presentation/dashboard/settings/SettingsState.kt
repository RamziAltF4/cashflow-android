package com.ramzi.cashflow.presentation.dashboard.settings

import com.ramzi.cashflow.domain.model.AccentColor
import com.ramzi.cashflow.domain.model.PlanConfig
import com.ramzi.cashflow.domain.model.User

data class SettingsState(
    val loading: Boolean = true,
    val user: User? = null,
    val planConfig: PlanConfig? = null,
    val txCount: Int = 0,

    // Edit profile
    val editName: String = "",
    val savingProfile: Boolean = false,
    val profileSaved: Boolean = false,

    // Theme
    val themeMode: String = "system",
    val accentId: String = "indigo",
    val availableAccents: List<AccentColor> = emptyList(),

    // Dialog
    val showLogoutConfirm: Boolean = false,
    val showClearDataConfirm: Boolean = false,
    val showDeleteAccountConfirm: Boolean = false,

    // Set PIN
    val showSetPinSheet: Boolean = false,
    val pinSaving: Boolean = false,
    val pinError: String? = null,
    val pinSuccess: Boolean = false,

    // 2FA
    val show2FASheet: Boolean = false,
    val twoFASaving: Boolean = false,
    val twoFAError: String? = null,
    val twoFASuccess: Boolean = false,

    // ⭐ Biometric
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val biometricToggling: Boolean = false,

    // ⭐ Auto-Lock Timeout
    val lockTimeoutMs: Int = 30_000,
    val showLockTimeoutSheet: Boolean = false,

    // ⭐ BARU — Currency
    val currencyCode: String = "IDR",
    val showCurrencySheet: Boolean = false,

    val error: String? = null
) {
    val canChangeAccent: Boolean
        get() = planConfig?.plan?.id == "pro" || planConfig?.plan?.id == "business"

    val isBusiness: Boolean
        get() = planConfig?.plan?.id == "business"

    val canUse2FA: Boolean
        get() = planConfig?.plan?.id == "pro" || planConfig?.plan?.id == "business"

    /** ⭐ BARU — shortcut label currency uppercase. */
    val currencyLabel: String
        get() = currencyCode.uppercase().ifBlank { "IDR" }
}