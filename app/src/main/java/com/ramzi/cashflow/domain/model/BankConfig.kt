package com.ramzi.cashflow.domain.model

data class BankConfig(
    val bankName: String = "",
    val accountNumber: String = "",
    val accountName: String = "",
    val note: String = ""
) {
    val isConfigured: Boolean
        get() = bankName.isNotBlank() &&
                accountNumber.isNotBlank() &&
                accountName.isNotBlank()

    companion object {
        val EMPTY = BankConfig()
    }
}