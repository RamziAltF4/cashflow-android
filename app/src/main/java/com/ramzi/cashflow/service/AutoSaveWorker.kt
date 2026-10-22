package com.ramzi.cashflow.service


import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.repository.DetectedTxRepositoryImpl
import com.ramzi.cashflow.domain.model.DetectedStatus
import com.ramzi.cashflow.domain.model.TxOrigin
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WorkManager yang auto-save detected_txs PENDING yang tanggalnya < hari ini.
 *
 * Dijadwalkan setiap hari jam 00:00 (kurang lebih).
 */
class AutoSaveWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrBlank()) {
            Log.d(TAG, "No user session, skip")
            return Result.success()
        }

        val firestore = FirebaseFirestore.getInstance()
        val detectedRepo = DetectedTxRepositoryImpl(firestore)
        val txCol = firestore.collection("transactions")

        return try {
            val today = todayIso()
            val pendingBefore = detectedRepo.getPendingBefore(userId, today)

            Log.d(TAG, "Auto-save: ${pendingBefore.size} item(s) to save")

            var saved = 0
            for (item in pendingBefore) {
                try {
                    val txRef = txCol.document()
                    txRef.set(
                        mapOf(
                            "userId" to item.userId,
                            "type" to item.type.id,
                            "desc" to item.merchant.ifBlank { "Auto import BCA" },
                            "amount" to item.amount,
                            "category" to item.categoryMapped,
                            "date" to item.date,
                            "note" to "Auto-save dari notifikasi BCA",
                            "origin" to TxOrigin.AUTO_IMPORT.id,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                    ).await()

                    detectedRepo.updateStatus(item.id, DetectedStatus.AUTO_SAVED.id)
                    saved++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to save item ${item.id}", e)
                }
            }

            Log.d(TAG, "Auto-save done: $saved saved")
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "AutoSaveWorker failed", e)
            Result.retry()
        }
    }

    private fun todayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    companion object {
        private const val TAG = "AutoSaveWorker"
        private const val WORK_NAME = "cashflow_auto_save_daily"

        /**
         * Jadwalkan periodic worker setiap hari jam 00:00.
         * Panggil di Application.onCreate() atau MainActivity.onCreate().
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Hitung delay sampai jam 00:00 berikutnya
            val now = Calendar.getInstance()
            val next = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val delayMs = next.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<AutoSaveWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        /**
         * Hentikan worker (kalau user nonaktifkan auto-import).
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
