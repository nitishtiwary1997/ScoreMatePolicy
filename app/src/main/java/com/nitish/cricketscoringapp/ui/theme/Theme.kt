package com.nitish.cricketscoringapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CricketDarkColorScheme = darkColorScheme(
    // Primary – emerald green
    primary                = EmeraldPrimary,
    onPrimary              = Color(0xFF000000),
    primaryContainer       = EmeraldContainer,
    onPrimaryContainer     = EmeraldLight,

    // Secondary – premium gold
    secondary              = GoldPrimary,
    onSecondary            = Color(0xFF000000),
    secondaryContainer     = GoldContainer,
    onSecondaryContainer   = GoldLight,

    // Tertiary – muted teal accent
    tertiary               = Color(0xFF00BCD4),
    onTertiary             = Color(0xFF000000),
    tertiaryContainer      = Color(0xFF003038),
    onTertiaryContainer    = Color(0xFF80DEEA),

    // Error
    error                  = CricketRed,
    onError                = Color(0xFF000000),
    errorContainer         = CricketRedDim,
    onErrorContainer       = Color(0xFFFF8A80),

    // Backgrounds / surfaces
    background             = DarkBg,
    onBackground           = TextPrimary,
    surface                = DarkSurface,
    onSurface              = TextPrimary,
    surfaceVariant         = DarkSurface2,
    onSurfaceVariant       = TextSecondary,
    surfaceTint            = EmeraldPrimary,

    // Outline
    outline                = OutlineColor,
    outlineVariant         = DividerColor,
    inverseSurface         = TextPrimary,
    inverseOnSurface       = DarkBg,
    inversePrimary         = EmeraldDark,
    scrim                  = Color(0x99000000)
)

@Composable
fun CricketScoringAppTheme(
    darkTheme: Boolean = true,           // Always dark – can be toggled by user setting
    content: @Composable () -> Unit
) {
    val colorScheme = CricketDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
