package com.ramzi.cashflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.biometricDataStore by preferencesDataStore(name = "biometric_prefs")

class BiometricPreference(private val context: Context) {

    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    /** Flow status biometric (reactive) */
    val enabled: Flow<Boolean> = context.biometricDataStore.data
        .map { prefs -> prefs[KEY_ENABLED] ?: false }

    /** Simpan status biometric */
    suspend fun setEnabled(value: Boolean) {
        context.biometricDataStore.edit { prefs ->
            prefs[KEY_ENABLED] = value
        }
    }

    /** Ambil sekali (suspend) — untuk cek cepat */
    suspend fun isEnabled(): Boolean = enabled.first()
}