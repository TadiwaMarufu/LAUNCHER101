package com.example.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Supported item types on the Now Bar.
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
 * Individual item presentable in the Now Bar.
 */
data class NowBarItem(
    val id: String,
    val type: NowBarType,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val priority: Int = 0,
    val isPinned: Boolean = true,
    val isEnabled: Boolean = true,
    val data: Any? = null
)

/**
 * Media playback state for Now Bar media card.
 */
data class MediaPlaybackState(
    val isPlaying: Boolean = false,
    val title: String = "Violet Waves",
    val artist: String = "Purple Audio Lab",
    val progress: Float = 0.42f
)

/**
 * Battery information state.
 */
data class BatteryInfoState(
    val percentage: Int = 85,
    val isCharging: Boolean = false,
    val healthText: String = "Good"
)

/**
 * Focus task item model for Focus profile and launcher widgets.
 */
data class LauncherTask(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
