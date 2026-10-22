package com.ramzi.cashflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.domain.model.BankConfig
import com.ramzi.cashflow.domain.repository.ConfigRepository
import kotlinx.coroutines.tasks.await

class ConfigRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ConfigRepository {

    override suspend fun getBankConfig(): BankConfig {
        return try {
            val snap = firestore
                .collection("config")
                .document("bank")
                .get()
                .await()

            if (snap.exists()) {
                BankConfig(
                    bankName      = snap.getString("bankName")      ?: "",
                    accountNumber = snap.getString("accountNumber") ?: "",
                    accountName   = snap.getString("accountName")   ?: "",
                    note          = snap.getString("note")          ?: ""
                )
            } else {
                BankConfig.EMPTY
            }
        } catch (e: Exception) {
            BankConfig.EMPTY
        }
    }
}