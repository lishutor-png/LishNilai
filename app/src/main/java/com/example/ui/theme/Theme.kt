package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDarkTheme,
    onPrimary = SurfaceDark,
    primaryContainer = TealPrimaryDark,
    onPrimaryContainer = TealPrimaryContainer,
    secondary = IndigoSecondaryDarkTheme,
    onSecondary = SurfaceDark,
    secondaryContainer = IndigoSecondary,
    onSecondaryContainer = IndigoSecondaryContainer,
    tertiary = AmberTertiary,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCardDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = RedError,
    errorContainer = RedErrorContainer,
    onError = OnRedError,
    onErrorContainer = OnRedErrorContainer,
    outline = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceCardLight,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = OnTealPrimaryContainer,
    secondary = IndigoSecondary,
    onSecondary = SurfaceCardLight,
    secondaryContainer = IndigoSecondaryContainer,
    onSecondaryContainer = OnIndigoSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = SurfaceCardLight,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = OnAmberTertiaryContainer,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceElevated,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = RedError,
    errorContainer = RedErrorContainer,
    onError = OnRedError,
    onErrorContainer = OnRedErrorContainer,
    outline = OutlineLight
)

@Composable
fun LishNilaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
