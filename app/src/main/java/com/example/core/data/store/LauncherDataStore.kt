package com.example.core.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.core.model.GestureAction
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.QualityTier
import com.example.core.model.WallpaperPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Single persistent storage boundary for launcher-owned state.
 *
 * Existing SharedPreferences from the prototype is migrated automatically
 * on first DataStore access.
 */
class LauncherDataStore(
    private val context: Context
) {

    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            migrations = listOf(
                SharedPreferencesMigration(
                    context = context,
                    sharedPreferencesName = LEGACY_PREFS_NAME
                )
            ),
            produceFile = {
                context.preferencesDataStoreFile(DATASTORE_NAME)
            }
        )

    val activeProfile: Flow<LauncherProfile> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.activeProfile]
                .toEnumOrDefault(LauncherProfile.FLUID)
        }

    val qualityTier: Flow<QualityTier> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.qualityTier]
                .toEnumOrDefault(QualityTier.MEDIUM)
        }

    val wallpaperPreset: Flow<WallpaperPreset> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.wallpaperPreset]
                .toEnumOrDefault(WallpaperPreset.DEEP_PURPLE)
        }

    val iconShape: Flow<IconShape> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.iconShape]
                .toEnumOrDefault(IconShape.SQUIRCLE)
        }

    val showIconLabels: Flow<Boolean> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.showIconLabels] ?: true
        }

    val swipeUpAction: Flow<GestureAction> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.swipeUpAction]
                .toEnumOrDefault(GestureAction.OPEN_DRAWER)
        }

    val swipeDownAction: Flow<GestureAction> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.swipeDownAction]
                .toEnumOrDefault(GestureAction.OPEN_SEARCH)
        }

    val doubleTapAction: Flow<GestureAction> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.doubleTapAction]
                .toEnumOrDefault(GestureAction.SWITCH_PROFILE)
        }

    val trackAppUsage: Flow<Boolean> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.trackAppUsage] ?: true
        }

    val contextualSuggestions: Flow<Boolean> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.contextualSuggestions] ?: true
        }

    val onboardingCompleted: Flow<Boolean> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.onboardingCompleted] ?: false
        }

    val pinnedApps: Flow<Set<String>> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.pinnedApps] ?: emptySet()
        }

    val tasksJson: Flow<String?> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.tasksJson]
        }

    val homePage: Flow<Int> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.homePage] ?: 0
        }

    val appUsageJson: Flow<String?> =
        dataStore.data.safePreferences().map { preferences ->
            preferences[Keys.appUsageJson]
        }

    suspend fun setActiveProfile(profile: LauncherProfile) {
        dataStore.edit { preferences ->
            preferences[Keys.activeProfile] = profile.name
        }
    }

    suspend fun setQualityTier(tier: QualityTier) {
        dataStore.edit { preferences ->
            preferences[Keys.qualityTier] = tier.name
        }
    }

    suspend fun setWallpaperPreset(preset: WallpaperPreset) {
        dataStore.edit { preferences ->
            preferences[Keys.wallpaperPreset] = preset.name
        }
    }

    suspend fun setIconShape(shape: IconShape) {
        dataStore.edit { preferences ->
            preferences[Keys.iconShape] = shape.name
        }
    }

    suspend fun setShowIconLabels(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.showIconLabels] = show
        }
    }

    suspend fun setSwipeUpAction(action: GestureAction) {
        dataStore.edit { preferences ->
            preferences[Keys.swipeUpAction] = action.name
        }
    }

    suspend fun setSwipeDownAction(action: GestureAction) {
        dataStore.edit { preferences ->
            preferences[Keys.swipeDownAction] = action.name
        }
    }

    suspend fun setDoubleTapAction(action: GestureAction) {
        dataStore.edit { preferences ->
            preferences[Keys.doubleTapAction] = action.name
        }
    }

    suspend fun setTrackAppUsage(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.trackAppUsage] = enabled
        }
    }

    suspend fun setContextualSuggestions(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.contextualSuggestions] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.onboardingCompleted] = completed
        }
    }

    suspend fun setPinnedApps(apps: Set<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.pinnedApps] = apps
        }
    }

    suspend fun setTasksJson(json: String) {
        dataStore.edit { preferences ->
            preferences[Keys.tasksJson] = json
        }
    }

    suspend fun setHomePage(page: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.homePage] = page.coerceAtLeast(0)
        }
    }

    suspend fun setAppUsageJson(json: String) {
        dataStore.edit { preferences ->
            preferences[Keys.appUsageJson] = json
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun Flow<Preferences>.safePreferences(): Flow<Preferences> =
        catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(
        default: T
    ): T {
        if (this == null) return default

        return try {
            enumValueOf<T>(this)
        } catch (_: IllegalArgumentException) {
            default
        }
    }

    private object Keys {
        val activeProfile = stringPreferencesKey("active_profile")
        val qualityTier = stringPreferencesKey("quality_tier")
        val wallpaperPreset = stringPreferencesKey("wallpaper_preset")
        val iconShape = stringPreferencesKey("icon_shape")
        val showIconLabels = booleanPreferencesKey("show_icon_labels")

        val swipeUpAction = stringPreferencesKey("gesture_swipe_up")
        val swipeDownAction = stringPreferencesKey("gesture_swipe_down")
        val doubleTapAction = stringPreferencesKey("gesture_double_tap")

        val trackAppUsage = booleanPreferencesKey("track_app_usage")
        val contextualSuggestions =
            booleanPreferencesKey("contextual_suggestions")

        val onboardingCompleted =
            booleanPreferencesKey("onboarding_completed")

        val pinnedApps =
            stringSetPreferencesKey("pinned_apps")

        val tasksJson =
            stringPreferencesKey("tasks_json")

        val homePage =
            intPreferencesKey("home_page")

        val appUsageJson =
            stringPreferencesKey("app_usage_json")
    }

    companion object {
        private const val LEGACY_PREFS_NAME =
            "purple_launcher_prefs"

        private const val DATASTORE_NAME =
            "purple_launcher.preferences_pb"
    }
}
