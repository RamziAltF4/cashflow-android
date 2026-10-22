package com.ramzi.cashflow.presentation.dashboard.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.repository.ActivityLogRepositoryImpl
import com.ramzi.cashflow.data.repository.ConfigRepositoryImpl
import com.ramzi.cashflow.data.repository.CustomerRepositoryImpl
import com.ramzi.cashflow.data.repository.InvoiceRepositoryImpl
import com.ramzi.cashflow.data.repository.RecurringRepositoryImpl
import com.ramzi.cashflow.data.repository.UserRepositoryImpl
import com.ramzi.cashflow.data.repository.WorkspaceRepositoryImpl

object BusinessViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()

            return BusinessViewModel(
                userRepository = UserRepositoryImpl(firestore),
                configRepository = ConfigRepositoryImpl(firestore),
                recurringRepository = RecurringRepositoryImpl(firestore),
                workspaceRepository = WorkspaceRepositoryImpl(firestore),
                customerRepository = CustomerRepositoryImpl(firestore),
                invoiceRepository = InvoiceRepositoryImpl(firestore),
                activityRepository = ActivityLogRepositoryImpl(firestore),
                firebaseAuth = FirebaseAuth.getInstance(),
                firestore = firestore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}