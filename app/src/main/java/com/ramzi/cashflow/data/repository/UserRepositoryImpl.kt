package com.ramzi.cashflow.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.AccountStatus
import com.ramzi.cashflow.domain.model.Plan
import com.ramzi.cashflow.domain.model.User
import com.ramzi.cashflow.domain.model.UserPreferences
import com.ramzi.cashflow.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val col = firestore.collection("users")

    override fun observeById(uid: String): Flow<User?> = callbackFlow {
        val reg = col.document(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w("UserRepository", "Snapshot error: ${err.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(mapDoc(snap.data ?: emptyMap(), id = snap.id))
        }
        awaitClose { reg.remove() }
    }

    override suspend fun getById(uid: String): User? {
        val doc = col.document(uid).get().await()
        if (!doc.exists()) return null
        return mapDoc(doc.data ?: emptyMap(), id = doc.id)
    }

    override suspend fun updateName(uid: String, name: String) {
        col.document(uid).update("name", name).await()
    }

    override suspend fun updatePreferences(uid: String, prefs: UserPreferences) {
        col.document(uid).update(
            mapOf(
                "preferences" to mapOf(
                    "emailWeekly" to prefs.emailWeekly,
                    "budgetAlert" to prefs.budgetAlert,
                    "promo" to prefs.promo
                ),
                "preferencesUpdatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    override suspend fun updatePreference(uid: String, key: String, value: Boolean) {
        col.document(uid).update(
            mapOf(
                "preferences.$key" to value,
                "preferencesUpdatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    override suspend fun updateBudget(uid: String, amount: Long) {
        col.document(uid).update(
            mapOf(
                "budget" to amount,
                "budgetUpdatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    override suspend fun enable2FA(uid: String, secret: String) {
        col.document(uid).update(
            mapOf(
                "twoFAEnabled" to true,
                "twoFASecret" to secret,
                "twoFAEnabledAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    override suspend fun disable2FA(uid: String) {
        col.document(uid).update(
            mapOf(
                "twoFAEnabled" to false,
                "twoFASecret" to null
            )
        ).await()
    }

    override suspend fun updateApiKey(uid: String, apiKey: String) {
        col.document(uid).update(
            mapOf(
                "apiKey" to apiKey,
                "apiKeyCreatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun enablePinFlag(uid: String) {
        col.document(uid).update("pinEnabled", true).await()
    }

    // ⭐ BARU
    override suspend fun updateCurrency(uid: String, code: String) {
        col.document(uid).update("currencyCode", code.uppercase()).await()
    }

    private fun mapDoc(map: Map<String, Any?>, id: String = ""): User {
        @Suppress("UNCHECKED_CAST")
        val prefsMap = map["preferences"] as? Map<String, Any?> ?: emptyMap()
        val prefs = UserPreferences(
            emailWeekly = prefsMap["emailWeekly"] as? Boolean ?: true,
            budgetAlert = prefsMap["budgetAlert"] as? Boolean ?: true,
            promo = prefsMap["promo"] as? Boolean ?: false
        )

        return User(
            id = (map["id"] as? String).orEmpty().ifBlank { id },
            name = (map["name"] as? String).orEmpty(),
            email = (map["email"] as? String).orEmpty(),
            plan = Plan.fromId(map["plan"] as? String),
            status = AccountStatus.fromId(map["status"] as? String),
            transactions = ((map["transactions"] as? Number)?.toInt()) ?: 0,
            joined = ((map["joined"] as? Number)?.toLong()) ?: 0L,
            createdAt = map["createdAt"] as? com.google.firebase.Timestamp,
            pinEnabled = map["pinEnabled"] as? Boolean ?: false,
            pinHash = map["pinHash"] as? String,
            pinSalt = map["pinSalt"] as? String,
            biometricEnabled = map["biometricEnabled"] as? Boolean ?: false,
            twoFAEnabled = map["twoFAEnabled"] as? Boolean ?: false,
            twoFASecret = map["twoFASecret"] as? String,
            twoFAEnabledAt = map["twoFAEnabledAt"] as? com.google.firebase.Timestamp,
            budget = ((map["budget"] as? Number)?.toLong()) ?: 0L,

            // ⭐ BARU
            currencyCode = (map["currencyCode"] as? String)?.uppercase() ?: "IDR",

            apiKey = map["apiKey"] as? String,
            apiKeyCreatedAt = ((map["apiKeyCreatedAt"] as? Number)?.toLong()) ?: 0L,
            preferences = prefs
        )
    }
}