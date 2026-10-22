package com.ramzi.cashflow.presentation.dashboard.imports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.local.CurrencyPreference
import com.ramzi.cashflow.data.repository.DetectedTxRepositoryImpl
import com.ramzi.cashflow.data.repository.MerchantAliasRepositoryImpl

class ImportsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportsViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            return ImportsViewModel(
                detectedRepo = DetectedTxRepositoryImpl(firestore),
                aliasRepo = MerchantAliasRepositoryImpl(firestore),
                firebaseAuth = FirebaseAuth.getInstance(),
                firestore = firestore,
                currencyPreference = CurrencyPreference(context.applicationContext)   // ⭐ BARU
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}