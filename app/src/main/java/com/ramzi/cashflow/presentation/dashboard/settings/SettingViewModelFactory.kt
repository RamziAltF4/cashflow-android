package com.ramzi.cashflow.presentation.dashboard.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.local.AccentPreference
import com.ramzi.cashflow.data.local.BiometricPreference
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.data.local.LockTimeoutPreference
import com.ramzi.cashflow.data.local.ThemePreference
import com.ramzi.cashflow.data.remote.AuthApiModule
import com.ramzi.cashflow.data.repository.PinRepositoryImpl
import com.ramzi.cashflow.data.repository.TxRepositoryImpl
import com.ramzi.cashflow.data.repository.UserRepositoryImpl

class SettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                app = context.applicationContext as Application,
                userRepository = UserRepositoryImpl(firestore),
                txRepository = TxRepositoryImpl(firestore),
                pinRepository = PinRepositoryImpl(AuthApiModule.authApi),
                themePreference = ThemePreference(context.applicationContext),
                accentPreference = AccentPreference(context.applicationContext),
                biometricPreference = BiometricPreference(context.applicationContext),
                lockTimeoutPreference = LockTimeoutPreference(context.applicationContext),
                currencyPreference = CurrencyPreference(context.applicationContext),   // ⭐ BARU
                firebaseAuth = FirebaseAuth.getInstance()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}