package com.ramzi.cashflow.presentation.dashboard.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.repository.ConfigRepositoryImpl
import com.ramzi.cashflow.data.repository.PlanRepositoryImpl
import com.ramzi.cashflow.data.repository.UserRepositoryImpl

object PlansViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlansViewModel::class.java)) {
            return PlansViewModel(
                userRepository = UserRepositoryImpl(
                    FirebaseFirestore.getInstance()
                ),
                planRepository = PlanRepositoryImpl(
                    FirebaseFirestore.getInstance()
                ),
                configRepository = ConfigRepositoryImpl(
                    FirebaseFirestore.getInstance()
                ),
                firebaseAuth = FirebaseAuth.getInstance()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}