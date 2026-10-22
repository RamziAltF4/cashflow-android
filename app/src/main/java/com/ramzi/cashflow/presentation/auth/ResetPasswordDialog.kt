package com.ramzi.cashflow.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.ramzi.cashflow.presentation.theme.LocalAccent

/**
 * Dialog untuk reset password via email.
 * Padanan flow "Lupa password?" di login.html.
 *
 * Menggunakan Firebase sendPasswordResetEmail.
 * Firebase akan kirim email dengan link reset password ke email tersebut.
 */
@Composable
fun ResetPasswordDialog(
    isOpen: Boolean,
    email: String,
    sending: Boolean,
    success: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    if (!isOpen) return

    val accent = LocalAccent.current

    AlertDialog(
        onDismissRequest = { if (!sending) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            if (success) {
                SuccessHeader(accent)
            } else {
                RequestHeader(accent)
            }
        },
        text = {
            if (success) {
                SuccessBody(email)
            } else {
                RequestBody(
                    email = email,
                    error = error,
                    onEmailChange = onEmailChange
                )
            }
        },
        confirmButton = {
            if (success) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.base,
                        contentColor = Color.White
                    )
                ) {
                    Text("Mengerti", fontWeight = FontWeight.Black)
                }
            } else {
                Button(
                    onClick = onSend,
                    enabled = !sending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.base,
                        contentColor = Color.White
                    )
                ) {
                    if (sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Kirim Link", fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            if (!success) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !sending
                ) {
                    Text(
                        "Batal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

// ============================================================
// Header (sebelum kirim)
// ============================================================
@Composable
private fun RequestHeader(accent: com.ramzi.cashflow.domain.model.AccentColor) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.soft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🔒",
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Lupa Password?",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kami akan kirim link reset",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// Body (sebelum kirim)
// ============================================================
@Composable
private fun RequestBody(
    email: String,
    error: String?,
    onEmailChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Masukkan email yang terdaftar. Kami akan mengirim tautan untuk mengatur ulang password Anda.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "nama@email.com",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )

        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(10.dp)
            ) {
                Text(
                    text = error,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ============================================================
// Header (setelah sukses kirim)
// ============================================================
@Composable
private fun SuccessHeader(accent: com.ramzi.cashflow.domain.model.AccentColor) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFECFDF5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "✓",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF10B981)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Email Terkirim!",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Cek inbox Anda",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// Body (setelah sukses kirim)
// ============================================================
@Composable
private fun SuccessBody(email: String) {
    Column {
        Text(
            text = "Kami telah mengirim tautan reset password ke:",
            fontSize = 12.sp,
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
                text = email,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Buka link di email tersebut untuk membuat password baru. Kalau tidak muncul, cek folder spam atau promosi.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}