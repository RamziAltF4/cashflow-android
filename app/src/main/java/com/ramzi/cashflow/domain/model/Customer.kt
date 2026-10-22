package com.ramzi.cashflow.domain.model


import com.google.firebase.Timestamp

/**
 * Padanan dokumen Firestore `customers/{id}` (business-extras.js).
 *
 *   { userId, name, phone, type, createdAt }
 *   type = "customer" | "supplier"
 */
data class Customer(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val type: CustomerType = CustomerType.CUSTOMER,
    val createdAt: Timestamp? = null
)

enum class CustomerType(val id: String, val label: String) {
    CUSTOMER("customer", "Customer"),
    SUPPLIER("supplier", "Supplier");

    companion object {
        fun fromId(id: String?): CustomerType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CUSTOMER
    }
}
