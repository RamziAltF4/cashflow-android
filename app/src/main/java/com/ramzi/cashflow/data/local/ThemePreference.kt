package com.ramzi.cashflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore untuk menyimpan preferensi tema.
 * Nilai: "system" | "light" | "dark"
 */
private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

class ThemePreference(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        const val MODE_SYSTEM = "system"
        const val MODE_LIGHT = "light"
        const val MODE_DARK = "dark"
    }

    val themeMode: Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: MODE_SYSTEM
    }

    suspend fun setThemeMode(mode: String) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = mode
        }
    }
}