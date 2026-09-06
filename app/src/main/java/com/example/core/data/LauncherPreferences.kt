package com.example.core.data

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.GestureAction
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.LauncherTask
import com.example.core.model.QualityTier
import com.example.core.model.WallpaperPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class LauncherPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("purple_launcher_prefs", Context.MODE_PRIVATE)

    // Reactive StateFlows for UI observation
    private val _activeProfile = MutableStateFlow(loadActiveProfile())
    val activeProfile: StateFlow<LauncherProfile> = _activeProfile.asStateFlow()

    private val _qualityTier = MutableStateFlow(loadQualityTier())
    val qualityTier: StateFlow<QualityTier> = _qualityTier.asStateFlow()

    private val _wallpaperPreset = MutableStateFlow(loadWallpaperPreset())
    val wallpaperPreset: StateFlow<WallpaperPreset> = _wallpaperPreset.asStateFlow()

    private val _iconShape = MutableStateFlow(loadIconShape())
    val iconShape: StateFlow<IconShape> = _iconShape.asStateFlow()

    private val _showIconLabels = MutableStateFlow(prefs.getBoolean(KEY_SHOW_ICON_LABELS, true))
    val showIconLabels: StateFlow<Boolean> = _showIconLabels.asStateFlow()

    private val _swipeUpAction = MutableStateFlow(loadGestureAction(KEY_GESTURE_SWIPE_UP, GestureAction.OPEN_DRAWER))
    val swipeUpAction: StateFlow<GestureAction> = _swipeUpAction.asStateFlow()

    private val _swipeDownAction = MutableStateFlow(loadGestureAction(KEY_GESTURE_SWIPE_DOWN, GestureAction.OPEN_SEARCH))
    val swipeDownAction: StateFlow<GestureAction> = _swipeDownAction.asStateFlow()

    private val _doubleTapAction = MutableStateFlow(loadGestureAction(KEY_GESTURE_DOUBLE_TAP, GestureAction.SWITCH_PROFILE))
    val doubleTapAction: StateFlow<GestureAction> = _doubleTapAction.asStateFlow()

    private val _trackAppUsage = MutableStateFlow(prefs.getBoolean(KEY_TRACK_APP_USAGE, true))
    val trackAppUsage: StateFlow<Boolean> = _trackAppUsage.asStateFlow()

    private val _contextualSuggestions = MutableStateFlow(prefs.getBoolean(KEY_CONTEXTUAL_SUGGESTIONS, true))
    val contextualSuggestions: StateFlow<Boolean> = _contextualSuggestions.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _pinnedApps = MutableStateFlow(loadPinnedApps())
    val pinnedApps: StateFlow<Set<String>> = _pinnedApps.asStateFlow()

    private val _tasks = MutableStateFlow(loadTasks())
    val tasks: StateFlow<List<LauncherTask>> = _tasks.asStateFlow()

    // -------------------------------------------------------------
    // Setters with persistence
    // -------------------------------------------------------------

    fun setActiveProfile(profile: LauncherProfile) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE, profile.name).apply()
        _activeProfile.value = profile
    }

    fun cycleNextProfile(): LauncherProfile {
        val profiles = LauncherProfile.values()
        val nextIndex = (activeProfile.value.ordinal + 1) % profiles.size
        val nextProfile = profiles[nextIndex]
        setActiveProfile(nextProfile)
        return nextProfile
    }

    fun setQualityTier(tier: QualityTier) {
        prefs.edit().putString(KEY_QUALITY_TIER, tier.name).apply()
        _qualityTier.value = tier
    }

    fun setWallpaperPreset(preset: WallpaperPreset) {
        prefs.edit().putString(KEY_WALLPAPER_PRESET, preset.name).apply()
        _wallpaperPreset.value = preset
    }

    fun setIconShape(shape: IconShape) {
        prefs.edit().putString(KEY_ICON_SHAPE, shape.name).apply()
        _iconShape.value = shape
    }

    fun setShowIconLabels(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_ICON_LABELS, show).apply()
        _showIconLabels.value = show
    }

    fun setGestureSwipeUp(action: GestureAction) {
        prefs.edit().putString(KEY_GESTURE_SWIPE_UP, action.name).apply()
        _swipeUpAction.value = action
    }

    fun setGestureSwipeDown(action: GestureAction) {
        prefs.edit().putString(KEY_GESTURE_SWIPE_DOWN, action.name).apply()
        _swipeDownAction.value = action
    }

    fun setGestureDoubleTap(action: GestureAction) {
        prefs.edit().putString(KEY_GESTURE_DOUBLE_TAP, action.name).apply()
        _doubleTapAction.value = action
    }

    fun setTrackAppUsage(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRACK_APP_USAGE, enabled).apply()
        _trackAppUsage.value = enabled
    }

    fun setContextualSuggestions(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONTEXTUAL_SUGGESTIONS, enabled).apply()
        _contextualSuggestions.value = enabled
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    fun toggleAppPin(componentName: String) {
        val current = _pinnedApps.value.toMutableSet()
        if (current.contains(componentName)) {
            current.remove(componentName)
        } else {
            current.add(componentName)
        }
        prefs.edit().putStringSet(KEY_PINNED_APPS, current).apply()
        _pinnedApps.value = current
    }

    fun addTask(text: String) {
        val newTask = LauncherTask(
            id = System.currentTimeMillis().toString(),
            text = text,
            isCompleted = false
        )
        val updated = listOf(newTask) + _tasks.value
        saveTasks(updated)
    }

    fun toggleTask(id: String) {
        val updated = _tasks.value.map { task ->
            if (task.id == id) task.copy(isCompleted = !task.isCompleted) else task
        }
        saveTasks(updated)
    }

    fun removeTask(id: String) {
        val updated = _tasks.value.filterNot { it.id == id }
        saveTasks(updated)
    }

    // -------------------------------------------------------------
    // Private Loaders
    // -------------------------------------------------------------

    private fun loadActiveProfile(): LauncherProfile {
        val name = prefs.getString(KEY_ACTIVE_PROFILE, LauncherProfile.FLUID.name)
        return try {
            LauncherProfile.valueOf(name ?: LauncherProfile.FLUID.name)
        } catch (_: Exception) {
            LauncherProfile.FLUID
        }
    }

    private fun loadQualityTier(): QualityTier {
        val name = prefs.getString(KEY_QUALITY_TIER, QualityTier.MEDIUM.name)
        return try {
            QualityTier.valueOf(name ?: QualityTier.MEDIUM.name)
        } catch (_: Exception) {
            QualityTier.MEDIUM
        }
    }

    private fun loadWallpaperPreset(): WallpaperPreset {
        val name = prefs.getString(KEY_WALLPAPER_PRESET, WallpaperPreset.DEEP_PURPLE.name)
        return try {
            WallpaperPreset.valueOf(name ?: WallpaperPreset.DEEP_PURPLE.name)
        } catch (_: Exception) {
            WallpaperPreset.DEEP_PURPLE
        }
    }

    private fun loadIconShape(): IconShape {
        val name = prefs.getString(KEY_ICON_SHAPE, IconShape.SQUIRCLE.name)
        return try {
            IconShape.valueOf(name ?: IconShape.SQUIRCLE.name)
        } catch (_: Exception) {
            IconShape.SQUIRCLE
        }
    }

    private fun loadGestureAction(key: String, default: GestureAction): GestureAction {
        val name = prefs.getString(key, default.name)
        return try {
            GestureAction.valueOf(name ?: default.name)
        } catch (_: Exception) {
            default
        }
    }

    private fun loadPinnedApps(): Set<String> {
        return prefs.getStringSet(KEY_PINNED_APPS, emptySet()) ?: emptySet()
    }

    private fun loadTasks(): List<LauncherTask> {
        val raw = prefs.getString(KEY_TASKS_JSON, null) ?: return listOf(
            LauncherTask("1", "Experience all 5 personalities", true),
            LauncherTask("2", "Test horizontal category swipe", false),
            LauncherTask("3", "Pin favorite apps to Now Bar", false)
        )
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<LauncherTask>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LauncherTask(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveTasks(tasks: List<LauncherTask>) {
        try {
            val array = JSONArray()
            for (task in tasks) {
                val obj = JSONObject()
                obj.put("id", task.id)
                obj.put("text", task.text)
                obj.put("isCompleted", task.isCompleted)
                obj.put("timestamp", task.timestamp)
                array.put(obj)
            }
            prefs.edit().putString(KEY_TASKS_JSON, array.toString()).apply()
            _tasks.value = tasks
        } catch (_: Exception) {
            _tasks.value = tasks
        }
    }

    companion object {
        private const val KEY_ACTIVE_PROFILE = "active_profile"
        private const val KEY_QUALITY_TIER = "quality_tier"
        private const val KEY_WALLPAPER_PRESET = "wallpaper_preset"
        private const val KEY_ICON_SHAPE = "icon_shape"
        private const val KEY_SHOW_ICON_LABELS = "show_icon_labels"
        private const val KEY_GESTURE_SWIPE_UP = "gesture_swipe_up"
        private const val KEY_GESTURE_SWIPE_DOWN = "gesture_swipe_down"
        private const val KEY_GESTURE_DOUBLE_TAP = "gesture_double_tap"
        private const val KEY_TRACK_APP_USAGE = "track_app_usage"
        private const val KEY_CONTEXTUAL_SUGGESTIONS = "contextual_suggestions"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_PINNED_APPS = "pinned_apps"
        private const val KEY_TASKS_JSON = "tasks_json"

        @Volatile
        private var INSTANCE: LauncherPreferences? = null

        fun getInstance(context: Context): LauncherPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LauncherPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
