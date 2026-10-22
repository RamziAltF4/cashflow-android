package com.ramzi.cashflow.util

/**
 * Rate konversi dari IDR (base currency) ke currency lain.
 *
 * Cara pakai:
 *   val usd = CurrencyRates.convert(1_580_000L, "USD")  // = 100 (Rp 1.580.000 / 15.800)
 *
 * Update manual kalau kurs berubah. Base = IDR (rate = 1.0).
 */
object CurrencyRates {

    /** Base currency — semua transaksi disimpan dalam IDR. */
    const val BASE = "IDR"

    /**
     * 1 unit currency = X IDR.
     * Contoh: 1 USD = 15.800 IDR → map["USD"] = 15_800.0
     */
    private val idrPerUnit: Map<String, Double> = mapOf(
        "IDR" to 1.0,
        "USD" to 15_800.0,
        "SAR" to 4_210.0,
        "EUR" to 17_200.0,
        "GBP" to 20_050.0,
        "JPY" to 105.0,
        "MYR" to 3_560.0,
        "SGD" to 11_800.0,
        "AUD" to 10_400.0,
        "CNY" to 2_180.0,
        "INR" to 190.0,
        "KRW" to 11.5,
        "THB" to 445.0,
        "AED" to 4_300.0,
    )

    /** Konversi amount dari IDR ke currency target. */
    fun convert(amountIdr: Long, targetCurrency: String): Long {
        if (targetCurrency.equals(BASE, ignoreCase = true)) return amountIdr
        val idrPer1 = idrPerUnit[targetCurrency.uppercase()] ?: return amountIdr
        if (idrPer1 <= 0.0) return amountIdr
        return (amountIdr / idrPer1).toLong()
    }

    /** Rate untuk 1 unit currency target dalam IDR. */
    fun rateToIdr(currencyCode: String): Double =
        idrPerUnit[currencyCode.uppercase()] ?: 1.0

    fun isSupported(currencyCode: String): Boolean =
        idrPerUnit.containsKey(currencyCode.uppercase())
}