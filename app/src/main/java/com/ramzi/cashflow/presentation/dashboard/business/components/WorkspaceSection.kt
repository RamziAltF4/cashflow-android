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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.Workspace

/**
 * Section workspace / multi-akun.
 * User bisa pindah workspace, tambah, dan hapus.
 */
@Composable
fun WorkspaceSection(
    workspaces: List<Workspace>,
    currentWorkspaceId: String,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
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
                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Workspaces,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Workspace",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Kelola multi-akun / proyek",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF6366F1))
                    .clickable { onAdd() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Tambah",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ LIST ============
        workspaces.forEach { ws ->
            WorkspaceItem(
                workspace = ws,
                isActive = ws.id == currentWorkspaceId,
                isDefault = ws.id == Workspace.DEFAULT_ID,
                onSelect = { onSelect(ws.id) },
                onDelete = { onDelete(ws.id) }
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun WorkspaceItem(
    workspace: Workspace,
    isActive: Boolean,
    isDefault: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = if (isActive)
        Color(0xFF6366F1).copy(alpha = 0.10f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val borderColor = if (isActive)
        Color(0xFF6366F1).copy(alpha = 0.4f)
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isActive) Color(0xFF6366F1)
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = workspace.initial,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = workspace.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = if (isActive) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isDefault) "Workspace utama" else "Workspace tambahan",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isActive) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(18.dp)
            )
        } else if (!isDefault) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}