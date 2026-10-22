package com.ramzi.cashflow

import com.ramzi.cashflow.domain.model.ForecastResult
import com.ramzi.cashflow.domain.model.HealthScore
import com.ramzi.cashflow.domain.model.InvoiceStatus
import com.ramzi.cashflow.domain.model.TaxResult
import com.ramzi.cashflow.domain.model.Transaction
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.util.Formatting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessLogicUnitTest {

    @Test
    fun testInvoiceStatusResolution() {
        assertEquals(InvoiceStatus.PAID, InvoiceStatus.fromId("paid"))
        assertEquals(InvoiceStatus.PENDING, InvoiceStatus.fromId("pending"))
        assertEquals(InvoiceStatus.CANCELLED, InvoiceStatus.fromId("cancelled"))
        assertEquals(InvoiceStatus.PENDING, InvoiceStatus.fromId("unknown_status"))
    }

    @Test
    fun testTaxCalculation() {
        val tax = TaxResult(
            dpp = 10_000_000L,
            ppnEnabled = true,
            pphEnabled = true
        )
        // PPN 10% dari 10.000.000 = 1.000.000
        assertEquals(1_000_000L, tax.ppnValue)
        // PPh 2% dari 10.000.000 = 200.000
        assertEquals(200_000L, tax.pphValue)
        // Total = 10.000.000 + 1.000.000 - 200.000 = 10.800.000
        assertEquals(10_800_000L, tax.total)
    }

    @Test
    fun testHealthScoreCalculation() {
        val scoreGood = HealthScore.calculate(
            totalIncome = 15_000_000L,
            totalExpense = 5_000_000L,
            incomeDays = 10,
            txCount = 20
        )
        assertTrue(scoreGood.score in 0..100)
        assertEquals("A", scoreGood.grade)
        assertTrue(scoreGood.savingRate > 0.5f)
        assertTrue(scoreGood.isHealthy)
    }

    @Test
    fun testFormattingRupiah() {
        assertEquals("Rp 0", Formatting.formatRupiah(0L))
        assertEquals("Rp 50.000", Formatting.formatRupiah(50_000L))
        assertEquals("Rp 1.500.000", Formatting.formatRupiah(1_500_000L))
    }

    @Test
    fun testForecastCalculation() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val txs = listOf(
            Transaction(id = "1", type = TxType.INCOME, amount = 1_000_000L, date = today),
            Transaction(id = "2", type = TxType.EXPENSE, amount = 400_000L, date = today)
        )
        val forecast = ForecastResult.calculate(txs, lookbackDays = 30)
        assertEquals(600_000L, forecast.currentBalance)
        assertTrue(forecast.isPositive)
    }
}
