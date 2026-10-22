package com.ramzi.cashflow.data.repository


import com.ramzi.cashflow.data.remote.AuthApi
import com.ramzi.cashflow.data.remote.dto.ApiErrorResponse
import com.ramzi.cashflow.data.remote.dto.PinSetRequest
import com.ramzi.cashflow.domain.repository.PinRepository
import com.ramzi.cashflow.domain.repository.PinSetException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class PinRepositoryImpl(
    private val authApi: AuthApi
) : PinRepository {

    override suspend fun setPin(idToken: String, pin: String) {
        try {
            val resp = authApi.pinSet(
                bearer = "Bearer $idToken",
                body = PinSetRequest(pin = pin)
            )
            if (!resp.success) {
                throw PinSetException(
                    resp.message ?: "Gagal menyimpan PIN."
                )
            }
        } catch (e: HttpException) {
            val serverMsg = try {
                val body = e.response()?.errorBody()?.string().orEmpty()
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<ApiErrorResponse>(body)
                    .error
            } catch (_: Throwable) {
                null
            }
            throw PinSetException(
                serverMsg ?: "Gagal menyimpan PIN (${e.code()})."
            )
        }
    }
}
