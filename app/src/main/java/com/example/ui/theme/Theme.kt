package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AuraDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextWhite,
    secondary = CyberCyan,
    onSecondary = MidnightBlack,
    tertiary = AccentMagenta,
    onTertiary = TextWhite,
    background = MidnightBlack,
    onBackground = TextWhite,
    surface = MidnightSurface,
    onSurface = TextWhite,
    surfaceVariant = MidnightSurfaceCard,
    onSurfaceVariant = TextGray,
    outline = BorderSlate
)

@Composable
fun AuraTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AuraDarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MidnightBlack.toArgb()
            window.navigationBarColor = MidnightBlack.toArgb()
            
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            // Since it's a deep dark theme, we want light status/nav bar icons
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep MyApplicationTheme for backward-compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AuraTheme(content = content)
}
