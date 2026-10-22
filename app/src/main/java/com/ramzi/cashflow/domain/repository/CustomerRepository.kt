package com.ramzi.cashflow.domain.repository


import com.ramzi.cashflow.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {

    fun observeAll(userId: String): Flow<List<Customer>>

    suspend fun getAll(userId: String): List<Customer>

    suspend fun create(customer: Customer)

    suspend fun delete(id: String)
}
