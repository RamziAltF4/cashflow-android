package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.User
import com.ramzi.cashflow.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Padanan operasi user di core.js:
 *   - loadUserData()  → getDoc(users/{uid})
 *   - updateDoc(users/{uid}, {...}) — untuk save profile, preferences, 2FA, dll
 *   - users.budget    → saveBudget()
 */
interface UserRepository {

    /** Realtime stream user data. Padanan loadUserData() di core.js. */
    fun observeById(uid: String): Flow<User?>

    /** Ambil sekali (untuk splash/auth check). */
    suspend fun getById(uid: String): User?

    /** Update nama profil (padanan #saveProfile / #saveAllSettings). */
    suspend fun updateName(uid: String, name: String)

    /** Update preferences notifikasi. */
    suspend fun updatePreferences(uid: String, prefs: UserPreferences)

    /** Update 1 preference (padanan sync pref di pro.js/business.js). */
    suspend fun updatePreference(uid: String, key: String, value: Boolean)

    /** Business only — set budget bulanan. */
    suspend fun updateBudget(uid: String, amount: Long)

    /** Simpan 2FA secret setelah verifikasi. */
    suspend fun enable2FA(uid: String, secret: String)

    /** Matikan 2FA. */
    suspend fun disable2FA(uid: String)

    /** Business only — set API key. */
    suspend fun updateApiKey(uid: String, apiKey: String)

    suspend fun enablePinFlag(uid: String)

    /** ⭐ BARU — simpan display currency user. */
    suspend fun updateCurrency(uid: String, code: String)
}