package com.example.core.model

import androidx.compose.ui.graphics.Color

/**
 * The Five Personalities of The Purple Launcher.
 * These are not just themes or color presets — they are five different interpretations
 * of what an Android launcher can be.
 */
enum class LauncherProfile(
    val title: String,
    val tagline: String,
    val description: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val backgroundBase: Color,
    val surfaceBase: Color,
    val textPrimary: Color,
    val textSecondary: Color
) {
    FLUID(
        title = "Fluid",
        tagline = "Alive, dynamic, soft, flowing",
        description = "An organic living environment where information breathes and surfaces flow with contextual responsiveness.",
        primaryAccent = Color(0xFFA855F7), // Luminous Purple
        secondaryAccent = Color(0xFFC084FC), // Soft Lilac
        backgroundBase = Color(0xFF0F0B18),
        surfaceBase = Color(0xFF1E172E),
        textPrimary = Color(0xFFF3E8FF),
        textSecondary = Color(0xFFA89BB9)
    ),
    PREMIUM(
        title = "Premium",
        tagline = "Refined, elegant, precise, polished",
        description = "Sophisticated architectural balance with restrained motion, precision typography, and controlled depth.",
        primaryAccent = Color(0xFFC4B5FD), // Platinum Lavender
        secondaryAccent = Color(0xFFE2E8F0), // Cool Platinum
        backgroundBase = Color(0xFF09090B), // Deep Obsidian
        surfaceBase = Color(0xFF18181B), // Graphite Glass
        textPrimary = Color(0xFFFAFAFA),
        textSecondary = Color(0xFFA1A1AA)
    ),
    CALM(
        title = "Calm",
        tagline = "Quiet, minimal, peaceful, restrained",
        description = "Intentionally avoids visual noise with generous whitespace, serene typography, and low cognitive load.",
        primaryAccent = Color(0xFF8B5CF6), // Subdued Iris
        secondaryAccent = Color(0xFF94A3B8), // Zen Slate
        backgroundBase = Color(0xFF050507), // Pure Calm Night
        surfaceBase = Color(0xFF111115), // Restrained Tile
        textPrimary = Color(0xFFEDEDED),
        textSecondary = Color(0xFF71717A)
    ),
    FOCUS(
        title = "Focus",
        tagline = "Productive, fast, functional",
        description = "Distraction-resistant dashboard prioritizing actionable tasks, calendar agenda, and command velocity.",
        primaryAccent = Color(0xFF9333EA), // Electric Purple
        secondaryAccent = Color(0xFF38BDF8), // Signal Cyan
        backgroundBase = Color(0xFF0C0A14), // High Contrast Dark
        surfaceBase = Color(0xFF161224), // Utility Surface
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF94A3B8)
    ),
    EXPRESSIVE(
        title = "Expressive",
        tagline = "Artistic, experimental, unconventional",
        description = "Unconventional visual compositions, bold oversized typography, and creative widget arrangements.",
        primaryAccent = Color(0xFFD946EF), // Radiant Magenta
        secondaryAccent = Color(0xFFA855F7), // Neon Violet
        backgroundBase = Color(0xFF0B0616), // Cyber Void
        surfaceBase = Color(0xFF201338), // Expressive Tile
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFFD8B4FE)
    )
}

/**
 * Performance and Hardware-Adaptive Quality Tier.
 * Allows The Purple Launcher to run smoothly even on low-RAM or older devices.
 */
enum class QualityTier(val label: String, val description: String) {
    LOW("Battery & Low RAM", "Disables real-time blur, uses lightweight animations and low memory footprint."),
    MEDIUM("Balanced", "Smooth spring motion with optimized surface opacities and fast rendering."),
    HIGH("Maximum Polish", "Full organic springs, subtle blur layers, and dynamic depth effects.")
}

/**
 * Custom icon shapes supported by the hybrid icon system.
 */
enum class IconShape(val label: String) {
    CIRCLE("Circle"),
    SQUIRCLE("Squircle"),
    ROUNDED_RECT("Rounded"),
    TEARDROP("Teardrop")
}

/**
 * Wallpaper style selection.
 */
enum class WallpaperPreset(val label: String, val gradientColors: List<Color>) {
    SYSTEM("System Wallpaper", listOf(Color.Transparent, Color.Transparent)),
    DARK_VOID("Obsidian Void", listOf(Color(0xFF08060C), Color(0xFF120D1D), Color(0xFF050308))),
    DEEP_PURPLE("Deep Frequency", listOf(Color(0xFF140827), Color(0xFF250F47), Color(0xFF090412))),
    MISTY_SLATE("Misty Twilight", listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF090D16))),
    COSMIC_NEON("Cosmic Neon", listOf(Color(0xFF2E0854), Color(0xFF170B3B), Color(0xFF0A0314)))
}

/**
 * Launcher Gesture Actions.
 */
enum class GestureAction(val label: String) {
    OPEN_DRAWER("Open App Drawer"),
    OPEN_SEARCH("Open Command Search"),
    SWITCH_PROFILE("Switch Personality"),
    OPEN_SETTINGS("Open Launcher Settings"),
    LOCK_DEVICE("Lock Screen (if permitted)"),
    NONE("None")
}
