package com.ramzi.cashflow.domain.model

/**
 * Hasil perhitungan kalkulator pajak (PPN/PPh).
 * Padanan logika dari business-extras.js `renderTaxCalculator`.
 */
data class TaxResult(
    val dpp: Long = 0L,
    val ppnEnabled: Boolean = true,
    val pphEnabled: Boolean = false
) {
    val ppnValue: Long
        get() = if (ppnEnabled) (dpp * 0.10).toLong() else 0L

    val pphValue: Long
        get() = if (pphEnabled) (dpp * 0.02).toLong() else 0L

    val total: Long
        get() = dpp + ppnValue - pphValue
}