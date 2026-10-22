package com.cashflow.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

/**
 * Padanan konseptual dari `window.fb` di firebase-config.js.
 *
 * Di web, Firebase di-init manual + di-expose via window.fb.
 * Di Android, init dilakukan otomatis oleh plugin google-services
 * setelah `google-services.json` disertakan di module app.
 *
 * Tidak ada `firebaseReady()` Promise di Android —
 * Firebase tersedia sinkron lewat FirebaseApp default.
 */
object FirebaseRefs {

    val auth: FirebaseAuth by lazy { Firebase.auth }

    val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Koleksi — sesuaikan dengan nama yang dipakai di login.js / file JS lain.
     * TIDAK BOLEH diasumsikan. Untuk sementara hanya konstanta top-level
     * yang benar-benar terlihat di HTML (tidak ada) — jadi saya tandai
     * sebagai TODO dan menunggu file JS.
     */
    object Collections {
        // TODO(dependency): nama koleksi aktual menunggu login.js / file JS lain.
        // Contoh yang SERING muncul di project sejenis — JANGAN dipakai sebelum diverifikasi:
        // const val USERS = "users"
    }

    /**
     * Helper padanan `orderBy`/`limit` — di Kotlin jadi fluent call:
     *   firestore.collection("x").orderBy("createdAt", Query.Direction.DESCENDING).limit(10)
     */
    val defaultOrderDesc: Query.Direction = Query.Direction.DESCENDING
}