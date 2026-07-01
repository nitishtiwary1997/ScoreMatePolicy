package com.cric.cricketscoring.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val outline: Color,
    val divider: Color,
    val isDark: Boolean
)

fun darkAppColors() = AppColors(
    bg            = Color(0xFF0A0E14),
    surface       = Color(0xFF131920),
    surface2      = Color(0xFF1A2230),
    surface3      = Color(0xFF1F2A3C),
    textPrimary   = Color(0xFFF0F4FF),
    textSecondary = Color(0xFF8A9BB5),
    textTertiary  = Color(0xFF4A5568),
    outline       = Color(0xFF2A3A52),
    divider       = Color(0xFF1E2D44),
    isDark        = true
)

fun lightAppColors() = AppColors(
    bg            = Color(0xFFF3F7FB),
    surface       = Color(0xFFFFFFFF),
    surface2      = Color(0xFFEDF2F8),
    surface3      = Color(0xFFE2EAF4),
    textPrimary   = Color(0xFF0D1927),
    textSecondary = Color(0xFF546E8A),
    textTertiary  = Color(0xFF8FA3B8),
    outline       = Color(0xFFCCD6E0),
    divider       = Color(0xFFDDE5ED),
    isDark        = false
)

val LocalAppColors = compositionLocalOf { darkAppColors() }
