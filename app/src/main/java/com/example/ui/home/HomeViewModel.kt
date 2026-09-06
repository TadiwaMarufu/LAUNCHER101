package com.example.ui.home

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

    /*
     * Keep the combination type-safe.
     *
     * Kotlin's large-arity combine overload can fall back to the
     * vararg Array<Any> overload in this project, which destroys
     * the concrete types of the values. Nested combines avoid that.
     */
    private val appearanceState =
        combine(
            wallpaper,
            iconShapeFlow,
            iconLabels
        ) { wallpaperPreset, iconShape, showIconLabels ->
            AppearanceState(
                wallpaperPreset = wallpaperPreset,
                iconShape = iconShape,
                showIconLabels = showIconLabels
            )
        }

    private val contentState =
        combine(
            tasks,
            homeItems
        ) { taskList, items ->
            ContentState(
                tasks = taskList,
                homeItems = items
            )
        }

    val uiState: StateFlow<HomeUiState> =
        combine(
            profile,
            appearanceState,
            contentState
        ) { activeProfile, appearance, content ->

            HomeUiState(
                activeProfile = activeProfile,
                wallpaperPreset = appearance.wallpaperPreset,
                iconShape = appearance.iconShape,
                showIconLabels = appearance.showIconLabels,
                tasks = content.tasks,
                homeItems = content.homeItems
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

    fun setProfile(profile: LauncherProfile) {
        viewModelScope.launch {
            dependencies.profileRepository.setProfile(profile)
        }
    }

    fun toggleAppPin(componentName: String) {
        viewModelScope.launch {
            val pinned =
                dependencies.profileRepository.pinnedApps.first()

            if (componentName in pinned) {
                dependencies.profileRepository.unpinApp(componentName)
            } else {
                dependencies.profileRepository.pinApp(componentName)
            }
        }
    }

    fun addHomeItem(item: HomeItem) {
        viewModelScope.launch {
            homeLayoutRepository.addItem(item)
        }
    }

    fun removeHomeItem(itemId: String) {
        viewModelScope.launch {
            homeLayoutRepository.removeItem(itemId)
        }
    }

    fun updateHomeItem(item: HomeItem) {
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

    fun addAppToHome(app: AppItem) {
        viewModelScope.launch {
            val id = homeLayoutRepository.newItemId("app")

            homeLayoutRepository.addItem(
                HomeItem(
                    id = id,
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
            )
        }
    }

    fun addClockToHome() {
        viewModelScope.launch {
            val current = homeLayoutRepository.items.first()

            if (current.any { it.type == HomeItemType.CLOCK }) return@launch

            homeLayoutRepository.addItem(
                HomeItem(
                    id = homeLayoutRepository.newItemId("clock"),
                    type = HomeItemType.CLOCK,
                    page = 0,
                    x = 2,
                    y = 1,
                    width = 2,
                    height = 1
                )
            )
        }
    }

    fun addNowBarToHome() {
        viewModelScope.launch {
            val current = homeLayoutRepository.items.first()

            if (current.any { it.type == HomeItemType.NOW_BAR }) return@launch

            homeLayoutRepository.addItem(
                HomeItem(
                    id = homeLayoutRepository.newItemId("nowbar"),
                    type = HomeItemType.NOW_BAR,
                    page = 0,
                    x = 0,
                    y = 7,
                    width = 5,
                    height = 1
                )
            )
        }
    }

    fun removeWidget(appWidgetId: Int) {
        viewModelScope.launch {
            dependencies.widgetManager.removeWidget(appWidgetId)

            val current =
                homeLayoutRepository.items.first()

            current
                .filter {
                    it.type == HomeItemType.WIDGET &&
                        it.appWidgetId == appWidgetId
                }
                .forEach {
                    homeLayoutRepository.removeItem(it.id)
                }
        }
    }

    private data class AppearanceState(
        val wallpaperPreset: WallpaperPreset,
        val iconShape: IconShape,
        val showIconLabels: Boolean
    )

    private data class ContentState(
        val tasks: List<LauncherTask>,
        val homeItems: List<HomeItem>
    )
}

class HomeViewModelFactory(
    private val dependencies: LauncherDependencies
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dependencies) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
