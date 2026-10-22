package com.cashflow.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

/**
 * Padanan dari wrappedGetDoc / wrappedSetDoc / dst. di firebase-config.js.
 *
 * Web: log error terpusat + rethrow.
 * Android: Result<T> + log terpusat + tidak crash ke UI.
 *
 * Catatan: `kotlinx-coroutines-play-services` diperlukan untuk .await().
 */
sealed interface FirebaseResult<out T> {
    data class Success<T>(val data: T) : FirebaseResult<T>
    data class Failure(val error: Throwable, val op: String, val path: String?) : FirebaseResult<Nothing>
}

private suspend fun <T> firebaseCall(
    op: String,
    path: String?,
    block: suspend () -> T
): FirebaseResult<T> = try {
    FirebaseResult.Success(block())
} catch (t: Throwable) {
    // Padanan console.error("FIREBASE REQUEST FAILED", {...}) di web
    android.util.Log.e("FirebaseRefs", "FIREBASE REQUEST FAILED op=$op path=$path", t)
    FirebaseResult.Failure(t, op, path)
}

/**
 * Padanan wrappedGetDoc.
 */
suspend fun getDocSafe(
    path: String,
    block: suspend () -> DocumentSnapshot
): FirebaseResult<DocumentSnapshot> =
    firebaseCall(op = "getDoc", path = path, block = block)

/**
 * Padanan wrappedGetDocs.
 */
suspend fun getDocsSafe(
    path: String?,
    block: suspend () -> QuerySnapshot
): FirebaseResult<QuerySnapshot> =
    firebaseCall(op = "getDocs", path = path, block = block)

/**
 * Padanan wrappedAddDoc.
 */
suspend fun <T> addDocSafe(
    path: String,
    block: suspend () -> T
): FirebaseResult<T> =
    firebaseCall(op = "addDoc", path = path, block = block)

/**
 * Padanan wrappedSetDoc / wrappedUpdateDoc / wrappedDeleteDoc.
 */
suspend fun <T> writeSafe(
    op: String,             // "setDoc" | "updateDoc" | "deleteDoc"
    path: String,
    block: suspend () -> T
): FirebaseResult<T> =
    firebaseCall(op = op, path = path, block = block)