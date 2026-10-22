package com.ramzi.cashflow.domain.model

enum class AuthError {
    INVALID_CREDENTIALS,
    TOO_MANY_REQUESTS,
    USER_DISABLED,
    EMAIL_ALREADY_IN_USE,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    ACCOUNT_NOT_FOUND,
    PIN_NOT_SET,
    PIN_TOO_MANY_ATTEMPTS,
    FIREBASE_NOT_READY,
    INVALID_SERVER_RESPONSE,
    CUSTOM_TOKEN_MISSING,
    NETWORK,
    UNKNOWN;

    companion object {
        fun fromFirebaseCode(code: String?): AuthError = when (code) {
            "auth/invalid-credential",
            "auth/user-not-found",
            "auth/wrong-password" -> INVALID_CREDENTIALS
            "auth/too-many-requests" -> TOO_MANY_REQUESTS
            "auth/user-disabled" -> USER_DISABLED
            "auth/email-already-in-use" -> EMAIL_ALREADY_IN_USE
            "auth/invalid-email" -> INVALID_EMAIL
            "auth/weak-password" -> WEAK_PASSWORD
            "auth/network-request-failed" -> NETWORK
            else -> UNKNOWN
        }

        fun fromServerMessage(msg: String?): AuthError {
            val m = msg?.lowercase().orEmpty()
            return when {
                "akun tidak ditemukan" in m -> ACCOUNT_NOT_FOUND
                "pin belum diatur" in m -> PIN_NOT_SET
                "pin salah" in m -> INVALID_CREDENTIALS
                "terlalu banyak percobaan" in m -> PIN_TOO_MANY_ATTEMPTS
                "missing or insufficient permissions" in m -> FIREBASE_NOT_READY
                "response yang tidak valid" in m -> INVALID_SERVER_RESPONSE
                "custom token tidak diterima" in m -> CUSTOM_TOKEN_MISSING
                "unable to resolve host" in m -> NETWORK
                "timeout" in m -> NETWORK
                else -> UNKNOWN
            }
        }
    }
}