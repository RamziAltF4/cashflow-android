package com.ramzi.cashflow.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ramzi.cashflow.data.repository.UserRepositoryImpl
import com.ramzi.cashflow.domain.model.Plan
import com.ramzi.cashflow.domain.model.PlanConfigs
import com.ramzi.cashflow.domain.model.TxType
import com.ramzi.cashflow.presentation.auth.LoginScreen
import com.ramzi.cashflow.presentation.auth.PinLoginScreen
import com.ramzi.cashflow.presentation.auth.RegisterScreen
import com.ramzi.cashflow.presentation.dashboard.DashboardEventBus
import com.ramzi.cashflow.presentation.dashboard.DashboardShell
import com.ramzi.cashflow.presentation.dashboard.DashboardTab
import com.ramzi.cashflow.presentation.dashboard.business.BusinessScreen
import com.ramzi.cashflow.presentation.dashboard.home.HomeScreen
import com.ramzi.cashflow.presentation.dashboard.imports.ImportsScreen
import com.ramzi.cashflow.presentation.dashboard.plans.PlansScreen
import com.ramzi.cashflow.presentation.dashboard.report.ReportScreen
import com.ramzi.cashflow.presentation.dashboard.settings.SettingsScreen
import com.ramzi.cashflow.presentation.dashboard.tx.TxFormScreen
import com.ramzi.cashflow.presentation.dashboard.tx.TxScreen
import com.ramzi.cashflow.presentation.dashboard.tx.TxViewModel
import com.ramzi.cashflow.presentation.dashboard.tx.TxViewModelFactory
import com.ramzi.cashflow.presentation.splash.SplashScreen

@Composable
fun CashflowNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onAuthenticated = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) },
                onGoToPinLogin = { navController.navigate(Routes.PIN_LOGIN) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PIN_LOGIN) {
            PinLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        /* ============================================================
           DASHBOARD
           ============================================================ */
        composable(Routes.DASHBOARD) {
            DashboardRoute(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                    FirebaseAuth.getInstance().signOut()
                },
                onNavigateToImports = {
                    navController.navigate(Routes.IMPORTS)
                }
            )
        }

        /* ============================================================
           IMPORTS — Transaksi Terdeteksi dari Notifikasi BCA
           ============================================================ */
        composable(Routes.IMPORTS) {
            ImportsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/* ============================================================
   DASHBOARD ROUTE
   ============================================================ */
@Composable
private fun DashboardRoute(
    onLogout: () -> Unit,
    onNavigateToImports: () -> Unit
) {
    val eventBus = remember { DashboardEventBus() }
    val txViewModel: TxViewModel = viewModel(factory = TxViewModelFactory(LocalContext.current))
    val txState by txViewModel.state.collectAsState()
    val formVisible = txState.formOpen

    /* ---------- Observe user plan + name dari Firestore ---------- */
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val userRepo = remember {
        UserRepositoryImpl(FirebaseFirestore.getInstance())
    }
    var userPlan by remember { mutableStateOf(Plan.FREE) }
    var planLabel by remember { mutableStateOf("Free") }
    var userName by remember { mutableStateOf("") }

    val uid = firebaseAuth.currentUser?.uid
    if (uid != null) {
        LaunchedEffect(uid) {
            userRepo.observeById(uid).collect { user ->
                if (user != null) {
                    userPlan = user.plan
                    planLabel = PlanConfigs.of(user.plan).label
                    userName = user.name
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DashboardShell(
            userPlanLabel = planLabel,
            userPlan = userPlan,
            userName = userName,
            eventBus = eventBus,
            onLogout = onLogout
        ) { tab ->
            when (tab) {
                DashboardTab.HOME -> HomeScreen(
                    onAddTransaction = { type ->
                        val txType = if (type == "expense") TxType.EXPENSE else TxType.INCOME
                        eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.TX))
                        txViewModel.openAddForm(txType)
                    },
                    onNavigateToTx = {
                        eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.TX))
                    },
                    onNavigateToReport = {
                        eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.REPORT))
                    },
                    onNavigateToPlans = {
                        eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.PLANS))
                    },
                    onNavigateToImports = onNavigateToImports
                )

                DashboardTab.TX -> TxScreen(viewModel = txViewModel)

                DashboardTab.REPORT -> ReportScreen()

                DashboardTab.BUSINESS -> {
                    if (userPlan == Plan.BUSINESS) {
                        BusinessScreen()
                    } else {
                        LaunchedEffect(Unit) {
                            eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.HOME))
                        }
                        HomeScreen(
                            onAddTransaction = { type ->
                                val txType = if (type == "expense") TxType.EXPENSE else TxType.INCOME
                                eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.TX))
                                txViewModel.openAddForm(txType)
                            },
                            onNavigateToTx = {
                                eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.TX))
                            },
                            onNavigateToReport = {
                                eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.REPORT))
                            },
                            onNavigateToPlans = {
                                eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.PLANS))
                            },
                            onNavigateToImports = onNavigateToImports
                        )
                    }
                }

                DashboardTab.PLANS -> PlansScreen()

                DashboardTab.SETTINGS -> SettingsScreen(
                    onLogout = onLogout,
                    onNavigateToPlans = {
                        eventBus.send(DashboardEventBus.Event.SwitchTab(DashboardTab.PLANS))
                    },
                    onNavigateToImports = onNavigateToImports
                )
            }
        }

        if (formVisible) {
            TxFormScreen(viewModel = txViewModel)
        }
    }
}

/* ============================================================
   PLACEHOLDER
   ============================================================ */
@Composable
private fun TabPlaceholder(tab: DashboardTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${tab.label} (placeholder)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}