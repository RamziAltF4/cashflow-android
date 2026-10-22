package com.ramzi.cashflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.accentDataStore by preferencesDataStore(name = "accent_prefs")

class AccentPreference(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private val KEY_ACCENT = stringPreferencesKey("accent_id")
        const val DEFAULT = "indigo"
    }

    val accentId: Flow<String> = appContext.accentDataStore.data.map { prefs ->
        prefs[KEY_ACCENT] ?: DEFAULT
    }

    suspend fun setAccentId(id: String) {
        appContext.accentDataStore.edit { prefs ->
            prefs[KEY_ACCENT] = id
        }
    }
}
