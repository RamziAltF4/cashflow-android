package com.ramzi.cashflow.domain.model

import com.google.firebase.Timestamp

/**
 * Model user Cashflow.
 *
 * Field lengkap sesuai Firestore `users/{uid}`.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",

    val plan: Plan = Plan.FREE,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val transactions: Int = 0,
    val joined: Long = 0L,
    val createdAt: Timestamp? = null,
    val avatarUrl: String? = null,

    // Dashboard PIN (client-side SHA-256) — tidak dipakai di Android
    val pinEnabled: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null,

    // Biometric / WebAuthn
    val biometricEnabled: Boolean = false,

    // 2FA (Pro & Business)
    val twoFAEnabled: Boolean = false,
    val twoFASecret: String? = null,
    val twoFAEnabledAt: Timestamp? = null,

    // Business — budget bulanan (Rp)
    val budget: Long = 0L,

    // ⭐ BARU — display currency (ISO 4217). Base transaksi tetap IDR.
    val currencyCode: String = "IDR",

    // Business — API key
    val apiKey: String? = null,
    val apiKeyCreatedAt: Long = 0L,

    // Preferences notifikasi
    val preferences: UserPreferences = UserPreferences()
) {
    /** Label plan untuk UI ("Free", "Pro", "Business"). */
    val planLabel: String
        get() = PlanConfigs.of(plan).label

    /** Limit transaksi — `Int.MAX_VALUE` untuk unlimited. */
    val planLimit: Int
        get() = PlanConfigs.of(plan).limit

    /** Apakah plan unlimited (Business). */
    val isUnlimited: Boolean
        get() = planLimit == Int.MAX_VALUE

    /** ⭐ BARU — shortcut currency uppercase. */
    val displayCurrency: String
        get() = currencyCode.uppercase().ifBlank { "IDR" }
}

data class UserPreferences(
    val emailWeekly: Boolean = true,
    val budgetAlert: Boolean = true,
    val promo: Boolean = false
)