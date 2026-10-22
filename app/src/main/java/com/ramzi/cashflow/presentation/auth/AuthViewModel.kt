package com.ramzi.cashflow.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.ramzi.cashflow.data.local.SavedIdentifierPrefs
import com.ramzi.cashflow.data.repository.PinLoginException
import com.ramzi.cashflow.domain.model.AuthError
import com.ramzi.cashflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    private val savedIdentifierPrefs: SavedIdentifierPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // Baca saved identifier dari DataStore saat init
        viewModelScope.launch {
            val saved = savedIdentifierPrefs.get()
            _state.update { it.copy(savedIdentifier = saved) }
        }
    }

    // ---- TAB ----
    fun setTab(tab: AuthTab) {
        _state.update {
            it.copy(
                tab = tab,
                loginError = null,
                regError = null,
                regStep = if (tab == AuthTab.REGISTER) 1 else it.regStep
            )
        }
    }

    // ---- LOGIN EMAIL ----
    fun onLoginEmailChange(v: String) =
        _state.update { it.copy(loginEmail = v, loginError = null) }

    fun onLoginPasswordChange(v: String) =
        _state.update { it.copy(loginPassword = v, loginError = null) }

    fun toggleLoginPasswordVisible() =
        _state.update { it.copy(loginPasswordVisible = !it.loginPasswordVisible) }

    fun onLogin(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.loginEmail.isBlank() || s.loginPassword.isBlank()) {
            _state.update { it.copy(loginError = AuthError.UNKNOWN) }
            return
        }
        _state.update { it.copy(loginLoading = true, loginError = null) }
        viewModelScope.launch {
            try {
                val identifierClean = s.loginEmail.trim().lowercase()
                authRepository.signInWithEmail(identifierClean, s.loginPassword)

                // ✅ SIMPAN identifier ke DataStore untuk PIN login nanti
                savedIdentifierPrefs.set(identifierClean)

                _state.update {
                    it.copy(
                        loginLoading = false,
                        success = true,
                        savedIdentifier = identifierClean
                    )
                }
                onSuccess()
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loginLoading = false, loginError = mapFirebaseError(e))
                }
            }
        }
    }

    // ---- REGISTER ----
    fun onRegNameChange(v: String) =
        _state.update { it.copy(regName = v, regError = null) }
    fun onRegEmailChange(v: String) =
        _state.update { it.copy(regEmail = v, regError = null) }
    fun onRegPasswordChange(v: String) =
        _state.update { it.copy(regPassword = v, regError = null) }
    fun onRegConfirmChange(v: String) =
        _state.update { it.copy(regConfirm = v, regError = null) }
    fun toggleRegPasswordVisible() =
        _state.update { it.copy(regPasswordVisible = !it.regPasswordVisible) }
    fun toggleRegConfirmVisible() =
        _state.update { it.copy(regConfirmVisible = !it.regConfirmVisible) }

    fun onRegisterStep1() {
        val s = _state.value
        if (s.regName.isBlank() || s.regEmail.isBlank()) {
            _state.update { it.copy(regError = AuthError.UNKNOWN) }
            return
        }
        _state.update { it.copy(regStep = 2, regError = null) }
    }

    fun onRegisterStep1Back() =
        _state.update { it.copy(regStep = 1, regError = null) }

    fun onRegisterStep2(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.regPassword != s.regConfirm) {
            _state.update { it.copy(regError = AuthError.UNKNOWN) }
            return
        }
        if (s.regPassword.length < 8) {
            _state.update { it.copy(regError = AuthError.WEAK_PASSWORD) }
            return
        }
        _state.update { it.copy(regLoading = true, regError = null) }
        viewModelScope.launch {
            try {
                val emailClean = s.regEmail.trim().lowercase()
                authRepository.register(
                    name = s.regName.trim(),
                    email = emailClean,
                    password = s.regPassword
                )

                // ✅ Simpan identifier juga setelah register
                savedIdentifierPrefs.set(emailClean)

                _state.update {
                    it.copy(
                        regLoading = false,
                        success = true,
                        savedIdentifier = emailClean
                    )
                }
                onSuccess()
            } catch (e: Throwable) {
                _state.update {
                    it.copy(regLoading = false, regError = mapFirebaseError(e))
                }
            }
        }
    }

    // ---- PIN LOGIN ----
    fun onPinInput(raw: String) {
        val filtered = raw.filter { it.isDigit() }.take(6)
        _state.update { it.copy(pinDigits = filtered, pinError = null) }
    }

    fun onPinSubmit(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.pinDigits.length != 6) return

        val identifier = s.savedIdentifier ?: s.loginEmail.ifBlank { null }
        if (identifier.isNullOrBlank()) {
            _state.update {
                it.copy(pinError = AuthError.ACCOUNT_NOT_FOUND)
            }
            return
        }

        _state.update { it.copy(pinLoading = true, pinError = null) }
        viewModelScope.launch {
            try {
                authRepository.signInWithPin(identifier, s.pinDigits)
                _state.update {
                    it.copy(pinLoading = false, success = true, pinDigits = "")
                }
                onSuccess()
            } catch (e: PinLoginException) {
                _state.update {
                    it.copy(pinLoading = false, pinError = e.error, pinDigits = "")
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        pinLoading = false,
                        pinError = AuthError.fromServerMessage(e.message),
                        pinDigits = ""
                    )
                }
            }
        }
    }

    fun setSavedIdentifier(identifier: String?) {
        _state.update { it.copy(savedIdentifier = identifier) }
        viewModelScope.launch {
            if (identifier != null) savedIdentifierPrefs.set(identifier)
        }
    }

    // ---- RESET PASSWORD ----
    fun openResetPasswordDialog() {
        val email = _state.value.loginEmail.trim()
        _state.update {
            it.copy(
                resetDialogOpen = true,
                resetEmail = email,
                resetError = null,
                resetSuccess = false
            )
        }
    }

    fun closeResetPasswordDialog() {
        _state.update {
            it.copy(
                resetDialogOpen = false,
                resetError = null,
                resetSuccess = false,
                resetSending = false
            )
        }
    }

    fun onResetEmailChange(v: String) =
        _state.update { it.copy(resetEmail = v, resetError = null) }

    fun sendPasswordReset() {
        val email = _state.value.resetEmail.trim()
        if (email.isBlank() || !email.contains("@")) {
            _state.update { it.copy(resetError = "Masukkan email yang valid.") }
            return
        }
        _state.update { it.copy(resetSending = true, resetError = null) }
        viewModelScope.launch {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                _state.update {
                    it.copy(
                        resetSending = false,
                        resetSuccess = true,
                        resetError = null
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        resetSending = false,
                        resetError = e.message ?: "Gagal kirim email reset."
                    )
                }
            }
        }
    }

    // ---- ERROR MAPPING ----
    private fun mapFirebaseError(e: Throwable): AuthError = when (e) {
        is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
        is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthRecentLoginRequiredException -> AuthError.TOO_MANY_REQUESTS
        else -> AuthError.fromServerMessage(e.message)
    }
}