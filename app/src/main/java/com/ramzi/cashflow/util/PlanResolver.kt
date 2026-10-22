package com.ramzi.cashflow.util

import android.content.Context
import com.ramzi.cashflow.domain.model.Plan

object PlanResolver {

    @Volatile
    private var cachedPlan: Plan? = null

    fun currentPlan(context: Context): Plan {
        cachedPlan?.let { return it }

        val plan = resolveFromAnySource(context)
        cachedPlan = plan
        return plan
    }

    fun update(plan: Plan) {
        cachedPlan = plan
    }

    private fun resolveFromAnySource(context: Context): Plan {
        val prefs = context.getSharedPreferences("cashflow_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("user_plan", "free") ?: "free"

        return when (raw.lowercase()) {
            "pro" -> Plan.PRO
            "business" -> Plan.BUSINESS
            else -> Plan.FREE
        }
    }
}