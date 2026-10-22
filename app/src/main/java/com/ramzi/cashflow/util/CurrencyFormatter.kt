package com.ramzi.cashflow.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formatter multi-currency.
 *
 * Semua amount yang masuk ke sini adalah **IDR mentah**.
 * Fungsi ini akan:
 *   1. Konversi IDR → target currency (via [CurrencyRates])
 *   2. Format pakai locale currency tersebut
 *
 * Contoh:
 *   CurrencyFormatter.format(1_580_000L, "IDR") // "Rp 1.580.000"
 *   CurrencyFormatter.format(1_580_000L, "USD") // "$100"
 *   CurrencyFormatter.format(1_580_000L, "SAR") // "ر.س 375"
 */
object CurrencyFormatter {

    /** Daftar currency yang didukung (untuk settings picker). */
    val supportedCurrencies: List<CurrencyOption> = listOf(
        CurrencyOption("IDR", "Indonesian Rupiah", "Rp"),
        CurrencyOption("USD", "US Dollar", "$"),
        CurrencyOption("SAR", "Saudi Riyal", "ر.س"),
        CurrencyOption("EUR", "Euro", "€"),
        CurrencyOption("GBP", "British Pound", "£"),
        CurrencyOption("JPY", "Japanese Yen", "¥"),
        CurrencyOption("MYR", "Malaysian Ringgit", "RM"),
        CurrencyOption("SGD", "Singapore Dollar", "S$"),
        CurrencyOption("AUD", "Australian Dollar", "A$"),
        CurrencyOption("CNY", "Chinese Yuan", "¥"),
        CurrencyOption("INR", "Indian Rupee", "₹"),
        CurrencyOption("KRW", "Korean Won", "₩"),
        CurrencyOption("THB", "Thai Baht", "฿"),
        CurrencyOption("AED", "UAE Dirham", "د.إ"),
    )

    /**
     * Format amount (IDR mentah) → string currency target.
     *
     * @param amountIdr nilai uang dalam IDR (base).
     * @param targetCurrency kode ISO 4217, mis. "USD".
     * @param showDecimals paksa tampil/sembunyi desimal. null = ikut default currency.
     */
    fun format(
        amountIdr: Long,
        targetCurrency: String = CurrencyRates.BASE,
        showDecimals: Boolean? = null
    ): String {
        val converted = CurrencyRates.convert(amountIdr, targetCurrency)
        return formatRaw(converted, targetCurrency, showDecimals)
    }

    /**
     * Format amount yang **sudah** dalam currency target (tanpa konversi).
     * Dipakai internal, atau kalau kamu punya nilai yang sudah dikonversi.
     */
    fun formatRaw(
        amount: Long,
        currencyCode: String,
        showDecimals: Boolean? = null
    ): String {
        val currency = runCatching { Currency.getInstance(currencyCode.uppercase()) }
            .getOrDefault(Currency.getInstance("IDR"))

        val displayLocale = localeFor(currency.currencyCode)
        val nf = NumberFormat.getCurrencyInstance(displayLocale).apply {
            this.currency = currency
            val digits = currency.defaultFractionDigits
            when (showDecimals) {
                true -> {
                    minimumFractionDigits = digits
                    maximumFractionDigits = digits
                }
                false -> {
                    minimumFractionDigits = 0
                    maximumFractionDigits = 0
                }
                null -> {
                    minimumFractionDigits = digits
                    maximumFractionDigits = digits
                }
            }
        }

        val raw = nf.format(amount)
        return normalize(raw, currency)
    }

    /** Symbol saja untuk 1 currency, mis. "USD" → "$". */
    fun symbolOf(currencyCode: String): String =
        runCatching { Currency.getInstance(currencyCode.uppercase()).symbol }
            .getOrDefault(currencyCode)

    /** Label lengkap untuk picker, mis. "USD — US Dollar". */
    fun labelOf(currencyCode: String): String {
        val opt = supportedCurrencies.firstOrNull {
            it.code.equals(currencyCode, ignoreCase = true)
        }
        return opt?.let { "${it.code} — ${it.label}" } ?: currencyCode
    }

    // ---------- internal ----------

    private fun localeFor(currencyCode: String): Locale = when (currencyCode.uppercase()) {
        "IDR" -> Locale("id", "ID")
        "USD" -> Locale.US
        "SAR" -> Locale("ar", "SA")
        "EUR" -> Locale.GERMANY
        "GBP" -> Locale.UK
        "JPY" -> Locale.JAPAN
        "MYR" -> Locale("ms", "MY")
        "SGD" -> Locale("en", "SG")
        "AUD" -> Locale("en", "AU")
        "CNY" -> Locale.CHINA
        "INR" -> Locale("en", "IN")
        "KRW" -> Locale.KOREA
        "THB" -> Locale("th", "TH")
        "AED" -> Locale("ar", "AE")
        else -> Locale.US
    }

    private fun normalize(raw: String, currency: Currency): String {
        // Ganti non-breaking space → spasi biasa
        var s = raw.replace("\u00A0", " ").trim()

        // Pastikan ada spasi antara simbol dan angka
        val symbol = currency.symbol
        if (s.startsWith(symbol) && s.length > symbol.length && s[symbol.length] != ' ') {
            s = s.replaceFirst(symbol, "$symbol ")
        }
        return s
    }

    data class CurrencyOption(
        val code: String,
        val label: String,
        val symbol: String
    )
}