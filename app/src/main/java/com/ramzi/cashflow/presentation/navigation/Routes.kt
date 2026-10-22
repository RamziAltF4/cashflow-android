package com.ramzi.cashflow.presentation.navigation

/**
 * Daftar route Navigation Compose.
 *
 * Padanan konseptual dari:
 *   - login.js → window.location.href = 'dashboard.html'
 *   - goView('view-login-email') → route "login"
 *   - goView('view-reg-email-1') → route "register"
 *
 * Dashboard memakai DashboardShell dengan state-based tab
 * (bukan route per section) karena mirip SPA.
 */
object Routes {

    // ---------- AUTH ----------
    const val SPLASH    = "splash"
    const val LOGIN     = "login"
    const val REGISTER  = "register"
    const val PIN_LOGIN = "pin_login"

    // ---------- DASHBOARD ----------
    const val DASHBOARD = "dashboard"
    const val IMPORTS = "imports" 

    // ---------- TODO (BATCH berikutnya) ----------
    // const val TRANSACTIONS = "transactions"
    // const val REPORT       = "report"
    // const val PLANS        = "plans"
    // const val SETTINGS     = "settings"
}