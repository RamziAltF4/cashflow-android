package com.ramzi.cashflow.util

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

object BiometricAuthHelper {

    private const val TAG = "BIOMETRIC"

    private const val AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.BIOMETRIC_WEAK

    /** Guard global: cegah 2 prompt jalan bareng */
    private val isPrompting = AtomicBoolean(false)

    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val result = manager.canAuthenticate(AUTHENTICATORS)
        val message = when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> "SUCCESS"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "NO_HARDWARE"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "HW_UNAVAILABLE"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "NONE_ENROLLED"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "SECURITY_UPDATE_REQUIRED"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "UNSUPPORTED"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "STATUS_UNKNOWN"
            else -> "OTHER"
        }
        Log.d(TAG, "canAuthenticate = $message (code=$result)")
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Verifikasi Biometrik",
        subtitle: String = "Sentuh sensor untuk melanjutkan"
    ): Boolean {
        // Kalau ada prompt lain jalan, langsung tolak
        if (!isPrompting.compareAndSet(false, true)) {
            Log.w(TAG, "authenticate() skip — prompt lain sedang aktif")
            return false
        }

        try {
            return suspendCancellableCoroutine { cont ->
                Log.d(TAG, "authenticate() called")
                Log.d(TAG, "activity = $activity")

                val executor = ContextCompat.getMainExecutor(activity)

                val prompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            Log.d(TAG, "onAuthenticationSucceeded")
                            super.onAuthenticationSucceeded(result)
                            isPrompting.set(false)
                            if (cont.isActive) cont.resume(true)
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            Log.d(TAG, "onAuthenticationError: code=$errorCode msg=$errString")
                            super.onAuthenticationError(errorCode, errString)
                            isPrompting.set(false)
                            if (cont.isActive) cont.resume(false)
                        }

                        override fun onAuthenticationFailed() {
                            // Retry — JANGAN resume, biarkan prompt tetap hidup
                            Log.d(TAG, "onAuthenticationFailed (retry)")
                            super.onAuthenticationFailed()
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText("Batal")
                    .setAllowedAuthenticators(AUTHENTICATORS)
                    .build()

                // Kalau coroutine di-cancel (user keluar / activity mati),
                // reset flag biar tidak "nyangkut" selamanya
                cont.invokeOnCancellation {
                    Log.w(TAG, "coroutine cancelled — reset isPrompting")
                    isPrompting.set(false)
                }

                try {
                    Log.d(TAG, "calling prompt.authenticate()")
                    prompt.authenticate(promptInfo)
                } catch (e: Throwable) {
                    Log.e(TAG, "prompt.authenticate() ERROR: ${e.message}", e)
                    isPrompting.set(false)
                    if (cont.isActive) cont.resume(false)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "authenticate() fatal: ${e.message}", e)
            isPrompting.set(false)
            return false
        }
    }
}