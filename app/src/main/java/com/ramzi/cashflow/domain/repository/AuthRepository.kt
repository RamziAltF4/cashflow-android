package com.ramzi.cashflow.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.ramzi.cashflow.domain.model.User

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser
    suspend fun signInWithPin(identifier: String, pin: String): FirebaseUser
    suspend fun signInWithBiometric(identifier: String): FirebaseUser
    suspend fun register(name: String, email: String, password: String): FirebaseUser
    suspend fun getUserProfile(uid: String): User?
    fun isFirebaseReady(): Boolean
    fun getCurrentUser(): FirebaseUser?
    suspend fun signOut()
}