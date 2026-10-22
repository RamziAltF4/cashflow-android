package com.ramzi.cashflow.presentation.dashboard.settings.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.util.QrCodeGenerator
import com.ramzi.cashflow.util.TotpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFASetupSheet(
    isOpen: Boolean,
    userEmail: String,
    onDismiss: () -> Unit,
    onVerified: (secret: String) -> Unit,
    saving: Boolean = false,
    error: String? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Generate secret sekali per session
    val secret = remember { TotpHelper.generateSecret() }
    val otpAuthUrl = remember(secret, userEmail) {
        TotpHelper.buildOtpAuthUrl(
            secret = secret,
            label = userEmail.ifBlank { "user@cashflow" }
        )
    }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var code by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    // Generate QR bitmap di background
    LaunchedEffect(otpAuthUrl) {
        qrBitmap = withContext(Dispatchers.Default) {
            QrCodeGenerator.generate(otpAuthUrl, sizePx = 512)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Verifikasi 2 Langkah",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Scan QR dengan Google Authenticator",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // QR Code
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val bmp = qrBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "QR Code 2FA",
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            Spacer(Modifier.height(16.dp))

            // Manual secret
            Text(
                text = "Atau masukkan manual:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Text(
                    text = secret.chunked(4).joinToString(" "),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            // Input kode 6 digit
            Text(
                text = "Masukkan kode 6 digit dari aplikasi",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { v ->
                    code = v.filter { it.isDigit() }.take(6)
                    localError = null
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            val displayError = localError ?: error
            if (displayError != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = displayError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        when {
                            code.length != 6 -> {
                                localError = "Kode harus 6 digit."
                            }
                            !TotpHelper.verifyCode(secret, code) -> {
                                localError = "Kode salah atau sudah kedaluwarsa. Coba lagi."
                            }
                            else -> {
                                onVerified(secret)
                            }
                        }
                    },
                    enabled = !saving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(2f)
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.size(6.dp))
                    }
                    Text(
                        "Verifikasi & Aktifkan",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}