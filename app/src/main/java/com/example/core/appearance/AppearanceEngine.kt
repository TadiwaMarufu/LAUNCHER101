package com.example.core.appearance

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Resolves Purple Launcher appearance independently from personality.
 *
 * Personalities describe behavior.
 * Appearance describes presentation.
 */
object AppearanceEngine {

    fun resolve(
        context: Context,
        dark: Boolean,
        dynamicColor: Boolean = true
    ): LauncherAppearance {
        if (
            dynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            val scheme = if (dark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }

            return LauncherAppearance(
                background = scheme.background,
                surface = scheme.surface,
                elevatedSurface = scheme.surfaceContainerHigh,
                primary = scheme.primary,
                secondary = scheme.secondary,
                onBackground = scheme.onBackground,
                onSurface = scheme.onSurface,
                onSurfaceVariant = scheme.onSurfaceVariant,
                divider = scheme.outline.copy(alpha = 0.20f),
                scrim = scheme.scrim,
                cornerRadius = 24f,
                surfaceAlpha = 0.92f,
                borderWidth = 1f,
                useDynamicColor = true,
                useWallpaperColor = true
            )
        }

        return LauncherAppearance.neutral(dark)
    }
}
