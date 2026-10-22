package com.ramzi.cashflow.domain.repository

import com.ramzi.cashflow.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Padanan CRUD transaksi di free.js/pro.js/business.js:
 *   - loadTx() → query Firestore transactions where userId == current
 *   - addDoc / updateDoc / deleteDoc
 *
 * Di Android pakai snapshotListener (Flow) supaya realtime.
 */
interface TxRepository {

    /**
     * Realtime stream transaksi user.
     * Padanan `loadTx()` yang dipanggil di initCore().
     */
    fun observeByUser(userId: String): Flow<List<Transaction>>

    /** Padanan addDoc(collection('transactions'), {...}) di handleSaveTx(). */
    suspend fun add(tx: Transaction): String

    /** Padanan updateDoc(doc('transactions', id), {...}). */
    suspend fun update(txId: String, tx: Transaction)

    /** Padanan deleteDoc(doc('transactions', id)). */
    suspend fun delete(txId: String)

    /** Padanan loop deleteDoc di `#clearDataBtn`. */
    suspend fun deleteAllForUser(userId: String)

    /** Ambil 1 transaksi (untuk edit). */
    suspend fun getById(txId: String): Transaction?
}