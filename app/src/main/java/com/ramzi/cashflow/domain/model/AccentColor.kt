package com.ramzi.cashflow.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 17 accent warna dari `ACCENTS` di core.js.
 *
 * Free/Pro hanya boleh 5 warna pertama (businessOnly = false).
 * Business boleh semua (17 warna).
 *
 * Setiap accent punya 8 token warna (base, hover, light, soft, soft2, text, ring, glow)
 * yang memetakan CSS variables di web:
 *   --accent, --accent-hover, --accent-light, --accent-soft, --accent-soft-2,
 *   --accent-text, --accent-ring, --accent-glow
 */
data class AccentColor(
    val id: String,
    val label: String,
    val base: Color,
    val hover: Color,
    val light: Color,
    val soft: Color,
    val soft2: Color,
    val text: Color,
    val ring: Color,
    val glow: Color,
    val businessOnly: Boolean
)

object AccentColors {

    // ============================================================
    // 5 WARNA DASAR (Free & Pro & Business)
    // ============================================================

    val INDIGO = AccentColor(
        id = "indigo", label = "Indigo",
        base = Color(0xFF4F46E5), hover = Color(0xFF4338CA), light = Color(0xFF6366F1),
        soft = Color(0xFFEEF2FF), soft2 = Color(0xFFE0E7FF), text = Color(0xFF3730A3),
        ring = Color(0x266366F1), glow = Color(0x594F46E5),
        businessOnly = false
    )

    val VIOLET = AccentColor(
        id = "violet", label = "Violet",
        base = Color(0xFF7C3AED), hover = Color(0xFF6D28D9), light = Color(0xFF8B5CF6),
        soft = Color(0xFFF5F3FF), soft2 = Color(0xFFEDE9FE), text = Color(0xFF5B21B6),
        ring = Color(0x268B5CF6), glow = Color(0x597C3AED),
        businessOnly = false
    )

    val EMERALD = AccentColor(
        id = "emerald", label = "Emerald",
        base = Color(0xFF059669), hover = Color(0xFF047857), light = Color(0xFF10B981),
        soft = Color(0xFFECFDF5), soft2 = Color(0xFFD1FAE5), text = Color(0xFF065F46),
        ring = Color(0x2610B981), glow = Color(0x59059669),
        businessOnly = false
    )

    val ROSE = AccentColor(
        id = "rose", label = "Rose",
        base = Color(0xFFE11D48), hover = Color(0xFFBE123C), light = Color(0xFFF43F5E),
        soft = Color(0xFFFFF1F2), soft2 = Color(0xFFFFE4E6), text = Color(0xFF9F1239),
        ring = Color(0x26F43F5E), glow = Color(0x59E11D48),
        businessOnly = false
    )

    val AMBER = AccentColor(
        id = "amber", label = "Amber",
        base = Color(0xFFD97706), hover = Color(0xFFB45309), light = Color(0xFFF59E0B),
        soft = Color(0xFFFFFBEB), soft2 = Color(0xFFFEF3C7), text = Color(0xFF92400E),
        ring = Color(0x26F59E0B), glow = Color(0x59D97706),
        businessOnly = false
    )

    // ============================================================
    // 12 WARNA BUSINESS-ONLY
    // ============================================================

    val OCEAN = AccentColor(
        id = "ocean", label = "Ocean",
        base = Color(0xFF0891B2), hover = Color(0xFF0E7490), light = Color(0xFF06B6D4),
        soft = Color(0xFFECFEFF), soft2 = Color(0xFFCFFAFE), text = Color(0xFF155E75),
        ring = Color(0x2606B6D4), glow = Color(0x590891B2),
        businessOnly = true
    )

    val SUNSET = AccentColor(
        id = "sunset", label = "Sunset",
        base = Color(0xFFEA580C), hover = Color(0xFFC2410C), light = Color(0xFFF97316),
        soft = Color(0xFFFFF7ED), soft2 = Color(0xFFFFEDD5), text = Color(0xFF9A3412),
        ring = Color(0x26F97316), glow = Color(0x59EA580C),
        businessOnly = true
    )

    val PINK = AccentColor(
        id = "pink", label = "Pink",
        base = Color(0xFFDB2777), hover = Color(0xFFBE185D), light = Color(0xFFEC4899),
        soft = Color(0xFFFDF2F8), soft2 = Color(0xFFFCE7F3), text = Color(0xFF9D174D),
        ring = Color(0x26EC4899), glow = Color(0x59DB2777),
        businessOnly = true
    )

    val TEAL = AccentColor(
        id = "teal", label = "Teal",
        base = Color(0xFF0D9488), hover = Color(0xFF0F766E), light = Color(0xFF14B8A6),
        soft = Color(0xFFF0FDFA), soft2 = Color(0xFFCCFBF1), text = Color(0xFF115E59),
        ring = Color(0x2614B8A6), glow = Color(0x590D9488),
        businessOnly = true
    )

    val LIME = AccentColor(
        id = "lime", label = "Lime",
        base = Color(0xFF65A30D), hover = Color(0xFF4D7C0F), light = Color(0xFF84CC16),
        soft = Color(0xFFF7FEE7), soft2 = Color(0xFFECFCCB), text = Color(0xFF3F6212),
        ring = Color(0x2684CC16), glow = Color(0x5965A30D),
        businessOnly = true
    )

    val CYAN = AccentColor(
        id = "cyan", label = "Cyan",
        base = Color(0xFF0284C7), hover = Color(0xFF0369A1), light = Color(0xFF0EA5E9),
        soft = Color(0xFFF0F9FF), soft2 = Color(0xFFE0F2FE), text = Color(0xFF075985),
        ring = Color(0x260EA5E9), glow = Color(0x590284C7),
        businessOnly = true
    )

    val FUCHSIA = AccentColor(
        id = "fuchsia", label = "Fuchsia",
        base = Color(0xFFC026D3), hover = Color(0xFFA21CAF), light = Color(0xFFD946EF),
        soft = Color(0xFFFDF4FF), soft2 = Color(0xFFFAE8FF), text = Color(0xFF86198F),
        ring = Color(0x26D946EF), glow = Color(0x59C026D3),
        businessOnly = true
    )

    val SLATE = AccentColor(
        id = "slate", label = "Slate",
        base = Color(0xFF475569), hover = Color(0xFF334155), light = Color(0xFF64748B),
        soft = Color(0xFFF8FAFC), soft2 = Color(0xFFF1F5F9), text = Color(0xFF1E293B),
        ring = Color(0x2664748B), glow = Color(0x59475569),
        businessOnly = true
    )

    val GOLD = AccentColor(
        id = "gold", label = "Gold",
        base = Color(0xFFCA8A04), hover = Color(0xFFA16207), light = Color(0xFFEAB308),
        soft = Color(0xFFFEFCE8), soft2 = Color(0xFFFEF9C3), text = Color(0xFF854D0E),
        ring = Color(0x26EAB308), glow = Color(0x59CA8A04),
        businessOnly = true
    )

    val CRIMSON = AccentColor(
        id = "crimson", label = "Crimson",
        base = Color(0xFFDC2626), hover = Color(0xFFB91C1C), light = Color(0xFFEF4444),
        soft = Color(0xFFFEF2F2), soft2 = Color(0xFFFEE2E2), text = Color(0xFF991B1B),
        ring = Color(0x26EF4444), glow = Color(0x59DC2626),
        businessOnly = true
    )

    val MINT = AccentColor(
        id = "mint", label = "Mint",
        base = Color(0xFF14B8A6), hover = Color(0xFF0D9488), light = Color(0xFF2DD4BF),
        soft = Color(0xFFF0FDFA), soft2 = Color(0xFFCCFBF1), text = Color(0xFF115E59),
        ring = Color(0x262DD4BF), glow = Color(0x5914B8A6),
        businessOnly = true
    )

    val MIDNIGHT = AccentColor(
        id = "midnight", label = "Midnight",
        base = Color(0xFF1E293B), hover = Color(0xFF0F172A), light = Color(0xFF334155),
        soft = Color(0xFFF1F5F9), soft2 = Color(0xFFE2E8F0), text = Color(0xFF0F172A),
        ring = Color(0x26334155), glow = Color(0x591E293B),
        businessOnly = true
    )

    // ============================================================
    // URUTAN & HELPER
    // ============================================================

    val ALL_ORDERED: List<AccentColor> = listOf(
        INDIGO, VIOLET, EMERALD, ROSE, AMBER,
        TEAL, MINT, CYAN, OCEAN,
        PINK, FUCHSIA,
        SUNSET, GOLD, LIME,
        CRIMSON, SLATE, MIDNIGHT
    )

    val DEFAULT: AccentColor = INDIGO

    fun byId(id: String?): AccentColor =
        ALL_ORDERED.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT

    fun forPlan(plan: Plan): List<AccentColor> {
        val allowBusiness = plan == Plan.BUSINESS
        return ALL_ORDERED.filter { allowBusiness || !it.businessOnly }
    }
}