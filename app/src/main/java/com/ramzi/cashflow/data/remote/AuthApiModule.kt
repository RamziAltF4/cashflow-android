package com.ramzi.cashflow.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object AuthApiModule {

    /**
     * BASE_URL — URL site Netlify Cashflow.
     * PASTIKAN nama site benar (cashflowzi).
     */
    private const val BASE_URL =
        "https://cashflowzi.netlify.app/.netlify/functions/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(AuthApi::class.java)
    }
}