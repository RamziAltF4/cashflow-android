package com.ramzi.cashflow.data.remote

import com.ramzi.cashflow.data.remote.dto.PinLoginRequest
import com.ramzi.cashflow.data.remote.dto.PinLoginResponse
import com.ramzi.cashflow.data.remote.dto.PinSetRequest
import com.ramzi.cashflow.data.remote.dto.PinSetResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    /**
     * Login via PIN — padanan fetch('/.netlify/functions/pin-login').
     * Return customToken untuk signInWithCustomToken.
     */
    @POST("pin-login")
    suspend fun pinLogin(
        @Body body: PinLoginRequest
    ): PinLoginResponse

    /**
     * Set / ubah PIN — padanan fetch('/.netlify/functions/pin-set')
     * dengan Authorization: Bearer <firebaseIdToken>.
     *
     * PIN di-hash scrypt di backend, disimpan di auth_secrets/{uid}.
     */
    @POST("set-pin")
    suspend fun pinSet(
        @Header("Authorization") bearer: String,
        @Body body: PinSetRequest
    ): PinSetResponse
}