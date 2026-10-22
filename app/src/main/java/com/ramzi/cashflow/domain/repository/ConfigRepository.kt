package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.BankConfig

interface ConfigRepository {
    suspend fun getBankConfig(): BankConfig
}