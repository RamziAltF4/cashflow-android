package com.ramzi.cashflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.currencyDataStore by preferencesDataStore(name = "currency_prefs")

/**
 * Menyimpan pilihan currency user secara lokal (cache).
 * Sumber kebenaran tetap di Firestore `users/{uid}.currencyCode`.
 */
class CurrencyPreference(private val context: Context) {

    companion object {
        private val KEY_CURRENCY = stringPreferencesKey("currency_code")
        const val DEFAULT = "IDR"
    }

    val currencyCode: Flow<String> = context.currencyDataStore.data
        .map { prefs -> prefs[KEY_CURRENCY] ?: DEFAULT }

    suspend fun setCurrency(code: String) {
        context.currencyDataStore.edit { prefs ->
            prefs[KEY_CURRENCY] = code.uppercase()
        }
    }
}