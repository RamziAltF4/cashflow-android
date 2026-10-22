package com.ramzi.cashflow.util

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

/**
 * Helper TOTP (RFC 6238) — kompatibel dengan Google Authenticator.
 * SHA1, 6 digit, 30 detik.
 */
object TotpHelper {

    private const val DIGITS = 6
    private const val PERIOD_SECONDS = 30L

    /** Karakter base32 (sesuai RFC 4648, tanpa padding). */
    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /**
     * Generate secret base32 acak, panjang 32 char (160 bit).
     * Kompatibel dengan Google Authenticator.
     */
    fun generateSecret(length: Int = 32): String {
        val rnd = SecureRandom()
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(BASE32_ALPHABET[rnd.nextInt(BASE32_ALPHABET.length)])
        }
        return sb.toString()
    }

    /**
     * Decode base32 string → ByteArray.
     * Implementasi manual RFC 4648 (tanpa padding requirement).
     */
    private fun base32Decode(input: String): ByteArray {
        val cleaned = input.trim()
            .replace(" ", "")
            .replace("=", "")
            .uppercase()

        val output = java.io.ByteArrayOutputStream()
        var buffer = 0
        var bitsLeft = 0

        for (c in cleaned) {
            val idx = BASE32_ALPHABET.indexOf(c)
            if (idx < 0) continue // skip karakter invalid
            buffer = (buffer shl 5) or idx
            bitsLeft += 5

            if (bitsLeft >= 8) {
                output.write((buffer shr (bitsLeft - 8)) and 0xFF)
                bitsLeft -= 8
            }
        }

        return output.toByteArray()
    }

    /**
     * Generate kode 6 digit dari secret + waktu sekarang.
     */
    fun generateCurrentCode(secret: String): String {
        val config = TimeBasedOneTimePasswordConfig(
            codeDigits = DIGITS,
            hmacAlgorithm = HmacAlgorithm.SHA1,
            timeStep = PERIOD_SECONDS,
            timeStepUnit = TimeUnit.SECONDS
        )
        val generator = TimeBasedOneTimePasswordGenerator(
            secret = base32Decode(secret),
            config = config
        )
        return generator.generate()
    }

    /**
     * Verifikasi kode 6 digit. Toleransi ±1 time step (30 detik sebelum/sesudah).
     */
    fun verifyCode(secret: String, code: String): Boolean {
        if (code.length != DIGITS || !code.all { it.isDigit() }) return false

        val config = TimeBasedOneTimePasswordConfig(
            codeDigits = DIGITS,
            hmacAlgorithm = HmacAlgorithm.SHA1,
            timeStep = PERIOD_SECONDS,
            timeStepUnit = TimeUnit.SECONDS
        )
        val generator = TimeBasedOneTimePasswordGenerator(
            secret = base32Decode(secret),
            config = config
        )

        return try {
            generator.isValid(code, 1)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Bikin otpauth URL untuk QR Code.
     */
    fun buildOtpAuthUrl(
        secret: String,
        label: String,
        issuer: String = "Cashflow"
    ): String {
        val encodedLabel = java.net.URLEncoder.encode("$issuer:$label", "UTF-8")
        val encodedIssuer = java.net.URLEncoder.encode(issuer, "UTF-8")
        return "otpauth://totp/$encodedLabel" +
            "?secret=$secret" +
            "&issuer=$encodedIssuer" +
            "&algorithm=SHA1" +
            "&digits=$DIGITS" +
            "&period=$PERIOD_SECONDS"
    }
}