package com.ramzi.cashflow.presentation.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.repository.ConfigRepositoryImpl
import com.ramzi.cashflow.data.repository.DetectedTxRepositoryImpl
import com.ramzi.cashflow.data.repository.TxRepositoryImpl
import com.ramzi.cashflow.data.repository.UserRepositoryImpl

object HomeViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            return HomeViewModel(
                userRepository = UserRepositoryImpl(firestore),
                txRepository = TxRepositoryImpl(firestore),
                detectedTxRepository = DetectedTxRepositoryImpl(firestore),
                configRepository = ConfigRepositoryImpl(firestore),
                firebaseAuth = FirebaseAuth.getInstance()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}