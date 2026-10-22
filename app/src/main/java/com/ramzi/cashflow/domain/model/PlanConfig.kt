package com.ramzi.cashflow.domain.model

/**
 * Konfigurasi satu paket langganan.
 * Padanan `PLANS` di core.js.
 */
data class PlanConfig(
    val plan: Plan,
    val label: String,
    val limit: Int,              // Int.MAX_VALUE = unlimited
    val priceMonthly: Long,      // Rupiah
    val priceYearly: Long,       // Rupiah
    val badge: String?,
    val features: List<String>
) {
    val isUnlimited: Boolean get() = limit == Int.MAX_VALUE

    val limitLabel: String get() = if (isUnlimited) "∞" else limit.toString()

    fun priceFor(billing: BillingMode): Long =
        if (billing == BillingMode.YEARLY) priceYearly else priceMonthly

    fun periodLabel(billing: BillingMode): String = when {
        priceMonthly == 0L -> ""
        billing == BillingMode.YEARLY -> "/tahun"
        else -> "/bulan"
    }
}

/**
 * Data semua plan Cashflow.
 *
 * Padanan dari core.js:
 *   const PLANS = {
 *     free:     { label: 'Free',     limit: 10,           price: 0,     priceYearly: 0 },
 *     pro:      { label: 'Pro',      limit: 100,          price: 29900, priceYearly: 299000 },
 *     business: { label: 'Business', limit: Infinity,     price: 49900, priceYearly: 499000 }
 *   };
 */
object PlanConfigs {

    val FREE = PlanConfig(
        plan = Plan.FREE,
        label = "Free",
        limit = 10,
        priceMonthly = 0L,
        priceYearly = 0L,
        badge = null,
        features = listOf(
            "10 transaksi",
            "Laporan dasar",
            "1 pengguna",
            "Kategori standar"
        )
    )

    val PRO = PlanConfig(
        plan = Plan.PRO,
        label = "Pro",
        limit = 100,
        priceMonthly = 29_900L,
        priceYearly = 299_000L,
        badge = "POPULER",
        features = listOf(
            "100 transaksi",
            "Laporan lengkap",
            "Export CSV",
            "Support email",
            "Warna aksen kustom"
        )
    )

    val BUSINESS = PlanConfig(
        plan = Plan.BUSINESS,
        label = "Business",
        limit = Int.MAX_VALUE,
        priceMonthly = 49_900L,
        priceYearly = 499_000L,
        badge = "TERLENGKAP",
        features = listOf(
            "Unlimited transaksi",
            "Semua fitur Pro",
            "Business Analytics dashboard",
            "Export CSV, JSON & Backup",
            "Tren 30 hari & forecasting",
            "Prioritas support 24/7",
            "Multi-user (5 anggota tim)",
            "Custom kategori tak terbatas",
            "Laporan YoY & MoM",
            "Budget planning & alert",
            "API access & Webhook",
            "Backup otomatis harian",
            "17 warna aksen eksklusif"
        )
    )

    /** Map semua plan — dipakai di PlansScreen & PlanConfig.of(). */
    val all: Map<Plan, PlanConfig> = mapOf(
        Plan.FREE to FREE,
        Plan.PRO to PRO,
        Plan.BUSINESS to BUSINESS
    )

    /** Ambil config berdasarkan plan. Default: Free. */
    fun of(plan: Plan): PlanConfig = all[plan] ?: FREE

    /** List berurutan untuk ditampilkan (Free → Pro → Business). */
    val ordered: List<PlanConfig> = listOf(FREE, PRO, BUSINESS)
}