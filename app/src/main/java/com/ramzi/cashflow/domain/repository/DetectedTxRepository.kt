package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.DetectedTx
import kotlinx.coroutines.flow.Flow

interface DetectedTxRepository {

    /** Observe semua detected_txs PENDING milik user, sortir terbaru dulu. */
    fun observePending(userId: String): Flow<List<DetectedTx>>

    /** One-shot get semua pending. */
    suspend fun getPending(userId: String): List<DetectedTx>

    /** One-shot get semua (termasuk yang sudah di-action), untuk audit. */
    suspend fun getAll(userId: String): List<DetectedTx>

    /**
     * Simpan detected tx baru (dari notif).
     * Return `true` kalau benar-benar disimpan (bukan duplikat).
     */
    suspend fun addIfNotDuplicate(tx: DetectedTx): Boolean

    /** Update status (SAVED / DISMISSED / AUTO_SAVED). */
    suspend fun updateStatus(id: String, status: String)

    /** Hapus 1 detected tx. */
    suspend fun delete(id: String)

    /** Hapus semua yang sudah tidak PENDING (cleanup). */
    suspend fun cleanupDone(userId: String)

    /**
     * Ambil semua PENDING yang tanggalnya < today.
     * Ini yang akan di-auto-save jam 00:00.
     */
    suspend fun getPendingBefore(userId: String, today: String): List<DetectedTx>
}