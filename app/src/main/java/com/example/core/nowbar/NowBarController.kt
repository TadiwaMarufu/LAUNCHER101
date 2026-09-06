package com.example.core.nowbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import com.example.core.data.LauncherPreferences
import com.example.core.model.BatteryInfoState
import com.example.core.model.LauncherProfile
import com.example.core.model.MediaPlaybackState
import com.example.core.model.NowBarItem
import com.example.core.model.NowBarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NowBarController private constructor(private val context: Context) {

    private val preferences = LauncherPreferences.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _mediaState = MutableStateFlow(
        MediaPlaybackState(
            isPlaying = false,
            title = "Frequency Flow",
            artist = "Purple Soundscape",
            progress = 0.35f
        )
    )
    val mediaState: StateFlow<MediaPlaybackState> = _mediaState.asStateFlow()

    private val _batteryState = MutableStateFlow(BatteryInfoState(85, false, "Good"))
    val batteryState: StateFlow<BatteryInfoState> = _batteryState.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _items = MutableStateFlow<List<NowBarItem>>(emptyList())
    val items: StateFlow<List<NowBarItem>> = _items.asStateFlow()

    init {
        registerBatteryMonitor()
        recomputeItems(preferences.activeProfile.value)

        // Observe profile changes to adapt Now Bar items
        scope.launch {
            preferences.activeProfile.collect { profile ->
                recomputeItems(profile)
            }
        }
    }

    private fun registerBatteryMonitor() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    intent?.let {
                        val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL
                        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
                        _batteryState.value = BatteryInfoState(pct, isCharging, "Good")
                        recomputeItems(preferences.activeProfile.value)
                    }
                }
            }
            context.registerReceiver(receiver, filter)
        } catch (_: Exception) {
            // Safe fallback if context prevents receiver registration
        }
    }

    fun togglePlayPause() {
        val current = _mediaState.value
        _mediaState.value = current.copy(isPlaying = !current.isPlaying)
        recomputeItems(preferences.activeProfile.value)
    }

    fun nextTrack() {
        val tracks = listOf(
            "Frequency Flow" to "Purple Soundscape",
            "Midnight Drift" to "Obsidian Echo",
            "Neon Horizon" to "Hyper Violet",
            "Zen Garden" to "Quiet Mind"
        )
        val currentIdx = tracks.indexOfFirst { it.first == _mediaState.value.title }
        val nextIdx = (currentIdx + 1) % tracks.size
        val next = tracks[nextIdx]
        _mediaState.value = _mediaState.value.copy(
            title = next.first,
            artist = next.second,
            isPlaying = true
        )
        recomputeItems(preferences.activeProfile.value)
    }

    fun startOrStopTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
        if (_isTimerRunning.value) {
            _timerSeconds.value = 25 * 60 // 25 min pomodoro by default
        }
        recomputeItems(preferences.activeProfile.value)
    }

    fun recomputeItems(profile: LauncherProfile) {
        val media = _mediaState.value
        val battery = _batteryState.value

        val profileChip = NowBarItem(
            id = "profile_chip",
            type = NowBarType.PROFILE_CHIP,
            title = profile.title,
            subtitle = "Personality",
            icon = Icons.Rounded.Tune,
            priority = 100
        )

        val mediaItem = NowBarItem(
            id = "media_player",
            type = NowBarType.MEDIA_PLAYER,
            title = media.title,
            subtitle = if (media.isPlaying) "Playing • ${media.artist}" else "Paused • ${media.artist}",
            icon = if (media.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
            priority = if (media.isPlaying) 90 else 40
        )

        val batteryItem = NowBarItem(
            id = "battery_info",
            type = NowBarType.BATTERY_INFO,
            title = "${battery.percentage}%",
            subtitle = if (battery.isCharging) "Charging" else "Battery",
            icon = if (battery.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
            priority = if (battery.percentage < 20 || battery.isCharging) 85 else 30
        )

        val timerItem = NowBarItem(
            id = "active_timer",
            type = NowBarType.ACTIVE_TIMER,
            title = if (_isTimerRunning.value) "${_timerSeconds.value / 60}m Left" else "Focus Timer",
            subtitle = if (_isTimerRunning.value) "Active Focus Session" else "Tap to start",
            icon = Icons.Rounded.Timer,
            priority = if (_isTimerRunning.value) 95 else 20
        )

        val calendarItem = NowBarItem(
            id = "calendar_glance",
            type = NowBarType.CALENDAR_GLANCE,
            title = "Next: Creative Flow",
            subtitle = "in 45m",
            icon = Icons.Rounded.CalendarToday,
            priority = 50
        )

        val searchItem = NowBarItem(
            id = "action_search",
            type = NowBarType.ACTION_SEARCH,
            title = "Search",
            subtitle = "Command Bar",
            icon = Icons.Rounded.Search,
            priority = 60
        )

        val drawerItem = NowBarItem(
            id = "action_drawer",
            type = NowBarType.ACTION_DRAWER,
            title = "Apps",
            subtitle = "All Categories",
            icon = Icons.Rounded.Apps,
            priority = 70
        )

        val settingsItem = NowBarItem(
            id = "action_settings",
            type = NowBarType.ACTION_SETTINGS,
            title = "Settings",
            subtitle = "Launcher",
            icon = Icons.Rounded.Settings,
            priority = 10
        )

        // Profile-specific selection and order
        val profileItems = when (profile) {
            LauncherProfile.FLUID -> listOf(
                profileChip,
                mediaItem,
                batteryItem,
                searchItem,
                drawerItem
            )
            LauncherProfile.PREMIUM -> listOf(
                profileChip,
                mediaItem,
                batteryItem,
                drawerItem,
                settingsItem
            )
            LauncherProfile.CALM -> listOf(
                profileChip,
                batteryItem,
                drawerItem
            )
            LauncherProfile.FOCUS -> listOf(
                profileChip,
                timerItem,
                calendarItem,
                searchItem,
                drawerItem
            )
            LauncherProfile.EXPRESSIVE -> listOf(
                profileChip,
                mediaItem,
                searchItem,
                drawerItem,
                batteryItem
            )
        }

        _items.value = profileItems.sortedByDescending { it.priority }
    }

    companion object {
        @Volatile
        private var INSTANCE: NowBarController? = null

        fun getInstance(context: Context): NowBarController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NowBarController(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
