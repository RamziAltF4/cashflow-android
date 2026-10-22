package com.cashflow.presentation.auth

/**
 * State UI murni, diturunkan dari struktur form di index.html.
 * TIDAK mengandung business logic — logic menunggu login.js.
 *
 * View (auth view) di web: view-login-email, view-login-pin,
 * view-reg-email-1, view-reg-email-2, view-success.
 */
enum class AuthView {
    LOGIN_EMAIL,
    LOGIN_PIN,
    REG_EMAIL_1,
    REG_EMAIL_2,
    SUCCESS
}

enum class AuthTab { LOGIN, REGISTER }

data class LoginEmailFormState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

data class LoginPinFormState(
    val userId: String? = null,          // #lpin-id
    val pin: String = "",                // #lpin-val (maks 6, numeric)
    val loading: Boolean = false,
    val error: String? = null
)

data class RegEmail1FormState(
    val name: String = "",
    val email: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

data class RegEmail2FormState(
    val emailDisplay: String = "",       // #lbl-re-email
    val password: String = "",
    val confirm: String = "",
    val passwordVisible: Boolean = false,
    val confirmVisible: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
) {
    /** Padanan #pw-str: bar 0..1. Rule sebenarnya ada di login.js. */
    val strength: Float
        get() {
            var s = 0
            if (password.length >= 8) s++
            if (password.any { it.isUpperCase() }) s++
            if (password.any { it.isDigit() }) s++
            if (password.any { !it.isLetterOrDigit() }) s++
            return s / 4f
        }
}

data class AuthUiState(
    val view: AuthView = AuthView.LOGIN_EMAIL,
    val tab: AuthTab = AuthTab.LOGIN,
    val loginEmail: LoginEmailFormState = LoginEmailFormState(),
    val loginPin: LoginPinFormState = LoginPinFormState(),
    val reg1: RegEmail1FormState = RegEmail1FormState(),
    val reg2: RegEmail2FormState = RegEmail2FormState()
)