package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.LauncherDependencies
import com.example.core.apps.AppManager
import com.example.core.data.repository.LauncherContentRepository
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.LauncherTask
import com.example.core.model.WallpaperPreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeProfile: LauncherProfile = LauncherProfile.FLUID,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.DEEP_PURPLE,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val showIconLabels: Boolean = true,
    val tasks: List<LauncherTask> = emptyList()
)

class HomeViewModel(
    private val dependencies: LauncherDependencies
) : ViewModel() {

    val appManager: AppManager =
        dependencies.appManager

    val contentRepository: LauncherContentRepository =
        dependencies.launcherContentRepository

    val recentlyLaunched =
        appManager.recentlyLaunched

    val frequentlyLaunched =
        appManager.frequentlyLaunched

    val usageTrackingEnabled =
        dependencies.intelligenceRepository.trackAppUsage

    private val profile =
        dependencies.profileRepository.activeProfile

    private val wallpaper =
        dependencies.appearanceRepository.wallpaperPreset

    private val iconShapeFlow =
        dependencies.appearanceRepository.iconShape

    private val iconLabels =
        dependencies.appearanceRepository.showIconLabels

    private val tasks =
        contentRepository.tasks

    val uiState: StateFlow<HomeUiState> =
        combine(
            profile,
            wallpaper,
            iconShapeFlow,
            iconLabels,
            tasks
        ) { activeProfile,
            wallpaperPreset,
            iconShape,
            showIconLabels,
            taskList ->

            HomeUiState(
                activeProfile = activeProfile,
                wallpaperPreset = wallpaperPreset,
                iconShape = iconShape,
                showIconLabels = showIconLabels,
                tasks = taskList
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun cycleProfile() {
        viewModelScope.launch {
            dependencies.profileRepository.cycleProfile()
        }
    }

    fun setProfile(profile: LauncherProfile) {
        viewModelScope.launch {
            dependencies.profileRepository.setProfile(profile)
        }
    }

    fun toggleAppPin(componentName: String) {
        viewModelScope.launch {
            contentRepository.toggleAppPin(componentName)
            appManager.loadInstalledApps()
        }
    }

    fun addTask(text: String) {
        viewModelScope.launch {
            contentRepository.addTask(text)
        }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch {
            contentRepository.toggleTask(id)
        }
    }

    fun removeTask(id: String) {
        viewModelScope.launch {
            contentRepository.removeTask(id)
        }
    }

    fun launchApp(
        context: android.content.Context,
        app: com.example.core.model.AppItem
    ) {
        appManager.launchApp(context, app)
    }

    fun refreshApps() {
        appManager.loadInstalledApps()
    }
}

class HomeViewModelFactory(
    private val dependencies: LauncherDependencies
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                HomeViewModel::class.java
            )
        ) {
            return HomeViewModel(dependencies) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
