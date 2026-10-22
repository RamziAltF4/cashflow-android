package com.ramzi.cashflow.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.local.SavedIdentifierPrefs
import com.ramzi.cashflow.data.remote.AuthApiModule
import com.ramzi.cashflow.data.repository.AuthRepositoryImpl

object AuthViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        throw UnsupportedOperationException(
            "Gunakan AuthViewModelFactory(context) — bukan object ini."
        )
    }

    /**
     * Factory helper untuk bikin AuthViewModel dengan Context.
     */
    fun create(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(
                        authRepository = AuthRepositoryImpl(
                            firebaseAuth = firebaseAuth,
                            firestore = FirebaseFirestore.getInstance(),
                            authApi = AuthApiModule.authApi
                        ),
                        firebaseAuth = firebaseAuth,
                        savedIdentifierPrefs = SavedIdentifierPrefs(context.applicationContext)
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
            }
        }
    }
}