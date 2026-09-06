package com.example.core.nowbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.mutableStateOf
import com.example.core.LauncherDependencies
import com.example.core.model.BatteryInfoState
import com.example.core.model.LauncherProfile
import com.example.core.model.MediaPlaybackState
import com.example.core.model.NowBarItem
import com.example.core.model.NowBarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central controller for the Purple Launcher Now Bar.
 *
 * The Now Bar is intentionally treated as a first-class launcher system,
 * rather than a hard-coded dock.
 *
 * Responsibilities:
 * - observe active profile
 * - observe intelligence preferences
 * - observe battery state
 * - maintain media state
 * - maintain timer state
 * - derive contextual Now Bar items
 * - expose stable StateFlows to UI
 *
 * UI is not responsible for deciding which Now Bar items should exist.
 */
class NowBarController private constructor(
    context: Context
) {

    private val appContext = context.applicationContext
    private val dependencies = LauncherDependencies.get(appContext)

    private val controllerScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _items = MutableStateFlow<List<NowBarItem>>(emptyList())
    val items: StateFlow<List<NowBarItem>> = _items.asStateFlow()

    private val _mediaState =
        MutableStateFlow(MediaPlaybackState())
    val mediaState: StateFlow<MediaPlaybackState> =
        _mediaState.asStateFlow()

    private val _batteryState =
        MutableStateFlow(BatteryInfoState())
    val batteryState: StateFlow<BatteryInfoState> =
        _batteryState.asStateFlow()

    private val _timerSeconds =
        MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> =
        _timerSeconds.asStateFlow()

    private val _isTimerRunning =
        MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> =
        _isTimerRunning.asStateFlow()

    /**
     * These are deliberately local snapshots of repository Flows.
     *
     * ProfileRepository and IntelligenceRepository expose Flow rather
     * than StateFlow, so the controller must not access `.value`.
     */
    private var currentProfile: LauncherProfile = LauncherProfile.FLUID
    private var contextualSuggestionsEnabled: Boolean = true

    private var timerJob: Job? = null

    init {
        observeRepositories()
        registerBatteryReceiver()
        recomputeItems()
    }

    private fun observeRepositories() {
        controllerScope.launch {
            dependencies.profileRepository.activeProfile.collect { profile ->
                currentProfile = profile
                recomputeItems()
            }
        }

        controllerScope.launch {
            dependencies.intelligenceRepository.contextualSuggestions.collect { enabled ->
                contextualSuggestionsEnabled = enabled
                recomputeItems()
            }
        }
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return

                val level = intent.getIntExtra(
                    BatteryManager.EXTRA_LEVEL,
                    0
                )

                val scale = intent.getIntExtra(
                    BatteryManager.EXTRA_SCALE,
                    100
                )

                val percentage =
                    if (scale > 0) {
                        ((level.toFloat() / scale.toFloat()) * 100f)
                            .toInt()
                            .coerceIn(0, 100)
                    } else {
                        0
                    }

                val status = intent.getIntExtra(
                    BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN
                )

                val charging =
                    status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val health = when (
                    intent.getIntExtra(
                        BatteryManager.EXTRA_HEALTH,
                        BatteryManager.BATTERY_HEALTH_UNKNOWN
                    )
                ) {
                    BatteryManager.BATTERY_HEALTH_GOOD ->
                        "Good"

                    BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                        "Overheat"

                    BatteryManager.BATTERY_HEALTH_DEAD ->
                        "Dead"

                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                        "Over-voltage"

                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE ->
                        "Failure"

                    else ->
                        "Unknown"
                }

                _batteryState.value = BatteryInfoState(
                    percentage = percentage,
                    isCharging = charging,
                    healthText = health
                )

                recomputeItems()
            }
        }

        appContext.registerReceiver(receiver, filter)
    }

    fun setMediaState(
        isPlaying: Boolean,
        title: String,
        artist: String = "",
        progress: Float = 0f
    ) {
        _mediaState.value = MediaPlaybackState(
            isPlaying = isPlaying,
            title = title,
            artist = artist,
            progress = progress.coerceIn(0f, 1f)
        )

        recomputeItems()
    }

    fun clearMedia() {
        _mediaState.value = MediaPlaybackState()
        recomputeItems()
    }

    fun togglePlayPause() {
        toggleMediaPlayback()
    }

    fun nextTrack() {
        /*
         * Track navigation is provider-dependent. The v0.1 controller
         * intentionally does not fabricate a media queue. If a future
         * media provider supplies queue information, this method becomes
         * the single controller boundary for advancing the queue.
         */
        if (_mediaState.value.title.isBlank()) return

        _mediaState.value = _mediaState.value.copy(
            progress = 0f
        )

        recomputeItems()
    }

    fun toggleMediaPlayback() {
        if (_mediaState.value.title.isBlank()) return

        _mediaState.value = _mediaState.value.copy(
            isPlaying = !_mediaState.value.isPlaying
        )

        recomputeItems()
    }

    fun startTimer(minutes: Int = 25) {
        val safeMinutes = minutes.coerceIn(1, 24 * 60)

        timerJob?.cancel()

        _isTimerRunning.value = true
        _timerSeconds.value = safeMinutes * 60

        timerJob = controllerScope.launch {
            while (_isTimerRunning.value && _timerSeconds.value > 0) {
                delay(1_000)
                _timerSeconds.value =
                    (_timerSeconds.value - 1).coerceAtLeast(0)
            }

            if (_timerSeconds.value == 0) {
                _isTimerRunning.value = false
            }

            recomputeItems()
        }

        recomputeItems()
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null

        _isTimerRunning.value = false
        _timerSeconds.value = 0

        recomputeItems()
    }

    fun onNowBarItemClicked(
        item: NowBarItem,
        context: Context,
        onOpenDrawer: (() -> Unit)? = null,
        onOpenSearch: (() -> Unit)? = null,
        onOpenSettings: (() -> Unit)? = null,
        onCycleProfile: (() -> Unit)? = null
    ) {
        when (item.type) {
            NowBarType.ACTION_DRAWER ->
                onOpenDrawer?.invoke()

            NowBarType.ACTION_SEARCH ->
                onOpenSearch?.invoke()

            NowBarType.ACTION_SETTINGS ->
                onOpenSettings?.invoke()

            NowBarType.PROFILE_CHIP ->
                onCycleProfile?.invoke()

            NowBarType.MEDIA_PLAYER ->
                toggleMediaPlayback()

            NowBarType.ACTIVE_TIMER -> {
                if (_isTimerRunning.value) {
                    stopTimer()
                }
            }

            NowBarType.BATTERY_INFO,
            NowBarType.CALENDAR_GLANCE -> {
                // Informational items currently have no direct action.
            }
        }
    }

    private fun recomputeItems() {
        val profile = currentProfile
        val battery = _batteryState.value
        val media = _mediaState.value
        val timerRunning = _isTimerRunning.value
        val timerSeconds = _timerSeconds.value

        val result = mutableListOf<NowBarItem>()

        /*
         * Every personality gets a profile identity item.
         * The presentation layer decides how prominently this appears.
         */
        result += NowBarItem(
            id = "profile",
            type = NowBarType.PROFILE_CHIP,
            title = profile.title,
            subtitle = profileSubtitle(profile),
            icon = Icons.Default.Tune,
            priority = 100,
            isPinned = true
        )

        /*
         * Media is genuinely contextual: it only appears when media
         * information exists.
         */
        if (media.title.isNotBlank()) {
            result += NowBarItem(
                id = "media",
                type = NowBarType.MEDIA_PLAYER,
                title = media.title,
                subtitle = media.artist.ifBlank { null },
                icon = Icons.Default.MusicNote,
                priority = 95
            )
        }

        if (timerRunning && timerSeconds > 0) {
            result += NowBarItem(
                id = "timer",
                type = NowBarType.ACTIVE_TIMER,
                title = formatTimer(timerSeconds),
                subtitle = "Timer",
                icon = Icons.Default.Alarm,
                priority = 90,
                isPinned = true
            )
        }

        /*
         * Battery remains useful across all personalities, but its
         * importance varies with the profile.
         */
        if (shouldShowBattery(profile)) {
            val chargingText =
                if (battery.isCharging) "Charging" else "Battery"

            result += NowBarItem(
                id = "battery",
                type = NowBarType.BATTERY_INFO,
                title = "${battery.percentage}%",
                subtitle = chargingText,
                icon = Icons.Default.BatteryFull,
                priority = batteryPriority(profile)
            )
        }

        /*
         * Contextual suggestions are explicitly user-controlled.
         */
        if (contextualSuggestionsEnabled) {
            result += contextualActions(profile)
        }

        /*
         * Core navigation remains available even when contextual
         * intelligence is disabled.
         */
        result += NowBarItem(
            id = "search",
            type = NowBarType.ACTION_SEARCH,
            title = "Search",
            subtitle = null,
            icon = Icons.Default.Search,
            priority = 50
        )

        result += NowBarItem(
            id = "drawer",
            type = NowBarType.ACTION_DRAWER,
            title = "Apps",
            subtitle = null,
            icon = Icons.Default.Apps,
            priority = 45
        )

        result += NowBarItem(
            id = "settings",
            type = NowBarType.ACTION_SETTINGS,
            title = "Settings",
            subtitle = null,
            icon = Icons.Default.Settings,
            priority = 40
        )

        _items.value =
            result
                .filter { it.isEnabled }
                .sortedByDescending { it.priority }
    }

    private fun contextualActions(
        profile: LauncherProfile
    ): List<NowBarItem> {
        return when (profile) {
            LauncherProfile.FLUID -> {
                listOf(
                    NowBarItem(
                        id = "fluid_search",
                        type = NowBarType.ACTION_SEARCH,
                        title = "Explore",
                        subtitle = "Quick search",
                        icon = Icons.Default.Search,
                        priority = 65
                    )
                )
            }

            LauncherProfile.PREMIUM -> {
                listOf(
                    NowBarItem(
                        id = "premium_settings",
                        type = NowBarType.ACTION_SETTINGS,
                        title = "Personalize",
                        subtitle = "Launcher settings",
                        icon = Icons.Default.Tune,
                        priority = 65
                    )
                )
            }

            LauncherProfile.CALM -> {
                emptyList()
            }

            LauncherProfile.FOCUS -> {
                listOf(
                    NowBarItem(
                        id = "focus_timer",
                        type = NowBarType.ACTIVE_TIMER,
                        title = "Start Focus",
                        subtitle = "25 min",
                        icon = Icons.Default.Alarm,
                        priority = 80
                    )
                )
            }

            LauncherProfile.EXPRESSIVE -> {
                listOf(
                    NowBarItem(
                        id = "expressive_search",
                        type = NowBarType.ACTION_SEARCH,
                        title = "Create",
                        subtitle = "Search & explore",
                        icon = Icons.Default.Search,
                        priority = 70
                    )
                )
            }
        }
    }

    private fun shouldShowBattery(
        profile: LauncherProfile
    ): Boolean {
        return when (profile) {
            LauncherProfile.FLUID,
            LauncherProfile.PREMIUM,
            LauncherProfile.FOCUS,
            LauncherProfile.EXPRESSIVE -> true

            LauncherProfile.CALM ->
                _batteryState.value.percentage <= 20 ||
                    _batteryState.value.isCharging
        }
    }

    private fun batteryPriority(
        profile: LauncherProfile
    ): Int {
        return when (profile) {
            LauncherProfile.FOCUS -> 85
            LauncherProfile.FLUID -> 70
            LauncherProfile.PREMIUM -> 60
            LauncherProfile.EXPRESSIVE -> 55
            LauncherProfile.CALM -> 80
        }
    }

    private fun profileSubtitle(
        profile: LauncherProfile
    ): String {
        return when (profile) {
            LauncherProfile.FLUID ->
                "Alive & responsive"

            LauncherProfile.PREMIUM ->
                "Refined & precise"

            LauncherProfile.CALM ->
                "Quiet & minimal"

            LauncherProfile.FOCUS ->
                "Fast & productive"

            LauncherProfile.EXPRESSIVE ->
                "Creative & experimental"
        }
    }

    private fun formatTimer(
        totalSeconds: Int
    ): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "%02d:%02d".format(
            minutes,
            seconds
        )
    }

    companion object {
        @Volatile
        private var instance: NowBarController? = null

        fun getInstance(
            context: Context
        ): NowBarController {
            return instance ?: synchronized(this) {
                instance ?: NowBarController(
                    context.applicationContext
                ).also {
                    instance = it
                }
            }
        }
    }
}
