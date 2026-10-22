package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.Recurring
import com.ramzi.cashflow.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface RecurringRepository {

    /** Observe semua transaksi berulang milik user (sort by nextRun). */
    fun observeAll(userId: String): Flow<List<Recurring>>

    /** One-shot get semua recurring. */
    suspend fun getAll(userId: String): List<Recurring>

    /** Buat recurring baru. */
    suspend fun create(recurring: Recurring)

    /** Update recurring. */
    suspend fun update(recurring: Recurring)

    /** Toggle aktif/nonaktif. */
    suspend fun toggleActive(id: String, active: Boolean)

    /** Hapus recurring. */
    suspend fun delete(id: String)

    /**
     * Proses recurring yang jatuh tempo hari ini:
     * - Buat transaksi baru di collection `transactions`
     * - Update `nextRun`, `lastRun`, `active`
     *
     * Return jumlah transaksi yang berhasil dibuat.
     */
    suspend fun processDue(
        userId: String,
        today: String
    ): ProcessResult

    data class ProcessResult(
        val createdCount: Int = 0,
        val createdDesc: List<String> = emptyList(),
        val createdTxs: List<Transaction> = emptyList()
    )
}