package com.example.core.appearance

import androidx.compose.ui.graphics.Color

/**
 * Purple Launcher visual system.
 *
 * Appearance is intentionally independent from LauncherProfile.
 * The product name does not imply a purple color scheme.
 */
data class LauncherAppearance(
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primary: Color,
    val secondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val divider: Color,
    val scrim: Color,

    val cornerRadius: Float,
    val surfaceAlpha: Float,
    val borderWidth: Float,

    val useDynamicColor: Boolean,
    val useWallpaperColor: Boolean
) {
    companion object {
        /**
         * Neutral fallback appearance.
         *
         * Dynamic/wallpaper-derived colors can replace these at runtime.
         * Nothing here depends on the product name "Purple Launcher".
         */
        fun neutral(dark: Boolean = true): LauncherAppearance {
            return if (dark) {
                LauncherAppearance(
                    background = Color(0xFF0B0B0D),
                    surface = Color(0xFF151518),
                    elevatedSurface = Color(0xFF1D1D21),
                    primary = Color(0xFFE7E7EA),
                    secondary = Color(0xFFB8B8BE),
                    onBackground = Color(0xFFF5F5F7),
                    onSurface = Color(0xFFF5F5F7),
                    onSurfaceVariant = Color(0xFFB4B4BA),
                    divider = Color(0x33222227),
                    scrim = Color(0x99000000),
                    cornerRadius = 24f,
                    surfaceAlpha = 0.92f,
                    borderWidth = 1f,
                    useDynamicColor = false,
                    useWallpaperColor = false
                )
            } else {
                LauncherAppearance(
                    background = Color(0xFFF7F7F8),
                    surface = Color(0xFFFFFFFF),
                    elevatedSurface = Color(0xFFF0F0F2),
                    primary = Color(0xFF202124),
                    secondary = Color(0xFF5F6368),
                    onBackground = Color(0xFF18181A),
                    onSurface = Color(0xFF18181A),
                    onSurfaceVariant = Color(0xFF5F6368),
                    divider = Color(0x2218181A),
                    scrim = Color(0x55000000),
                    cornerRadius = 24f,
                    surfaceAlpha = 0.94f,
                    borderWidth = 1f,
                    useDynamicColor = false,
                    useWallpaperColor = false
                )
            }
        }
    }
}
