package com.ramzi.cashflow.domain.repository

/**
 * Kontrak operasi PIN (set/ubah).
 * PIN login sendiri ditangani di AuthRepository.signInWithPin().
 */
interface PinRepository {

    /**
     * Set PIN baru ke backend (/pin-set).
     *
     * @param idToken Firebase ID token (Bearer). Diambil dari
     *                FirebaseAuth.currentUser.getIdToken(true).
     * @param pin 6 digit PIN.
     * @throws PinSetException jika backend return error.
     */
    suspend fun setPin(idToken: String, pin: String)
}

/**
 * Exception untuk error set PIN.
 */
class PinSetException(
    val message_: String
) : Exception(message_)