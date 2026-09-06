package com.example.core.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Supported item types on the Now Bar.
 *
 * The Now Bar is the launcher's primary interaction surface.
 * Providers can add contextual items without coupling the UI to
 * individual Android system services.
 */
enum class NowBarType {
    PROFILE_CHIP,
    MEDIA_PLAYER,
    BATTERY_INFO,
    ACTIVE_TIMER,
    CALENDAR_GLANCE,
    ACTION_SEARCH,
    ACTION_DRAWER,
    ACTION_SETTINGS
}

/**
 * Individual item presented by the Now Bar.
 */
data class NowBarItem(
    val id: String,
    val type: NowBarType,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val priority: Int = 0,
    val isPinned: Boolean = false,
    val isEnabled: Boolean = true,
    val data: Any? = null
)

/**
 * Real media playback state.
 *
 * Empty title/artist means no active media session is currently
 * supplying information. The launcher must never manufacture
 * playback information.
 */
data class MediaPlaybackState(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val progress: Float = 0f
)

/**
 * Battery information exposed to launcher surfaces.
 */
data class BatteryInfoState(
    val percentage: Int = 0,
    val isCharging: Boolean = false,
    val healthText: String = "Unknown"
)

/**
 * Focus task item model.
 */
data class LauncherTask(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
