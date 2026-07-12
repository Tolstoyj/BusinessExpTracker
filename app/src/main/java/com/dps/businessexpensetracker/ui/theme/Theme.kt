package com.dps.businessexpensetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LedgerGreen80,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF003731),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF005047),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFA0F2E2),
    secondary = SteelBlue80,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF0B3540),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF244C57),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFBFE9F6),
    tertiary = Amber80,
    onTertiary = androidx.compose.ui.graphics.Color(0xFF4A2800),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF6A3B00),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDDB8),
    background = androidx.compose.ui.graphics.Color(0xFF0F1513),
    onBackground = androidx.compose.ui.graphics.Color(0xFFDEE4E1),
    surface = androidx.compose.ui.graphics.Color(0xFF0F1513),
    onSurface = androidx.compose.ui.graphics.Color(0xFFDEE4E1),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF3F4946),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBEC9C5),
    outline = androidx.compose.ui.graphics.Color(0xFF89938F),
    surfaceDim = androidx.compose.ui.graphics.Color(0xFF0F1513),
    surfaceBright = androidx.compose.ui.graphics.Color(0xFF353B39),
    surfaceContainerLowest = androidx.compose.ui.graphics.Color(0xFF0A0F0E),
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFF171D1B),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF1B211F),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF252B29),
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF303633)
)

private val LightColorScheme = lightColorScheme(
    primary = LedgerGreen40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFA0F2E2),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF00201C),
    secondary = SteelBlue40,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFBFE9F6),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF001F28),
    tertiary = Amber40,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDDB8),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF2D1600),
    background = androidx.compose.ui.graphics.Color(0xFFF5F8F6),
    onBackground = androidx.compose.ui.graphics.Color(0xFF171D1B),
    surface = androidx.compose.ui.graphics.Color(0xFFFAFDFB),
    onSurface = androidx.compose.ui.graphics.Color(0xFF171D1B),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFDCE5E1),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF3F4946),
    outline = androidx.compose.ui.graphics.Color(0xFF6F7975),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFBEC9C5),
    surfaceDim = androidx.compose.ui.graphics.Color(0xFFD5DBD8),
    surfaceBright = androidx.compose.ui.graphics.Color(0xFFFAFDFB),
    surfaceContainerLowest = androidx.compose.ui.graphics.Color.White,
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFFF0F4F1),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFFEAEFEC),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFFE4E9E6),
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFFDEE4E1)
)

@Composable
fun BusinessExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
