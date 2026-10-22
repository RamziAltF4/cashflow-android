package com.ramzi.cashflow.domain.model

import com.google.firebase.Timestamp

/**
 * Padanan dokumen Firestore `recurring/{id}` (dari business-extras.js).
 *
 *   {
 *     userId, desc, amount, type, category, frequency,
 *     startDate, endDate, nextRun, lastRun,
 *     active, note, createdAt, updatedAt
 *   }
 *
 * - startDate / endDate / nextRun / lastRun = String ISO "YYYY-MM-DD"
 * - frequency = "daily" | "weekly" | "monthly" | "yearly"
 */
data class Recurring(
    val id: String = "",
    val userId: String = "",
    val desc: String = "",
    val amount: Long = 0L,
    val type: TxType = TxType.EXPENSE,
    val category: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val startDate: String = "",
    val endDate: String? = null,
    val nextRun: String = "",
    val lastRun: String? = null,
    val active: Boolean = true,
    val note: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val isDue: Boolean
        get() = active && nextRun.isNotBlank() && nextRun <= todayIso()

    val isEnded: Boolean
        get() = !endDate.isNullOrBlank() && nextRun > endDate

    companion object {
        private fun todayIso(): String {
            val d = java.util.Calendar.getInstance().time
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            return sdf.format(d)
        }
    }
}

enum class RecurringFrequency(val id: String, val label: String) {
    DAILY("daily", "Harian"),
    WEEKLY("weekly", "Mingguan"),
    MONTHLY("monthly", "Bulanan"),
    YEARLY("yearly", "Tahunan");

    companion object {
        fun fromId(id: String?): RecurringFrequency =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MONTHLY
    }
}