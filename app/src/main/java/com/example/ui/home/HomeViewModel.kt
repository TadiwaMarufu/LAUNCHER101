package com.example.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.LauncherDependencies
import com.example.core.apps.AppManager
import com.example.core.data.repository.HomeLayoutRepository
import com.example.core.data.repository.LauncherContentRepository
import com.example.core.model.AppItem
import com.example.core.model.HomeItem
import com.example.core.model.HomeItemType
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.LauncherTask
import com.example.core.model.WallpaperPreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeProfile: LauncherProfile = LauncherProfile.FLUID,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.DEEP_PURPLE,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val showIconLabels: Boolean = true,
    val tasks: List<LauncherTask> = emptyList(),
    val homeItems: List<HomeItem> = emptyList()
)

class HomeViewModel(
    private val dependencies: LauncherDependencies
) : ViewModel() {

    val appManager: AppManager =
        dependencies.appManager

    val contentRepository: LauncherContentRepository =
        dependencies.launcherContentRepository

    val homeLayoutRepository: HomeLayoutRepository =
        dependencies.homeLayoutRepository

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

    private val homeItems =
        homeLayoutRepository.items

    val uiState: StateFlow<HomeUiState> =
        combine(
            profile,
            wallpaper,
            iconShapeFlow,
            iconLabels,
            tasks,
            homeItems
        ) { activeProfile,
            wallpaperPreset,
            iconShape,
            showIconLabels,
            taskList,
            items ->

            HomeUiState(
                activeProfile = activeProfile,
                wallpaperPreset = wallpaperPreset,
                iconShape = iconShape,
                showIconLabels = showIconLabels,
                tasks = taskList,
                homeItems = items
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    init {
        viewModelScope.launch {
            homeLayoutRepository.ensureDefaultCanvas()
        }
    }

    fun cycleProfile() {
        viewModelScope.launch {
            dependencies.profileRepository.cycleProfile()
        }
    }

    fun setProfile(
        profile: LauncherProfile
    ) {
        viewModelScope.launch {
            dependencies.profileRepository.setProfile(profile)
        }
    }

    fun toggleAppPin(
        componentName: String
    ) {
        viewModelScope.launch {
            contentRepository.toggleAppPin(componentName)
            appManager.loadInstalledApps()
        }
    }

    fun addTask(
        text: String
    ) {
        viewModelScope.launch {
            contentRepository.addTask(text)
        }
    }

    fun toggleTask(
        id: String
    ) {
        viewModelScope.launch {
            contentRepository.toggleTask(id)
        }
    }

    fun removeTask(
        id: String
    ) {
        viewModelScope.launch {
            contentRepository.removeTask(id)
        }
    }

    fun launchApp(
        context: Context,
        app: AppItem
    ) {
        appManager.launchApp(
            context,
            app
        )
    }

    fun addHomeItem(
        item: HomeItem
    ) {
        viewModelScope.launch {
            homeLayoutRepository.addItem(item)
        }
    }

    fun removeHomeItem(
        itemId: String
    ) {
        viewModelScope.launch {
            homeLayoutRepository.removeItem(itemId)
        }
    }

    fun updateHomeItem(
        item: HomeItem
    ) {
        viewModelScope.launch {
            homeLayoutRepository.updateItem(item)
        }
    }

    fun moveHomeItem(
        itemId: String,
        page: Int,
        x: Int,
        y: Int
    ) {
        viewModelScope.launch {
            homeLayoutRepository.moveItem(
                itemId = itemId,
                page = page,
                x = x,
                y = y
            )
        }
    }

    fun addAppToHome(
        app: AppItem
    ) {
        val item =
            HomeItem(
                id =
                    homeLayoutRepository.newItemId(
                        "app"
                    ),
                type = HomeItemType.APP,
                page = 0,
                x = 0,
                y = 0,
                width = 1,
                height = 1,
                packageName = app.packageName,
                activityName = app.activityName,
                title = app.label
            )

        addHomeItem(item)
    }

    fun addClockToHome() {
        val item =
            HomeItem(
                id =
                    homeLayoutRepository.newItemId(
                        "clock"
                    ),
                type = HomeItemType.CLOCK,
                page = 0,
                x = 2,
                y = 1,
                width = 2,
                height = 1
            )

        addHomeItem(item)
    }

    fun addNowBarToHome() {
        val item =
            HomeItem(
                id =
                    homeLayoutRepository.newItemId(
                        "nowbar"
                    ),
                type = HomeItemType.NOW_BAR,
                page = 0,
                x = 0,
                y = 7,
                width = 5,
                height = 1
            )

        addHomeItem(item)
    }

    fun removeWidget(
        appWidgetId: Int
    ) {
        viewModelScope.launch {
            dependencies.widgetManager.removeWidget(
                appWidgetId
            )

            homeLayoutRepository
                .items
                .first()
                .filter {
                    it.appWidgetId == appWidgetId
                }
                .forEach {
                    homeLayoutRepository.removeItem(it.id)
                }
        }
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
            return HomeViewModel(
                dependencies
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
