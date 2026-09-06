package com.example.core.model

/**
 * Purple Launcher personalities.
 *
 * The product name "Purple Launcher" is not a color requirement.
 * Profiles describe how the launcher behaves and feels.
 */
enum class LauncherProfile(
    val title: String,
    val tagline: String,
    val description: String
) {
    FLUID(
        title = "Fluid",
        tagline = "Alive, dynamic, soft, flowing",
        description = "A responsive spatial environment with flowing transitions and contextual interaction."
    ),

    PREMIUM(
        title = "Premium",
        tagline = "Refined, elegant, precise",
        description = "A carefully composed experience built around hierarchy, spacing, typography and precision."
    ),

    CALM(
        title = "Calm",
        tagline = "Quiet, minimal, peaceful",
        description = "A low-noise environment that gives the user's content and space room to breathe."
    ),

    FOCUS(
        title = "Focus",
        tagline = "Fast, direct, purposeful",
        description = "A productivity-oriented experience optimized for quick access and reduced distraction."
    ),

    EXPRESSIVE(
        title = "Expressive",
        tagline = "Artistic, unconventional, yours",
        description = "A flexible playground for unusual compositions, typography and personal expression."
    )
}

/**
 * Hardware-adaptive rendering quality.
 */
enum class QualityTier(
    val label: String,
    val description: String
) {
    LOW(
        "Low",
        "Prioritizes battery life, memory usage and predictable rendering."
    ),

    MEDIUM(
        "Balanced",
        "Balances responsiveness, visual quality and resource usage."
    ),

    HIGH(
        "High",
        "Allows richer motion and visual effects on capable hardware."
    )
}

/**
 * Icon treatment belongs to Appearance, not personality.
 */
enum class IconShape(val label: String) {
    SYSTEM("System"),
    CIRCLE("Circle"),
    SQUIRCLE("Squircle"),
    ROUNDED("Rounded"),
    TEARDROP("Teardrop")
}

/**
 * Wallpaper treatment belongs to Appearance.
 */
enum class WallpaperPreset(val label: String) {
    SYSTEM("System Wallpaper"),
    USER("User Wallpaper"),
    BLURRED("Blurred"),
    DIMMED("Dimmed")
}

/**
 * Launcher-level gesture actions.
 */
enum class GestureAction(val label: String) {
    OPEN_DRAWER("Open App Drawer"),
    OPEN_SEARCH("Open Search"),
    SWITCH_PROFILE("Switch Profile"),
    OPEN_SETTINGS("Open Settings"),
    OPEN_NOTIFICATIONS("Open Notifications"),
    LOCK_DEVICE("Lock Device"),
    NONE("None")
)
