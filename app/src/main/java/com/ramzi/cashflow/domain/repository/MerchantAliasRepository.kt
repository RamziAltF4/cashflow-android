package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.MerchantAlias
import kotlinx.coroutines.flow.Flow

interface MerchantAliasRepository {
    fun observeAll(userId: String): Flow<List<MerchantAlias>>
    suspend fun getAll(userId: String): List<MerchantAlias>
    suspend fun save(alias: MerchantAlias)
    suspend fun delete(id: String)
    suspend fun find(userId: String, merchantRaw: String): MerchantAlias?
}