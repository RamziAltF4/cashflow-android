package com.ramzi.cashflow.presentation.auth

import com.ramzi.cashflow.domain.model.AuthError

data class AuthUiState(
    val tab: AuthTab = AuthTab.LOGIN,

    // ---- Login form ----
    val loginEmail: String = "",
    val loginPassword: String = "",
    val loginPasswordVisible: Boolean = false,
    val loginLoading: Boolean = false,
    val loginError: AuthError? = null,

    // ---- Register step 1 (name + email) ----
    val regName: String = "",
    val regEmail: String = "",
    val regStep: Int = 1,
    val regLoading: Boolean = false,
    val regError: AuthError? = null,

    // ---- Register step 2 (password + confirm) ----
    val regPassword: String = "",
    val regConfirm: String = "",
    val regPasswordVisible: Boolean = false,
    val regConfirmVisible: Boolean = false,

    // ---- Pin quick login ----
    val savedIdentifier: String? = null,
    val pinDigits: String = "",
    val pinLoading: Boolean = false,
    val pinError: AuthError? = null,

    // ---- Reset password dialog ----
    val resetDialogOpen: Boolean = false,
    val resetEmail: String = "",
    val resetSending: Boolean = false,
    val resetSuccess: Boolean = false,
    val resetError: String? = null,

    // ---- Success ----
    val success: Boolean = false
) {
    // ---------- PASSWORD VALIDATION ----------
    val passwordMinLength: Boolean get() = regPassword.length >= 8
    val passwordHasUpper: Boolean get() = regPassword.any { it.isUpperCase() }
    val passwordHasDigit: Boolean get() = regPassword.any { it.isDigit() }
    val passwordHasSymbol: Boolean
        get() = regPassword.any { !it.isLetterOrDigit() }

    /** Semua syarat password terpenuhi. */
    val passwordValid: Boolean
        get() = passwordMinLength && passwordHasUpper && passwordHasDigit && passwordHasSymbol

    /** Konfirmasi password cocok dengan password. */
    val passwordsMatch: Boolean
        get() = regPassword.isNotEmpty() && regPassword == regConfirm

    /** Bar 0..1 — seberapa banyak syarat terpenuhi. */
    val passwordStrength: Float
        get() {
            val done = listOf(
                passwordMinLength,
                passwordHasUpper,
                passwordHasDigit,
                passwordHasSymbol
            ).count { it }
            return done / 4f
        }

    /** Step 1 valid (nama & email tidak kosong). */
    val registerStep1Valid: Boolean
        get() = regName.isNotBlank() && regEmail.isNotBlank() && regEmail.contains("@")
}

enum class AuthTab { LOGIN, REGISTER }