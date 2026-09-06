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
    val wallpaperPreset: WallpaperPreset =
        WallpaperPreset.DEEP_PURPLE,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val showIconLabels: Boolean = true,
    val trackAppUsage: Boolean = true,
    val contextualSuggestions: Boolean = true
)

private data class AppearanceSettings(
    val qualityTier: QualityTier,
    val wallpaperPreset: WallpaperPreset,
    val iconShape: IconShape,
    val showIconLabels: Boolean
)

private data class IntelligenceSettings(
    val trackAppUsage: Boolean,
    val contextualSuggestions: Boolean
)

class SettingsViewModel(
    private val dependencies: LauncherDependencies
) : ViewModel() {

    private val profile =
        dependencies.profileRepository.activeProfile

    private val appearance =
        combine(
            dependencies.appearanceRepository.qualityTier,
            dependencies.appearanceRepository.wallpaperPreset,
            dependencies.appearanceRepository.iconShape,
            dependencies.appearanceRepository.showIconLabels
        ) {
            qualityTier,
            wallpaperPreset,
            iconShape,
            showIconLabels ->

            AppearanceSettings(
                qualityTier = qualityTier,
                wallpaperPreset = wallpaperPreset,
                iconShape = iconShape,
                showIconLabels = showIconLabels
            )
        }

    private val intelligence =
        combine(
            dependencies.intelligenceRepository.trackAppUsage,
            dependencies.intelligenceRepository.contextualSuggestions
        ) {
            trackAppUsage,
            contextualSuggestions ->

            IntelligenceSettings(
                trackAppUsage = trackAppUsage,
                contextualSuggestions = contextualSuggestions
            )
        }

    val uiState: StateFlow<SettingsUiState> =
        combine(
            profile,
            appearance,
            intelligence
        ) {
            activeProfile,
            appearanceSettings,
            intelligenceSettings ->

            SettingsUiState(
                activeProfile = activeProfile,
                qualityTier =
                    appearanceSettings.qualityTier,
                wallpaperPreset =
                    appearanceSettings.wallpaperPreset,
                iconShape =
                    appearanceSettings.iconShape,
                showIconLabels =
                    appearanceSettings.showIconLabels,
                trackAppUsage =
                    intelligenceSettings.trackAppUsage,
                contextualSuggestions =
                    intelligenceSettings.contextualSuggestions
            )
        }.stateIn(
            scope = viewModelScope,
            started =
                SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setProfile(
        profile: LauncherProfile
    ) {
        viewModelScope.launch {
            dependencies.profileRepository
                .setProfile(profile)
        }
    }

    fun setQualityTier(
        tier: QualityTier
    ) {
        viewModelScope.launch {
            dependencies.appearanceRepository
                .setQualityTier(tier)
        }
    }

    fun setWallpaperPreset(
        preset: WallpaperPreset
    ) {
        viewModelScope.launch {
            dependencies.appearanceRepository
                .setWallpaperPreset(preset)
        }
    }

    fun setIconShape(
        shape: IconShape
    ) {
        viewModelScope.launch {
            dependencies.appearanceRepository
                .setIconShape(shape)
        }
    }

    fun setShowIconLabels(
        show: Boolean
    ) {
        viewModelScope.launch {
            dependencies.appearanceRepository
                .setShowIconLabels(show)
        }
    }

    fun setTrackAppUsage(
        enabled: Boolean
    ) {
        viewModelScope.launch {
            dependencies.intelligenceRepository
                .setTrackAppUsage(enabled)
        }
    }

    fun setContextualSuggestions(
        enabled: Boolean
    ) {
        viewModelScope.launch {
            dependencies.intelligenceRepository
                .setContextualSuggestions(enabled)
        }
    }
}

class SettingsViewModelFactory(
    private val dependencies: LauncherDependencies
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                SettingsViewModel::class.java
            )
        ) {
            return SettingsViewModel(
                dependencies
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
