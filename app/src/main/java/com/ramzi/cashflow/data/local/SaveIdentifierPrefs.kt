package com.ramzi.cashflow.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Simpan identifier user (email) di DataStore.
 * Dipakai untuk PIN login — biar user tidak perlu isi email lagi.
 */
private val Context.savedIdDataStore by preferencesDataStore(name = "saved_identifier_prefs")

class SavedIdentifierPrefs(private val context: Context) {

    companion object {
        private val KEY_IDENTIFIER = stringPreferencesKey("saved_identifier")
    }

    private val appContext = context.applicationContext

    suspend fun set(identifier: String) {
        appContext.savedIdDataStore.edit { prefs ->
            prefs[KEY_IDENTIFIER] = identifier
        }
    }

    suspend fun get(): String? {
        return appContext.savedIdDataStore.data
            .map { prefs -> prefs[KEY_IDENTIFIER] }
            .first()
    }

    suspend fun clear() {
        appContext.savedIdDataStore.edit { prefs ->
            prefs.remove(KEY_IDENTIFIER)
        }
    }
}