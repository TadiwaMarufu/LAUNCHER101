package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.core.appearance.AppearanceEngine
import com.example.core.appearance.LauncherAppearance

/**
 * Global Purple Launcher appearance.
 *
 * Personality controls behavior.
 * Appearance controls presentation.
 */
val LocalLauncherAppearance =
    staticCompositionLocalOf {
        LauncherAppearance.neutral(true)
    }

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val appearance = AppearanceEngine.resolve(
        context = context,
        dark = darkTheme,
        dynamicColor = dynamicColor
    )

    val colorScheme =
        if (
            dynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else if (darkTheme) {
            darkColorScheme(
                primary = appearance.primary,
                secondary = appearance.secondary,
                background = appearance.background,
                surface = appearance.surface,
                surfaceContainerHigh = appearance.elevatedSurface,
                onBackground = appearance.onBackground,
                onSurface = appearance.onSurface,
                onSurfaceVariant = appearance.onSurfaceVariant
            )
        } else {
            lightColorScheme(
                primary = appearance.primary,
                secondary = appearance.secondary,
                background = appearance.background,
                surface = appearance.surface,
                surfaceContainerHigh = appearance.elevatedSurface,
                onBackground = appearance.onBackground,
                onSurface = appearance.onSurface,
                onSurfaceVariant = appearance.onSurfaceVariant
            )
        }

    CompositionLocalProvider(
        LocalLauncherAppearance provides appearance
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
