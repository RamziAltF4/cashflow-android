package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.Invoice
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {

    fun observeAll(userId: String): Flow<List<Invoice>>

    suspend fun getAll(userId: String): List<Invoice>

    suspend fun create(invoice: Invoice)

    suspend fun update(invoice: Invoice)

    suspend fun updateStatus(id: String, status: String)

    suspend fun batchUpdateStatus(ids: List<String>, status: String)

    suspend fun delete(id: String)
}