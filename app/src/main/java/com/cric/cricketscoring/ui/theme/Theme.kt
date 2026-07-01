package com.cric.cricketscoring.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CricketDarkColorScheme = darkColorScheme(
    primary                = EmeraldPrimary,
    onPrimary              = Color(0xFF000000),
    primaryContainer       = EmeraldContainer,
    onPrimaryContainer     = EmeraldLight,
    secondary              = GoldPrimary,
    onSecondary            = Color(0xFF000000),
    secondaryContainer     = GoldContainer,
    onSecondaryContainer   = GoldLight,
    tertiary               = Color(0xFF00BCD4),
    onTertiary             = Color(0xFF000000),
    tertiaryContainer      = Color(0xFF003038),
    onTertiaryContainer    = Color(0xFF80DEEA),
    error                  = CricketRed,
    onError                = Color(0xFF000000),
    errorContainer         = CricketRedDim,
    onErrorContainer       = Color(0xFFFF8A80),
    background             = Color(0xFF0A0E14),
    onBackground           = Color(0xFFF0F4FF),
    surface                = Color(0xFF131920),
    onSurface              = Color(0xFFF0F4FF),
    surfaceVariant         = Color(0xFF1A2230),
    onSurfaceVariant       = Color(0xFF8A9BB5),
    surfaceTint            = EmeraldPrimary,
    outline                = Color(0xFF2A3A52),
    outlineVariant         = Color(0xFF1E2D44),
    inverseSurface         = Color(0xFFF0F4FF),
    inverseOnSurface       = Color(0xFF0A0E14),
    inversePrimary         = EmeraldDark,
    scrim                  = Color(0x99000000)
)

private val CricketLightColorScheme = lightColorScheme(
    primary                = EmeraldDark,
    onPrimary              = Color(0xFFFFFFFF),
    primaryContainer       = Color(0xFFB7F5D4),
    onPrimaryContainer     = Color(0xFF003920),
    secondary              = Color(0xFFB8860B),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFFFF1B8),
    onSecondaryContainer   = Color(0xFF3D2E00),
    tertiary               = Color(0xFF0097A7),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFB2EBF2),
    onTertiaryContainer    = Color(0xFF001F24),
    error                  = Color(0xFFD32F2F),
    onError                = Color(0xFFFFFFFF),
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002),
    background             = Color(0xFFF3F7FB),
    onBackground           = Color(0xFF0D1927),
    surface                = Color(0xFFFFFFFF),
    onSurface              = Color(0xFF0D1927),
    surfaceVariant         = Color(0xFFEDF2F8),
    onSurfaceVariant       = Color(0xFF546E8A),
    surfaceTint            = EmeraldPrimary,
    outline                = Color(0xFFCCD6E0),
    outlineVariant         = Color(0xFFDDE5ED),
    inverseSurface         = Color(0xFF0D1927),
    inverseOnSurface       = Color(0xFFF0F4FF),
    inversePrimary         = EmeraldLight,
    scrim                  = Color(0x99000000)
)

@Composable
fun CricketScoringAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CricketDarkColorScheme else CricketLightColorScheme
    val appColors   = if (darkTheme) darkAppColors() else lightAppColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
