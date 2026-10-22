package com.ramzi.cashflow.domain.model

/**
 * Skor kesehatan keuangan (0-100) + grade.
 * Padanan logika dari business-extras.js `renderHealthScore`.
 */
data class HealthScore(
    val score: Int = 0,
    val grade: String = "D",
    val label: String = "Perlu Perhatian",
    val savingRate: Float = 0f,
    val incomeDays: Int = 0
) {
    val isHealthy: Boolean get() = score >= 60

    companion object {
        fun calculate(
            totalIncome: Long,
            totalExpense: Long,
            incomeDays: Int,
            txCount: Int
        ): HealthScore {
            val balance = totalIncome - totalExpense
            val savingRate = if (totalIncome > 0) balance.toFloat() / totalIncome.toFloat() else 0f

            var score = 0
            // Saving rate (max 40)
            score += ((savingRate * 100f).toInt()).coerceIn(0, 40)
            // Income days (max 30)
            score += (incomeDays * 3).coerceAtMost(30)
            // Tx count (max 30)
            score += txCount.coerceAtMost(30)
            score = score.coerceIn(0, 100)

            return when {
                score >= 80 -> HealthScore(score, "A", "Sangat Sehat", savingRate, incomeDays)
                score >= 60 -> HealthScore(score, "B", "Sehat", savingRate, incomeDays)
                score >= 40 -> HealthScore(score, "C", "Cukup", savingRate, incomeDays)
                else -> HealthScore(score, "D", "Perlu Perhatian", savingRate, incomeDays)
            }
        }
    }
}