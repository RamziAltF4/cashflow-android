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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.TaxResult
import com.ramzi.cashflow.util.Formatting

@Composable
fun TaxCalculatorCard(
    dppText: String,
    ppnEnabled: Boolean,
    pphEnabled: Boolean,
    onDppChange: (String) -> Unit,
    onPpnChange: (Boolean) -> Unit,
    onPphChange: (Boolean) -> Unit
) {
    val dpp = dppText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val result = TaxResult(dpp = dpp, ppnEnabled = ppnEnabled, pphEnabled = pphEnabled)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // ============ HEADER ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Kalkulator Pajak",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "PPN 10% & PPh 23 (2%)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ============ INPUT DPP ============
        OutlinedTextField(
            value = dppText,
            onValueChange = onDppChange,
            label = { Text("Nominal DPP (Rp)") },
            placeholder = { Text("1.000.000") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // ============ TOGGLES ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaxToggle(
                modifier = Modifier.weight(1f),
                label = "PPN 10%",
                checked = ppnEnabled,
                accent = Color(0xFF10B981),
                onCheckedChange = onPpnChange
            )
            TaxToggle(
                modifier = Modifier.weight(1f),
                label = "PPh 23 (2%)",
                checked = pphEnabled,
                accent = Color(0xFFE11D48),
                onCheckedChange = onPphChange
            )
        }

        Spacer(Modifier.height(14.dp))

        // ============ RESULT ============
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp)
        ) {
            TaxRow(
                label = "DPP",
                value = Formatting.formatRupiah(dpp),
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            if (ppnEnabled) {
                Spacer(Modifier.height(6.dp))
                TaxRow(
                    label = "+ PPN 10%",
                    value = Formatting.formatRupiah(result.ppnValue),
                    valueColor = Color(0xFF10B981)
                )
            }
            if (pphEnabled) {
                Spacer(Modifier.height(6.dp))
                TaxRow(
                    label = "− PPh 23 (2%)",
                    value = Formatting.formatRupiah(result.pphValue),
                    valueColor = Color(0xFFE11D48)
                )
            }

            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = Formatting.formatRupiah(result.total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
private fun TaxToggle(
    label: String,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (checked) accent.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
                width = 1.dp,
                color = if (checked) accent.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent
            ),
            modifier = Modifier.size(width = 34.dp, height = 20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaxRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}