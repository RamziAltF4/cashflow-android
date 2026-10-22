package com.ramzi.cashflow.presentation.dashboard.settings

import android.app.Application
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramzi.cashflow.data.local.AccentPreference
import com.ramzi.cashflow.data.local.BiometricPreference
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.data.local.LockTimeoutPreference
import com.ramzi.cashflow.data.local.ThemePreference
import com.ramzi.cashflow.domain.model.AccentColors
import com.ramzi.cashflow.domain.model.PlanConfigs
import com.ramzi.cashflow.domain.repository.PinRepository
import com.ramzi.cashflow.domain.repository.PinSetException
import com.ramzi.cashflow.domain.repository.TxRepository
import com.ramzi.cashflow.domain.repository.UserRepository
import com.ramzi.cashflow.util.BiometricAuthHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(
    private val app: Application,
    private val userRepository: UserRepository,
    private val txRepository: TxRepository,
    private val pinRepository: PinRepository,
    private val themePreference: ThemePreference,
    private val accentPreference: AccentPreference,
    private val biometricPreference: BiometricPreference,
    private val lockTimeoutPreference: LockTimeoutPreference,
    private val currencyPreference: CurrencyPreference,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val TAG = "BIOMETRIC_VM"

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private var biometricInProgress = false

    init {
        Log.d(TAG, "init() start")
        observeUser()
        observeTheme()
        observeAccent()
        observeBiometric()
        observeLockTimeout()
        observeCurrency()
        checkBiometricAvailability()
    }

    // ============================================================
    // OBSERVE
    // ============================================================
    private fun observeUser() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.observeById(uid).collect { user ->
                if (user != null) {
                    val planCfg = PlanConfigs.of(user.plan)
                    _state.update {
                        it.copy(
                            loading = false,
                            user = user,
                            planConfig = planCfg,
                            editName = if (it.editName.isBlank()) user.name else it.editName,
                            txCount = user.transactions,
                            availableAccents = AccentColors.forPlan(user.plan)
                        )
                    }
                    // ⭐ Sync Firestore → lokal (kalau beda)
                    if (user.currencyCode.isNotBlank() &&
                        user.currencyCode.uppercase() != _state.value.currencyCode.uppercase()
                    ) {
                        currencyPreference.setCurrency(user.currencyCode)
                    }
                }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreference.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }

    private fun observeAccent() {
        viewModelScope.launch {
            accentPreference.accentId.collect { id ->
                _state.update { it.copy(accentId = id) }
            }
        }
    }

    private fun observeBiometric() {
        viewModelScope.launch {
            biometricPreference.enabled.collect { enabled ->
                Log.d(TAG, "observeBiometric: enabled=$enabled")
                _state.update { it.copy(biometricEnabled = enabled) }
            }
        }
    }

    private fun observeLockTimeout() {
        viewModelScope.launch {
            lockTimeoutPreference.timeoutMs.collect { ms ->
                Log.d(TAG, "observeLockTimeout: ms=$ms")
                _state.update { it.copy(lockTimeoutMs = ms) }
            }
        }
    }

    // ⭐ BARU
    private fun observeCurrency() {
        viewModelScope.launch {
            currencyPreference.currencyCode.collect { code ->
                _state.update { it.copy(currencyCode = code) }
            }
        }
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val available = BiometricAuthHelper.canAuthenticate(app)
            Log.d(TAG, "checkBiometricAvailability: available=$available")
            _state.update { it.copy(biometricAvailable = available) }
        }
    }

    // ============================================================
    // PROFILE
    // ============================================================
    fun onEditNameChange(v: String) =
        _state.update { it.copy(editName = v, profileSaved = false) }

    fun saveProfile() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val newName = _state.value.editName.trim()
        if (newName.isBlank()) return

        _state.update { it.copy(savingProfile = true, error = null) }
        viewModelScope.launch {
            try {
                userRepository.updateName(uid, newName)
                _state.update { it.copy(savingProfile = false, profileSaved = true) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(savingProfile = false, error = e.message ?: "Gagal simpan")
                }
            }
        }
    }

    fun resetProfileField() {
        val originalName = _state.value.user?.name.orEmpty()
        _state.update { it.copy(editName = originalName, profileSaved = false) }
    }

    // ============================================================
    // THEME & ACCENT
    // ============================================================
    fun setThemeMode(mode: String) {
        viewModelScope.launch { themePreference.setThemeMode(mode) }
    }

    fun setAccent(id: String) {
        val s = _state.value
        if (!s.canChangeAccent) return
        if (s.availableAccents.none { it.id == id }) return
        viewModelScope.launch { accentPreference.setAccentId(id) }
    }

    // ============================================================
    // ⭐ BIOMETRIC
    // ============================================================
    fun toggleBiometric(value: Boolean, activity: FragmentActivity) {
        Log.d(TAG, "toggleBiometric(value=$value) called")
        Log.d(TAG, "biometricInProgress=$biometricInProgress")

        if (biometricInProgress) {
            Log.d(TAG, "SKIP: biometricInProgress=true")
            return
        }
        biometricInProgress = true

        viewModelScope.launch {
            _state.update { it.copy(biometricToggling = true) }
            try {
                if (value) {
                    Log.d(TAG, "checking canAuthenticate...")
                    val available = BiometricAuthHelper.canAuthenticate(app)
                    Log.d(TAG, "canAuthenticate = $available")

                    if (!available) {
                        _state.update {
                            it.copy(
                                biometricToggling = false,
                                error = "Perangkat tidak mendukung biometrik"
                            )
                        }
                        Log.d(TAG, "return: not available")
                        return@launch
                    }

                    Log.d(TAG, "calling BiometricAuthHelper.authenticate()...")
                    val ok = BiometricAuthHelper.authenticate(
                        activity = activity,
                        title = "Aktifkan Login Biometrik",
                        subtitle = "Verifikasi identitas untuk mengaktifkan"
                    )
                    Log.d(TAG, "authenticate result = $ok")

                    if (!ok) {
                        _state.update { it.copy(biometricToggling = false) }
                        Log.d(TAG, "return: auth failed/cancelled")
                        return@launch
                    }
                }

                Log.d(TAG, "saving biometricPreference=$value")
                biometricPreference.setEnabled(value)

                _state.update {
                    it.copy(
                        biometricEnabled = value,
                        biometricToggling = false,
                        error = null
                    )
                }
                Log.d(TAG, "SUCCESS: biometricEnabled=$value")
            } catch (e: Throwable) {
                Log.e(TAG, "ERROR: ${e.message}", e)
                _state.update {
                    it.copy(
                        biometricToggling = false,
                        error = e.message ?: "Gagal ubah biometrik"
                    )
                }
            } finally {
                biometricInProgress = false
            }
        }
    }

    // ============================================================
    // ⭐ AUTO-LOCK TIMEOUT
    // ============================================================
    fun showLockTimeoutSheet() = _state.update {
        it.copy(showLockTimeoutSheet = true)
    }

    fun dismissLockTimeoutSheet() = _state.update {
        it.copy(showLockTimeoutSheet = false)
    }

    fun setLockTimeout(ms: Int) {
        viewModelScope.launch {
            Log.d(TAG, "setLockTimeout: ms=$ms")
            lockTimeoutPreference.setTimeout(ms)
            _state.update { it.copy(showLockTimeoutSheet = false) }
        }
    }

    // ============================================================
    // ⭐ CURRENCY (BARU)
    // ============================================================
    fun showCurrencySheet() = _state.update {
        it.copy(showCurrencySheet = true)
    }

    fun dismissCurrencySheet() = _state.update {
        it.copy(showCurrencySheet = false)
    }

    fun setCurrency(code: String) {
        val uid = firebaseAuth.currentUser?.uid
        viewModelScope.launch {
            // 1. Simpan lokal dulu (instant)
            currencyPreference.setCurrency(code)
            _state.update { it.copy(currencyCode = code, showCurrencySheet = false) }

            // 2. Sync ke Firestore (kalau login)
            if (uid != null) {
                runCatching { userRepository.updateCurrency(uid, code) }
                    .onFailure { e ->
                        Log.e(TAG, "Gagal sync currency ke Firestore: ${e.message}")
                    }
            }
        }
    }

    // ============================================================
    // SET PIN
    // ============================================================
    fun showSetPinSheet() = _state.update {
        it.copy(showSetPinSheet = true, pinError = null, pinSuccess = false)
    }

    fun dismissSetPinSheet() = _state.update {
        it.copy(showSetPinSheet = false, pinError = null, pinSuccess = false, pinSaving = false)
    }

    fun savePin(pin: String, onSuccess: () -> Unit = {}) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _state.update { it.copy(pinError = "Sesi Firebase tidak valid.") }
            return
        }

        _state.update { it.copy(pinSaving = true, pinError = null) }
        viewModelScope.launch {
            try {
                val idToken = currentUser.getIdToken(true).await().token
                if (idToken.isNullOrBlank()) {
                    _state.update {
                        it.copy(pinSaving = false, pinError = "Gagal ambil ID token.")
                    }
                    return@launch
                }

                pinRepository.setPin(idToken, pin)
                userRepository.enablePinFlag(currentUser.uid)

                _state.update {
                    it.copy(
                        pinSaving = false,
                        pinSuccess = true,
                        pinError = null,
                        showSetPinSheet = false
                    )
                }
                onSuccess()
            } catch (e: PinSetException) {
                _state.update {
                    it.copy(pinSaving = false, pinError = e.message_)
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        pinSaving = false,
                        pinError = e.message ?: "Gagal menyimpan PIN."
                    )
                }
            }
        }
    }

    // ============================================================
    // 2FA
    // ============================================================
    fun show2FASheet() {
        val s = _state.value
        if (!s.canUse2FA) {
            _state.update { it.copy(error = "2FA khusus Pro & Business.") }
            return
        }
        _state.update {
            it.copy(show2FASheet = true, twoFAError = null, twoFASuccess = false)
        }
    }

    fun dismiss2FASheet() = _state.update {
        it.copy(show2FASheet = false, twoFAError = null, twoFASuccess = false, twoFASaving = false)
    }

    fun verify2FA(secret: String) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            _state.update { it.copy(twoFAError = "Sesi Firebase tidak valid.") }
            return
        }

        _state.update { it.copy(twoFASaving = true, twoFAError = null) }
        viewModelScope.launch {
            try {
                userRepository.enable2FA(uid, secret)
                _state.update {
                    it.copy(
                        twoFASaving = false,
                        twoFASuccess = true,
                        twoFAError = null,
                        show2FASheet = false
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        twoFASaving = false,
                        twoFAError = e.message ?: "Gagal mengaktifkan 2FA."
                    )
                }
            }
        }
    }

    fun disable2FA() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.disable2FA(uid)
            } catch (_: Throwable) { }
        }
    }

    // ============================================================
    // DIALOGS
    // ============================================================
    fun showLogoutConfirm() = _state.update { it.copy(showLogoutConfirm = true) }
    fun dismissLogoutConfirm() = _state.update { it.copy(showLogoutConfirm = false) }

    fun showClearDataConfirm() = _state.update { it.copy(showClearDataConfirm = true) }
    fun dismissClearDataConfirm() = _state.update { it.copy(showClearDataConfirm = false) }

    fun showDeleteAccountConfirm() = _state.update { it.copy(showDeleteAccountConfirm = true) }
    fun dismissDeleteAccountConfirm() = _state.update { it.copy(showDeleteAccountConfirm = false) }

    // ============================================================
    // ACTIONS
    // ============================================================
    fun clearAllTransactions(onDone: () -> Unit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                txRepository.deleteAllForUser(uid)
                _state.update { it.copy(showClearDataConfirm = false) }
                onDone()
            } catch (e: Throwable) {
                _state.update {
                    it.copy(showClearDataConfirm = false, error = e.message)
                }
            }
        }
    }
}