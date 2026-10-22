package com.ramzi.cashflow.presentation.dashboard.business.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ramzi.cashflow.domain.model.CustomerType

@Composable
fun CustomerModal(
    name: String,
    phone: String,
    type: CustomerType,
    isSaving: Boolean,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onTypeChange: (CustomerType) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tambah Kontak",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(14.dp))

                // ============ TYPE TOGGLE ============
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    TypeChip(
                        modifier = Modifier.weight(1f),
                        label = "Customer",
                        active = type == CustomerType.CUSTOMER,
                        accent = Color(0xFF06B6D4),
                        onClick = { onTypeChange(CustomerType.CUSTOMER) }
                    )
                    TypeChip(
                        modifier = Modifier.weight(1f),
                        label = "Supplier",
                        active = type == CustomerType.SUPPLIER,
                        accent = Color(0xFF8B5CF6),
                        onClick = { onTypeChange(CustomerType.SUPPLIER) }
                    )
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nama") },
                    placeholder = { Text("Budi Santoso / PT Maju Jaya") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("No. HP (opsional)") },
                    placeholder = { Text("0812-3456-7890") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06B6D4)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Simpan", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(
    modifier: Modifier = Modifier,
    label: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (active) MaterialTheme.colorScheme.surface
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}