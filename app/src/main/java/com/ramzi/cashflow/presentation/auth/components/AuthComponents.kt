package com.ramzi.cashflow.presentation.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.presentation.theme.LocalAccent

// ============================================================
// TAB SWITCHER — Pill style
// ============================================================
@Composable
fun AuthTabRow(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalAccent.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton(
            label = "Masuk",
            active = selected == 0,
            activeColor = accent.base,
            onClick = { onSelect(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            label = "Daftar",
            active = selected == 1,
            activeColor = accent.base,
            onClick = { onSelect(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabText"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = textColor,
            fontSize = 13.sp
        )
    }
}

// ============================================================
// TEXT FIELD — Modern outline style
// ============================================================
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisible: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    val accent = LocalAccent.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        trailingIcon = {
            if (isPassword && onToggleVisible != null) {
                TextButton(onClick = onToggleVisible) {
                    Text(
                        text = if (passwordVisible) "Sembunyikan" else "Tampilkan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent.base
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = accent.base,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            cursorColor = accent.base,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ============================================================
// PRIMARY BUTTON — Gradient accent
// ============================================================
@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true
) {
    val accent = LocalAccent.current
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent.base,
            contentColor = Color.White
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.5.dp,
                color = Color.White
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 0.3.sp
        )
    }
}

// ============================================================
// ERROR BOX — Merah soft
// ============================================================
@Composable
fun AuthErrorBox(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================
// PASSWORD STRENGTH BAR
// ============================================================
@Composable
fun PasswordStrengthBar(strength: Float, modifier: Modifier = Modifier) {
    val color = when {
        strength < 0.4f -> Color(0xFFF43F5E)
        strength < 0.8f -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
    val animated by animateFloatAsState(targetValue = strength, label = "strength")

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(color)
            )
        }
    }
}