package com.ramzi.cashflow.presentation.dashboard.tx

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.data.repository.TxRepositoryImpl

class TxViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TxViewModel::class.java)) {
            return TxViewModel(
                txRepository = TxRepositoryImpl(FirebaseFirestore.getInstance()),
                firebaseAuth = FirebaseAuth.getInstance(),
                currencyPreference = CurrencyPreference(context.applicationContext)   // ⭐ BARU
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}