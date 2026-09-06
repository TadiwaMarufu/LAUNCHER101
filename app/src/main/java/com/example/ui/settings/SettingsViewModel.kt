package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.LauncherDependencies
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.QualityTier
import com.example.core.model.WallpaperPreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeProfile: LauncherProfile = LauncherProfile.FLUID,
    val qualityTier: QualityTier = QualityTier.MEDIUM,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.DEEP_PURPLE,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val showIconLabels: Boolean = true,
    val trackAppUsage: Boolean = true,
    val contextualSuggestions: Boolean = true
)

class SettingsViewModel(
    private val dependencies: LauncherDependencies
) : ViewModel() {

    private val profile = dependencies.profileRepository.activeProfile
    private val quality = dependencies.appearanceRepository.qualityTier
    private val wallpaper = dependencies.appearanceRepository.wallpaperPreset
    private val iconShape = dependencies.appearanceRepository.iconShape
    private val iconLabels = dependencies.appearanceRepository.showIconLabels

    private val trackUsage =
        dependencies.intelligenceRepository.trackAppUsage

    private val suggestions =
        dependencies.intelligenceRepository.contextualSuggestions

    val uiState: StateFlow<SettingsUiState> =
        combine(
            profile,
            quality,
            wallpaper,
            iconShape,
            iconLabels,
            trackUsage,
            suggestions
        ) {
                activeProfile,
                qualityTier,
                wallpaperPreset,
                iconShape,
                showIconLabels,
                trackAppUsage,
                contextualSuggestions ->

            SettingsUiState(
                activeProfile = activeProfile,
                qualityTier = qualityTier,
                wallpaperPreset = wallpaperPreset,
                iconShape = iconShape,
                showIconLabels = showIconLabels,
                trackAppUsage = trackAppUsage,
                contextualSuggestions = contextualSuggestions
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setProfile(profile: LauncherProfile) {
        viewModelScope.launch {
            dependencies.profileRepository.setProfile(profile)
        }
    }

    fun setQualityTier(tier: QualityTier) {
        viewModelScope.launch {
            dependencies.appearanceRepository.setQualityTier(tier)
        }
    }

    fun setWallpaperPreset(preset: WallpaperPreset) {
        viewModelScope.launch {
            dependencies.appearanceRepository.setWallpaperPreset(preset)
        }
    }

    fun setIconShape(shape: IconShape) {
        viewModelScope.launch {
            dependencies.appearanceRepository.setIconShape(shape)
        }
    }

    fun setShowIconLabels(show: Boolean) {
        viewModelScope.launch {
            dependencies.appearanceRepository.setShowIconLabels(show)
        }
    }

    fun setTrackAppUsage(enabled: Boolean) {
        viewModelScope.launch {
            dependencies.intelligenceRepository.setTrackAppUsage(enabled)
        }
    }

    fun setContextualSuggestions(enabled: Boolean) {
        viewModelScope.launch {
            dependencies.intelligenceRepository.setContextualSuggestions(enabled)
        }
    }
}

class SettingsViewModelFactory(
    private val dependencies: LauncherDependencies
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(dependencies) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
