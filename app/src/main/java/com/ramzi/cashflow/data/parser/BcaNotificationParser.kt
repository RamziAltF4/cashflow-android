package com.ramzi.cashflow.data.parser

import com.ramzi.cashflow.domain.model.DetectedTx
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.domain.repository.MerchantAliasRepository
import com.ramzi.cashflow.util.CategoryMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BcaNotificationParser {

    const val PACKAGE_NAME = "com.bca.mybca.omni.android"
    const val NOTIF_TITLE = "Catatan Finansial"

    private val regexExpense = Regex(
        pattern = """Pengeluaran\s+sebesar\s+IDR\s+([\d.,]+)\s+di\s+kategori\s+(.+?)\.?\s*$""",
        option = RegexOption.IGNORE_CASE
    )

    private val regexIncome = Regex(
        pattern = """Pemasukan\s+sebesar\s+IDR\s+([\d.,]+)\s+dari\s+(.+?)\s+di\s+kategori\s+(.+?)\.?\s*$""",
        option = RegexOption.IGNORE_CASE
    )

    fun isBcaFinanceNotification(packageName: String?, title: String?): Boolean {
        if (packageName == null || title == null) return false
        if (packageName != PACKAGE_NAME) return false
        return title.trim().equals(NOTIF_TITLE, ignoreCase = true)
    }

    fun parse(
        body: String,
        userId: String,
        notifTimestamp: Long = System.currentTimeMillis()
    ): DetectedTx? {
        if (body.isBlank() || userId.isBlank()) return null

        regexExpense.find(body)?.let { match ->
            return buildFromMatch(
                match = match,
                type = TxType.EXPENSE,
                userId = userId,
                notifTimestamp = notifTimestamp,
                rawText = body,
                merchant = ""
            )
        }

        regexIncome.find(body)?.let { match ->
            return buildFromMatch(
                match = match,
                type = TxType.INCOME,
                userId = userId,
                notifTimestamp = notifTimestamp,
                rawText = body,
                merchant = match.groupValues[2].trim()
            )
        }

        return null
    }

    suspend fun applyAlias(
        userId: String,
        merchant: String,
        aliasRepo: MerchantAliasRepository
    ): String {
        if (merchant.isBlank()) return ""
        return try {
            val alias = aliasRepo.find(userId, merchant)
            alias?.displayName ?: merchant
        } catch (e: Exception) {
            merchant
        }
    }

    private fun buildFromMatch(
        match: MatchResult,
        type: TxType,
        userId: String,
        notifTimestamp: Long,
        rawText: String,
        merchant: String
    ): DetectedTx? {
        val amount = parseAmount(match.groupValues[1]) ?: return null
        if (amount <= 0) return null

        val categoryFromBank = when (type) {
            TxType.EXPENSE -> match.groupValues.getOrNull(2)?.trim().orEmpty()
            TxType.INCOME -> match.groupValues.getOrNull(3)?.trim().orEmpty()
        }

        val categoryMapped = CategoryMapper.resolve(
            type = type,
            bankCategory = categoryFromBank,
            merchant = merchant
        )

        val dateIso = isoDate(notifTimestamp)
        val cleanMerchant = cleanMerchantName(merchant)

        return DetectedTx(
            userId = userId,
            bank = "BCA",
            type = type,
            amount = amount,
            merchant = cleanMerchant,
            categoryFromBank = categoryFromBank,
            categoryMapped = categoryMapped,
            rawText = rawText,
            notifTimestamp = notifTimestamp,
            date = dateIso,
            fingerprint = buildFingerprint(
                amount = amount,
                type = type,
                merchant = cleanMerchant,
                date = dateIso
            )
        )
    }

    private fun parseAmount(raw: String): Long? {
        if (raw.isBlank()) return null
        val cleaned = when {
            raw.matches(Regex("""[\d,]+\.\d+""")) -> raw.replace(",", "").substringBefore(".")
            raw.matches(Regex("""[\d.]+,\d+""")) -> raw.replace(".", "").substringBefore(",")
            else -> raw.replace(",", "").replace(".", "")
        }
        return cleaned.toLongOrNull()
    }

    private fun cleanMerchantName(raw: String): String {
        if (raw.isBlank()) return ""

        var s = raw
        s = s.replace(Regex("""[*#_]+"""), " ")
        s = s.replace(Regex("""\s*\d{3,}\s*$"""), "")
        s = s.replace(Regex("""\s+"""), " ").trim()

        s = s.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                if (word.length <= 1) word.uppercase()
                else word.lowercase().replaceFirstChar { it.uppercase() }
            }

        return s
    }

    private fun buildFingerprint(
        amount: Long,
        type: TxType,
        merchant: String,
        date: String
    ): String {
        return "${type.id}|$amount|${merchant.lowercase()}|$date"
    }

    private fun isoDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(millis))
    }
}