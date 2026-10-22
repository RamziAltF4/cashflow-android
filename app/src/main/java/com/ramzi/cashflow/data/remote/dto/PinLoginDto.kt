package com.ramzi.cashflow.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PinLoginRequest(
    val identifier: String,
    val pin: String
)

@Serializable
data class PinLoginResponse(
    val success: Boolean,
    @SerialName("customToken") val customToken: String
)

@Serializable
data class PinSetRequest(
    val pin: String
)

@Serializable
data class PinSetResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class ApiErrorResponse(
    val error: String
)