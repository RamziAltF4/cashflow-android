package com.ramzi.cashflow.presentation.dashboard.plans.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.BankConfig
import com.ramzi.cashflow.domain.model.BillingMode
import com.ramzi.cashflow.domain.model.PlanConfig
import com.ramzi.cashflow.presentation.theme.LocalAccent
import com.ramzi.cashflow.util.Formatting
import java.io.ByteArrayOutputStream
import kotlin.math.min

/**
 * Dialog konfirmasi upgrade + upload bukti transfer.
 * Rekening tujuan diambil dari Firestore config/bank
 * (di-set admin via web admin panel).
 */
@Composable
fun UpgradeRequestDialog(
    isOpen: Boolean,
    plan: PlanConfig?,
    billingMode: BillingMode,
    bankConfig: BankConfig,                // ← BARU
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (proofBase64: String, note: String) -> Unit
) {
    if (!isOpen || plan == null) return

    val accent = LocalAccent.current
    val context = LocalContext.current

    var proofBase64 by remember { mutableStateOf<String?>(null) }
    var proofBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var note by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val original = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (original != null) {
                    val resized = resizeBitmap(original, maxDimension = 800)
                    proofBitmap = resized

                    val baos = ByteArrayOutputStream()
                    resized.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    val bytes = baos.toByteArray()
                    proofBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    localError = null
                }
            } catch (e: Throwable) {
                localError = "Gagal baca gambar: ${e.message}"
            }
        }
    }

    val price = plan.priceFor(billingMode)
    val billingLabel = if (billingMode == BillingMode.YEARLY) "Tahunan" else "Bulanan"

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Upgrade ke ${plan.label}",
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // ===== Ringkasan paket =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.soft)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Paket",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accent.text.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = plan.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accent.text
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accent.text.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = Formatting.formatRupiah(price),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accent.text
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Billing: $billingLabel",
                            fontSize = 11.sp,
                            color = accent.text.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ===== Rekening Tujuan (DINAMIS) =====
                Text(
                    text = "Rekening Tujuan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))

                if (bankConfig.isConfigured) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = bankConfig.bankName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = bankConfig.accountNumber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = accent.base,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "a.n. ${bankConfig.accountName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (bankConfig.note.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = bankConfig.note,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Admin belum mengatur rekening tujuan. Hubungi admin dulu sebelum transfer.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ===== Upload bukti =====
                Text(
                    text = "Bukti Transfer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))

                if (proofBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tap untuk pilih gambar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "JPG / PNG",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { pickImageLauncher.launch("image/*") }
                    ) {
                        Image(
                            bitmap = proofBitmap!!.asImageBitmap(),
                            contentDescription = "Bukti transfer",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Text(
                            text = "Tap untuk ganti",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ===== Catatan =====
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Catatan (opsional)", fontSize = 13.sp) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // ===== Error =====
                val displayError = localError ?: errorMessage
                if (displayError != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = displayError,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        !bankConfig.isConfigured ->
                            localError = "Admin belum mengatur rekening tujuan."
                        proofBase64 == null ->
                            localError = "Bukti transfer wajib dilampirkan."
                        else -> onSubmit(proofBase64!!, note)
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent.base,
                    contentColor = Color.White
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.size(6.dp))
                }
                Text("Kirim Request", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Batal", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val scale = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)

    return if (scale < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true
        )
    } else {
        bitmap
    }
}