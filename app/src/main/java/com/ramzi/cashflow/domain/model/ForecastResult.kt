package com.ramzi.cashflow.domain.model

/**
 * Prediksi saldo 7 & 30 hari ke depan.
 * Padanan logika dari business-extras.js `renderForecast`.
 */
data class ForecastResult(
    val currentBalance: Long = 0L,
    val forecast7: Long = 0L,
    val forecast30: Long = 0L,
    val dailyNet: Long = 0L,
    val avgIncome: Long = 0L,
    val avgExpense: Long = 0L,
    val daysAnalyzed: Int = 0
) {
    val isPositive: Boolean get() = dailyNet >= 0
    val hasEnoughData: Boolean get() = daysAnalyzed > 0

    companion object {
        /**
         * Hitung forecast dari list transaksi.
         * @param txs semua transaksi user
         * @param lookbackDays jumlah hari ke belakang yang dianalisa (default 30)
         */
        fun calculate(
            txs: List<Transaction>,
            lookbackDays: Int = 30
        ): ForecastResult {
            // Bangun set tanggal lookback
            val cal = java.util.Calendar.getInstance()
            val dateSet = mutableSetOf<String>()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            for (i in 0 until lookbackDays) {
                dateSet.add(sdf.format(cal.time))
                cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            }

            val recent = txs.filter { it.date in dateSet }
            val daysCount = recent.map { it.date }.distinct().size.coerceAtLeast(1)

            val totalIncome = recent.filter { it.type == TxType.INCOME }.sumOf { it.amount }
            val totalExpense = recent.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }

            val avgIncome = totalIncome / daysCount
            val avgExpense = totalExpense / daysCount
            val dailyNet = avgIncome - avgExpense

            val currentBalance = txs.sumOf {
                if (it.type == TxType.INCOME) it.amount else -it.amount
            }

            return ForecastResult(
                currentBalance = currentBalance,
                forecast7 = currentBalance + (dailyNet * 7),
                forecast30 = currentBalance + (dailyNet * 30),
                dailyNet = dailyNet,
                avgIncome = avgIncome,
                avgExpense = avgExpense,
                daysAnalyzed = daysCount
            )
        }
    }
}