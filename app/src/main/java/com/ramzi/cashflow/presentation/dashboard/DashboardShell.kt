package com.ramzi.cashflow.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzi.cashflow.domain.model.Plan
import com.ramzi.cashflow.presentation.theme.LocalAccent
import kotlinx.coroutines.launch

enum class DashboardTab(
    val label: String,
    val icon: ImageVector,
    val showInBottomBar: Boolean = true
) {
    HOME("Beranda", Icons.Filled.Home, showInBottomBar = true),
    TX("Transaksi", Icons.AutoMirrored.Filled.List, showInBottomBar = true),
    BUSINESS("Bisnis", Icons.Filled.BusinessCenter, showInBottomBar = true),
    REPORT("Laporan", Icons.Filled.BarChart, showInBottomBar = true),
    SETTINGS("Akun", Icons.Filled.AccountCircle, showInBottomBar = true),
    PLANS("Upgrade", Icons.Filled.Star, showInBottomBar = false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardShell(
    userPlanLabel: String = "Free",
    userPlan: Plan = Plan.FREE,
    userName: String = "",
    eventBus: DashboardEventBus,
    onLogout: () -> Unit,
    content: @Composable (tab: DashboardTab) -> Unit
) {
    val accent = LocalAccent.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val isBusinessUser = userPlan == Plan.BUSINESS

    val allTabs = remember(userPlan) {
        if (isBusinessUser) {
            listOf(
                DashboardTab.HOME,
                DashboardTab.TX,
                DashboardTab.REPORT,
                DashboardTab.BUSINESS,
                DashboardTab.SETTINGS
            )
        } else {
            listOf(
                DashboardTab.HOME,
                DashboardTab.TX,
                DashboardTab.REPORT,
                DashboardTab.SETTINGS,
                DashboardTab.PLANS
            )
        }
    }

    val bottomTabs = remember(allTabs, isBusinessUser) {
        if (isBusinessUser) {
            listOf(
                DashboardTab.HOME,
                DashboardTab.TX,
                DashboardTab.REPORT,
                DashboardTab.BUSINESS,
                DashboardTab.SETTINGS
            )
        } else {
            listOf(
                DashboardTab.HOME,
                DashboardTab.TX,
                DashboardTab.REPORT,
                DashboardTab.SETTINGS
            )
        }
    }

    val initial = userName.trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "C"

    val displayName = userName.trim().ifBlank { "Cashflow" }

    // Reset kalau selectedTab out of bounds atau jika tab tidak lagi ada di allTabs
    LaunchedEffect(allTabs.size, isBusinessUser) {
        if (selectedTab >= allTabs.size) {
            selectedTab = 0
        }
    }

    // Event bus listener dengan strict access gating
    LaunchedEffect(eventBus, allTabs, isBusinessUser) {
        eventBus.events.collect { event ->
            when (event) {
                is DashboardEventBus.Event.SwitchTab -> {
                    // Keamanan: Tolak akses ke Business jika bukan user Business
                    if (event.tab == DashboardTab.BUSINESS && !isBusinessUser) {
                        return@collect
                    }
                    val target = allTabs.indexOf(event.tab)
                    if (target >= 0) selectedTab = target
                }
                is DashboardEventBus.Event.OpenAddTransaction -> {
                    val target = allTabs.indexOf(DashboardTab.TX)
                    if (target >= 0) selectedTab = target
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accent.base),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            userPlanLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Drawer pakai `allTabs` (termasuk BUSINESS untuk user Business)
                allTabs.forEachIndexed { index, tab ->
                    DrawerItem(
                        tab = tab,
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLogout() }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Keluar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Keluar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = allTabs.getOrNull(selectedTab)?.label ?: "Cashflow",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accent.soft)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = userPlanLabel.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accent.base
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        bottomTabs.forEach { tab ->
                            val targetIndex = allTabs.indexOf(tab)
                            val isSelected = selectedTab == targetIndex
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = targetIndex },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        tab.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accent.base,
                                    selectedTextColor = accent.base,
                                    indicatorColor = accent.soft,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val currentTab = allTabs.getOrNull(selectedTab) ?: DashboardTab.HOME
                if (currentTab == DashboardTab.BUSINESS && !isBusinessUser) {
                    // Security guard: Non-business users cannot render Business tab
                    LaunchedEffect(Unit) {
                        selectedTab = 0
                    }
                    content(DashboardTab.HOME)
                } else {
                    content(currentTab)
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    tab: DashboardTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accent.soft else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (selected) accent.base
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            tab.label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) accent.text else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
    }
}