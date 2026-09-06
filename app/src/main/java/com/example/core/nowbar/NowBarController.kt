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
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import com.example.core.LauncherDependencies
import com.example.core.engine.NowBarBehavior
import com.example.core.engine.ProfileEngine
import com.example.core.model.BatteryInfoState
import com.example.core.model.LauncherProfile
import com.example.core.model.MediaPlaybackState
import com.example.core.model.NowBarItem
import com.example.core.model.NowBarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central runtime controller for the launcher Now Bar.
 *
 * The controller owns runtime state and delegates personality decisions
 * to ProfileEngine. It does not persist launcher settings itself.
 */
class NowBarController private constructor(
    context: Context
) {

    private val appContext = context.applicationContext
    private val dependencies = LauncherDependencies.get(appContext)

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private val _mediaState = MutableStateFlow(
        MediaPlaybackState(
            isPlaying = false,
            title = "",
            artist = "",
            progress = 0f
        )
    )

    val mediaState: StateFlow<MediaPlaybackState> =
        _mediaState.asStateFlow()

    private val _batteryState =
        MutableStateFlow(BatteryInfoState(0, false, "Unknown"))

    val batteryState: StateFlow<BatteryInfoState> =
        _batteryState.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)

    val timerSeconds: StateFlow<Int> =
        _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)

    val isTimerRunning: StateFlow<Boolean> =
        _isTimerRunning.asStateFlow()

    private val _items =
        MutableStateFlow<List<NowBarItem>>(emptyList())

    val items: StateFlow<List<NowBarItem>> =
        _items.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    init {
        registerBatteryMonitor()

        scope.launch {
            dependencies.profileRepository.activeProfile.collect { profile ->
                recomputeItems(profile)
            }
        }

        scope.launch {
            dependencies.intelligenceRepository.contextualSuggestions.collect {
                recomputeItems(
                    dependencies.profileRepository.activeProfile.value
                )
            }
        }
    }

    private fun registerBatteryMonitor() {
        if (batteryReceiver != null) return

        val receiver = object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (intent == null) return

                val level =
                    intent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL,
                        -1
                    )

                val scale =
                    intent.getIntExtra(
                        BatteryManager.EXTRA_SCALE,
                        -1
                    )

                val status =
                    intent.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        -1
                    )

                val isCharging =
                    status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val percentage =
                    if (level >= 0 && scale > 0) {
                        (level * 100) / scale
                    } else {
                        0
                    }

                val health =
                    when {
                        percentage <= 15 -> "Low"
                        isCharging -> "Charging"
                        else -> "Good"
                    }

                _batteryState.value =
                    BatteryInfoState(
                        percentage = percentage,
                        isCharging = isCharging,
                        health = health
                    )

                recomputeItems(
                    dependencies.profileRepository.activeProfile.value
                )
            }
        }

        batteryReceiver = receiver

        try {
            appContext.registerReceiver(
                receiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (_: Exception) {
            batteryReceiver = null
        }
    }

    /**
     * Temporary compatibility API.
     *
     * Actual media control will be connected to Android's MediaSession/
     * MediaController layer rather than synthetic demo tracks.
     */
    fun togglePlayPause() {
        if (_mediaState.value.title.isBlank()) return

        _mediaState.value =
            _mediaState.value.copy(
                isPlaying = !_mediaState.value.isPlaying
            )

        recomputeItems(
            dependencies.profileRepository.activeProfile.value
        )
    }

    /**
     * Temporary compatibility API.
     *
     * Real track changes will come from the Android media session.
     */
    fun nextTrack() {
        if (_mediaState.value.title.isBlank()) return

        _mediaState.value =
            _mediaState.value.copy(
                isPlaying = true
            )

        recomputeItems(
            dependencies.profileRepository.activeProfile.value
        )
    }

    fun startOrStopTimer() {
        if (_isTimerRunning.value) {
            _isTimerRunning.value = false
            _timerSeconds.value = 0
        } else {
            _isTimerRunning.value = true
            _timerSeconds.value = 25 * 60
        }

        recomputeItems(
            dependencies.profileRepository.activeProfile.value
        )
    }

    /**
     * Rebuilds the Now Bar from runtime state and the active personality.
     *
     * Personality selection is owned by ProfileEngine rather than being
     * duplicated here.
     */
    fun recomputeItems(profile: LauncherProfile) {
        val config = ProfileEngine.getConfig(profile)

        val media = _mediaState.value
        val battery = _batteryState.value
        val timerRunning = _isTimerRunning.value
        val contextualSuggestions =
            dependencies.intelligenceRepository.contextualSuggestions.value

        val items = mutableListOf<NowBarItem>()

        items += NowBarItem(
            id = "profile_chip",
            type = NowBarType.PROFILE_CHIP,
            title = profile.title,
            subtitle = "Personality",
            icon = Icons.Rounded.Tune,
            priority = 100
        )

        /*
         * Do not surface empty/fake media content.
         * This item will become active when the Android media provider
         * supplies a real playback session.
         */
        if (media.title.isNotBlank()) {
            items += NowBarItem(
                id = "media_player",
                type = NowBarType.MEDIA_PLAYER,
                title = media.title,
                subtitle = if (media.isPlaying) {
                    "Playing • ${media.artist}"
                } else {
                    "Paused • ${media.artist}"
                },
                icon = if (media.isPlaying) {
                    Icons.Rounded.GraphicEq
                } else {
                    Icons.Rounded.PlayArrow
                },
                priority = if (media.isPlaying) 90 else 40
            )
        }

        if (_batteryState.value.percentage > 0) {
            items += NowBarItem(
                id = "battery_info",
                type = NowBarType.BATTERY_INFO,
                title = "${battery.percentage}%",
                subtitle = if (battery.isCharging) {
                    "Charging"
                } else {
                    battery.health
                },
                icon = if (battery.isCharging) {
                    Icons.Rounded.BatteryChargingFull
                } else {
                    Icons.Rounded.BatteryStd
                },
                priority = when {
                    battery.percentage < 20 -> 95
                    battery.isCharging -> 85
                    else -> 30
                }
            )
        }

        if (timerRunning) {
            items += NowBarItem(
                id = "active_timer",
                type = NowBarType.ACTIVE_TIMER,
                title = formatTimer(_timerSeconds.value),
                subtitle = "Active Focus Session",
                icon = Icons.Rounded.Timer,
                priority = 95
            )
        }

        /*
         * Contextual suggestions are deliberately lightweight.
         * The controller does not invent calendar events or fake context.
         */
        if (contextualSuggestions) {
            when (config.nowBarBehavior) {
                NowBarBehavior.ACTION_FIRST -> {
                    items += NowBarItem(
                        id = "action_search",
                        type = NowBarType.ACTION_SEARCH,
                        title = "Search",
                        subtitle = "Command Bar",
                        icon = Icons.Rounded.Search,
                        priority = 80
                    )
                }

                NowBarBehavior.AMBIENT -> {
                    items += NowBarItem(
                        id = "action_search",
                        type = NowBarType.ACTION_SEARCH,
                        title = "Search",
                        subtitle = "Command Bar",
                        icon = Icons.Rounded.Search,
                        priority = 60
                    )
                }

                NowBarBehavior.CURATED -> {
                    items += NowBarItem(
                        id = "action_drawer",
                        type = NowBarType.ACTION_DRAWER,
                        title = "Apps",
                        subtitle = "All Categories",
                        icon = Icons.Rounded.Apps,
                        priority = 70
                    )
                }

                NowBarBehavior.QUIET -> {
                    // Calm intentionally exposes less.
                }

                NowBarBehavior.EXPERIMENTAL -> {
                    items += NowBarItem(
                        id = "action_search",
                        type = NowBarType.ACTION_SEARCH,
                        title = "Search",
                        subtitle = "Explore",
                        icon = Icons.Rounded.Search,
                        priority = 65
                    )
                }
            }
        }

        /*
         * The drawer remains available as a core launcher action.
         * Its priority is influenced by personality behavior.
         */
        if (
            items.none {
                it.type == NowBarType.ACTION_DRAWER
            }
        ) {
            items += NowBarItem(
                id = "action_drawer",
                type = NowBarType.ACTION_DRAWER,
                title = "Apps",
                subtitle = "All Categories",
                icon = Icons.Rounded.Apps,
                priority = when (config.nowBarBehavior) {
                    NowBarBehavior.QUIET -> 45
                    NowBarBehavior.ACTION_FIRST -> 75
                    else -> 70
                }
            )
        }

        if (config.nowBarBehavior == NowBarBehavior.CURATED) {
            items += NowBarItem(
                id = "action_settings",
                type = NowBarType.ACTION_SETTINGS,
                title = "Settings",
                subtitle = "Launcher",
                icon = Icons.Rounded.Settings,
                priority = 20
            )
        }

        _items.value =
            items.sortedByDescending { it.priority }
    }

    private fun formatTimer(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return if (remainingSeconds == 0) {
            "${minutes}m Left"
        } else {
            "%d:%02d Left".format(
                minutes,
                remainingSeconds
            )
        }
    }

    fun destroy() {
        batteryReceiver?.let { receiver ->
            try {
                appContext.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }

        batteryReceiver = null
        scope.cancel()
    }

    companion object {

        @Volatile
        private var INSTANCE: NowBarController? = null

        fun getInstance(context: Context): NowBarController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NowBarController(
                    context.applicationContext
                ).also {
                    INSTANCE = it
                }
            }
        }
    }
}
