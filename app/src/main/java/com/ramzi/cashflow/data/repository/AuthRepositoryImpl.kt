package com.ramzi.cashflow.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.remote.AuthApi
import com.ramzi.cashflow.data.remote.dto.ApiErrorResponse
import com.ramzi.cashflow.data.remote.dto.PinLoginRequest
import com.ramzi.cashflow.domain.model.AccountStatus
import com.ramzi.cashflow.domain.model.Plan
import com.ramzi.cashflow.domain.model.AuthError
import com.ramzi.cashflow.domain.model.User
import com.ramzi.cashflow.domain.model.UserPreferences
import com.ramzi.cashflow.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authApi: AuthApi
) : AuthRepository {

    // ============================================================
    // EMAIL LOGIN
    // ============================================================
    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): FirebaseUser {
        val result = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()

        return result.user
            ?: throw IllegalStateException("Firebase user null setelah login email")
    }

    // ============================================================
    // PIN LOGIN
    // ============================================================
    override suspend fun signInWithPin(
        identifier: String,
        pin: String
    ): FirebaseUser {
        val response = try {
            authApi.pinLogin(PinLoginRequest(identifier = identifier, pin = pin))
        } catch (e: HttpException) {
            val serverMsg = try {
                val body = e.response()?.errorBody()?.string().orEmpty()
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<ApiErrorResponse>(body)
                    .error
            } catch (_: Throwable) {
                null
            }
            throw PinLoginException(
                error = AuthError.fromServerMessage(serverMsg),
                rawMessage = serverMsg ?: e.message()
            )
        }

        val result = firebaseAuth
            .signInWithCustomToken(response.customToken)
            .await()

        return result.user
            ?: throw IllegalStateException("Firebase user null setelah PIN login")
    }

    // ============================================================
    // BIOMETRIC LOGIN
    // ============================================================
    override suspend fun signInWithBiometric(identifier: String): FirebaseUser {
        // TODO: menunggu backend passkey-challenge.js & passkey-verify.js
        error("Biometric login belum diimplementasikan — menunggu backend passkey-*.js")
    }

    // ============================================================
    // REGISTER
    // ============================================================
    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): FirebaseUser {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()

        val user = result.user
            ?: throw IllegalStateException("Firebase user null setelah register")

        // Padanan fb.setDoc(fb.doc(fb.db, 'users', uid), {...}) di login.js
        val profile = mapOf(
            "id" to user.uid,
            "name" to name,
            "email" to email,
            "plan" to "free",
            "status" to "active",
            "transactions" to 0,
            "joined" to System.currentTimeMillis(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(user.uid)
            .set(profile)
            .await()

        return user
    }

    // ============================================================
    // GET USER PROFILE
    // ============================================================
    override suspend fun getUserProfile(uid: String): User? {
        val snap = firestore.collection("users").document(uid).get().await()
        if (!snap.exists()) return null

        // Parse preferences
        @Suppress("UNCHECKED_CAST")
        val prefsMap = snap.get("preferences") as? Map<String, Any?> ?: emptyMap()
        val prefs = UserPreferences(
            emailWeekly = prefsMap["emailWeekly"] as? Boolean ?: true,
            budgetAlert = prefsMap["budgetAlert"] as? Boolean ?: true,
            promo = prefsMap["promo"] as? Boolean ?: false
        )

        return User(
            id = snap.getString("id").orEmpty(),
            name = snap.getString("name").orEmpty(),
            email = snap.getString("email").orEmpty(),
            plan = Plan.fromId(snap.getString("plan")),
            status = AccountStatus.fromId(snap.getString("status")),
            transactions = (snap.getLong("transactions") ?: 0L).toInt(),
            joined = snap.getLong("joined") ?: 0L,
            createdAt = snap.getTimestamp("createdAt"),
            pinEnabled = snap.getBoolean("pinEnabled") ?: false,
            biometricEnabled = snap.getBoolean("biometricEnabled") ?: false,
            twoFAEnabled = snap.getBoolean("twoFAEnabled") ?: false,
            twoFASecret = snap.getString("twoFASecret"),
            twoFAEnabledAt = snap.getTimestamp("twoFAEnabledAt"),
            preferences = prefs
        )
    }

    // ============================================================
    // UTIL
    // ============================================================
    override fun isFirebaseReady(): Boolean =
        firebaseAuth.app != null

    override fun getCurrentUser(): FirebaseUser? =
        firebaseAuth.currentUser

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

/**
 * Exception yang membawa AuthError + pesan server mentah.
 * Dipakai oleh AuthViewModel untuk mapping ke UI.
 */
class PinLoginException(
    val error: AuthError,
    val rawMessage: String?
) : Exception(rawMessage ?: error.name)