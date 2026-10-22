package com.ramzi.cashflow.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinSheet(
    isOpen: Boolean,
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    saving: Boolean = false,
    error: String? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = if (isEdit) "Ubah PIN" else "Buat PIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "PIN 6 digit untuk login cepat.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { v ->
                    pin = v.filter { it.isDigit() }.take(6)
                    localError = null
                },
                label = { Text("PIN (6 digit)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { v ->
                    confirm = v.filter { it.isDigit() }.take(6)
                    localError = null
                },
                label = { Text("Konfirmasi PIN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            val displayError = localError ?: error
            if (displayError != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = displayError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        when {
                            pin.length != 6 -> localError = "PIN harus 6 digit."
                            pin != confirm -> localError = "Konfirmasi PIN tidak cocok."
                            else -> onSave(pin)
                        }
                    },
                    enabled = !saving,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (saving) "Menyimpan..." else "Simpan PIN",
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", fontSize = 13.sp)
                }
            }
        }
    }
}