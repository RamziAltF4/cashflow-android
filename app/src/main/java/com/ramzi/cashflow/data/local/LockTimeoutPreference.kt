package com.ramzi.cashflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lockTimeoutDataStore by preferencesDataStore(name = "lock_timeout_prefs")

class LockTimeoutPreference(private val context: Context) {

    companion object {
        private val KEY_TIMEOUT_MS = intPreferencesKey("lock_timeout_ms")

        // Opsi timeout (ms)
        const val IMMEDIATELY = 0
        const val SEC_30 = 30_000
        const val MIN_1 = 60_000
        const val MIN_2 = 120_000
        const val MIN_5 = 300_000

        val ALL_OPTIONS = listOf(
            IMMEDIATELY to "Segera",
            SEC_30 to "30 detik",
            MIN_1 to "1 menit",
            MIN_2 to "2 menit",
            MIN_5 to "5 menit"
        )
    }

    val timeoutMs: Flow<Int> = context.lockTimeoutDataStore.data
        .map { prefs -> prefs[KEY_TIMEOUT_MS] ?: SEC_30 }  // default 30 detik

    suspend fun setTimeout(ms: Int) {
        context.lockTimeoutDataStore.edit { prefs ->
            prefs[KEY_TIMEOUT_MS] = ms
        }
    }
}