package com.ramzi.cashflow.presentation.dashboard.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.presentation.theme.LocalAccent

@Composable
fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        modifier = modifier.padding(start = 32.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 2.dp),
        content = content
    )
}

@Composable
fun SettingsInsetDivider(startIndent: Int = 60) {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        modifier = Modifier.padding(start = startIndent.dp)
    )
}

@Composable
private fun SettingsIconContainer(icon: ImageVector, tint: Color, bgTint: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgTint),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    iconTint: Color = LocalAccent.current.base,
    iconBgTint: Color = LocalAccent.current.base.copy(alpha = 0.12f),
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val resolvedIconTint = if (destructive) MaterialTheme.colorScheme.error else iconTint
    val resolvedBg = if (destructive) MaterialTheme.colorScheme.error.copy(alpha = 0.10f) else iconBgTint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(icon = icon, tint = resolvedIconTint, bgTint = resolvedBg)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = titleColor, letterSpacing = (-0.1).sp)
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(subtitle, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                    lineHeight = 15.sp)
            }
        }
        when {
            trailing != null -> trailing()
            valueText != null -> Text(valueText, fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
            onClick != null -> Text("›", fontSize = 17.sp, fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val accent = LocalAccent.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(icon, accent.base, accent.base.copy(alpha = 0.12f))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                letterSpacing = (-0.1).sp)
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(subtitle, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent.base,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsProfileHeader(
    name: String,
    email: String,
    planLabel: String,
    isBusiness: Boolean,
    onEditClick: () -> Unit,
    onViewPlansClick: () -> Unit
) {
    val accent = LocalAccent.current
    val initials = name.trim().split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(58.dp).clip(CircleShape).background(accent.base.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                color = accent.base, letterSpacing = 0.5.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(name.ifBlank { "Set your name" }, fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(2.dp))
            Text(email.ifBlank { "No email" }, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f))
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isBusiness) accent.base.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .clickable { onViewPlansClick() }
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(planLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp,
                    color = if (isBusiness) accent.base
                    else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Filled.Edit, "Edit profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp))
        }
    }
}