package com.ramzi.cashflow.domain.model



import com.google.firebase.Timestamp

/**
 * Padanan dokumen Firestore `invoices/{id}` (business-extras.js).
 *
 *   { userId, invoiceNumber, clientName, amount, status, date, createdAt }
 */
data class Invoice(
    val id: String = "",
    val userId: String = "",
    val invoiceNumber: String = "",
    val clientName: String = "",
    val amount: Long = 0L,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val date: Long = 0L,
    val createdAt: Timestamp? = null
)

enum class InvoiceStatus(val id: String, val label: String) {
    PENDING("pending", "Pending"),
    PAID("paid", "Lunas"),
    CANCELLED("cancelled", "Batal");

    companion object {
        fun fromId(id: String?): InvoiceStatus =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PENDING
    }
}