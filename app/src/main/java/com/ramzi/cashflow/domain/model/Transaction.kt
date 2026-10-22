package com.ramzi.cashflow.domain.model

import com.google.firebase.Timestamp

/**
 * Padanan dokumen Firestore `transactions/{id}` di free.js:
 *   {
 *     userId, type, desc, amount, category, date, note,
 *     createdAt, updatedAt
 *   }
 *
 * `date` disimpan sebagai string ISO "YYYY-MM-DD" (sesuai free.js).
 */
data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: TxType = TxType.INCOME,
    val desc: String = "",
    val amount: Long = 0L,
    val category: String = "",
    val date: String = "",
    val note: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val workspaceId: String = ""
)

enum class TxType(val id: String) {
    INCOME("income"),
    EXPENSE("expense");

    companion object {
        fun fromId(id: String?): TxType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: INCOME
    }
}