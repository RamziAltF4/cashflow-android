package com.ramzi.cashflow.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatting {

    private val localeID = Locale("id", "ID")

    private val rupiahFormat: NumberFormat = NumberFormat
        .getCurrencyInstance(localeID)
        .apply { maximumFractionDigits = 0 }

    private val dateIsoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateDisplayFormat = SimpleDateFormat("d MMM yyyy", localeID)

    fun formatRupiah(amount: Long): String {
        val formatted = rupiahFormat.format(amount)
            .replace("\u00A0", " ")
            .replace(",00", "")
            .trim()
        return if (formatted.startsWith("Rp") && !formatted.startsWith("Rp ")) {
            formatted.replaceFirst("Rp", "Rp ")
        } else {
            formatted
        }
    }

    fun formatDate(iso: String): String {
        if (iso.isBlank()) return ""
        return try {
            val d = dateIsoFormat.parse(iso) ?: return iso
            dateDisplayFormat.format(d)
        } catch (_: Throwable) {
            iso
        }
    }

    fun todayISO(): String = dateIsoFormat.format(Date())
}