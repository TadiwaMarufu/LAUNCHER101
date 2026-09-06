package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.LauncherDependencies
import com.example.core.engine.ProfileEngine
import com.example.core.model.HomeItem
import com.example.core.model.HomeItemType
import com.example.core.model.LauncherProfile
import com.example.core.nowbar.NowBarController
import com.example.ui.drawer.AppDrawerView
import com.example.ui.profileswitcher.ProfileSwitcherDialog
import com.example.ui.search.UniversalSearchSheet
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.LocalLauncherAppearance

enum class LauncherOverlayState {
    NONE,
    APP_DRAWER,
    SEARCH,
    PROFILE_SWITCHER,
    SETTINGS,
    ADD_MENU,
    ITEM_MENU
}

/**
 * Main launcher surface.
 *
 * HomeScreen owns orchestration only:
 * - launcher state
 * - overlays
 * - profile selection
 * - navigation gestures
 *
 * The actual home surface is entirely owned by HomeCanvas/HomeItem state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddWidget: () -> Unit = {}
) {
    val context = LocalContext.current
    val appearance = LocalLauncherAppearance.current

    val dependencies = remember {
        LauncherDependencies.get(context)
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(dependencies)
    )

    val state by homeViewModel.uiState.collectAsState()

    val allApps by homeViewModel.appManager
        .allApps
        .collectAsState()

    val categorizedApps by homeViewModel.appManager
        .categorizedApps
        .collectAsState()

    val recentlyLaunched by homeViewModel.appManager
        .recentlyLaunched
        .collectAsState()

    val frequentlyLaunched by homeViewModel.appManager
        .frequentlyLaunched
        .collectAsState()

    val usageTrackingEnabled by homeViewModel
        .usageTrackingEnabled
        .collectAsState(initial = true)

    val nowBarController = remember {
        NowBarController.getInstance(context)
    }

    val activeProfile = state.activeProfile

    val profileConfig = remember(activeProfile) {
        ProfileEngine.getConfig(activeProfile)
    }

    var overlayState by remember {
        mutableStateOf(LauncherOverlayState.NONE)
    }

    var editMode by remember {
        mutableStateOf(false)
    }

    var selectedItem by remember {
        mutableStateOf<HomeItem?>(null)
    }

    var addingApp by remember {
        mutableStateOf(false)
    }

    var dragAccumulatorY by remember {
        mutableFloatStateOf(0f)
    }

    WallpaperBackground(
        preset = state.wallpaperPreset
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {

            /*
             * Global navigation gestures.
             *
             * HomeCanvas owns long-press/item gestures.
             * This layer only handles directional navigation.
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeProfile) {
                        detectDragGestures(
                            onDragStart = {
                                dragAccumulatorY = 0f
                            },
                            onDragEnd = {
                                when {
                                    dragAccumulatorY < -100f -> {
                                        overlayState =
                                            LauncherOverlayState.APP_DRAWER
                                        editMode = false
                                    }

                                    dragAccumulatorY > 100f -> {
                                        overlayState =
                                            LauncherOverlayState.SEARCH
                                        editMode = false
                                    }
                                }

                                dragAccumulatorY = 0f
                            },
                            onDragCancel = {
                                dragAccumulatorY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulatorY += dragAmount.y
                            }
                        )
                    }
            )

            HomeCanvas(
                items = state.homeItems,
                allApps = allApps,
                iconShape = state.iconShape,
                showIconLabels = state.showIconLabels,
                nowBarController = nowBarController,
                profile = activeProfile,
                config = profileConfig,
                modifier = Modifier.fillMaxSize(),
                editMode = editMode,

                onLongPress = {
                    editMode = true
                    overlayState = LauncherOverlayState.NONE
                },

                onEmptyTap = {
                    if (editMode) {
                        editMode = false
                    }
                },

                onItemClick = { item ->
                    if (editMode) {
                        selectedItem = item
                        overlayState =
                            LauncherOverlayState.ITEM_MENU
                        return@HomeCanvas
                    }

                    when (item.type) {
                        HomeItemType.APP -> {
                            val component =
                                if (
                                    !item.packageName.isNullOrBlank() &&
                                    !item.activityName.isNullOrBlank()
                                ) {
                                    "${item.packageName}/${item.activityName}"
                                } else {
                                    null
                                }

                            val app = allApps.firstOrNull {
                                it.componentNameString == component
                            }

                            if (app != null) {
                                homeViewModel.launchApp(
                                    context,
                                    app
                                )
                            }
                        }

                        HomeItemType.CLOCK,
                        HomeItemType.NOW_BAR,
                        HomeItemType.WIDGET,
                        HomeItemType.SHORTCUT,
                        HomeItemType.FOLDER -> Unit
                    }
                },

                onItemLongPress = { item ->
                    selectedItem = item
                    editMode = true
                    overlayState =
                        LauncherOverlayState.ITEM_MENU
                },

                onItemMove = { item, x, y ->
                    homeViewModel.moveHomeItem(
                        itemId = item.id,
                        page = item.page,
                        x = x,
                        y = y
                    )
                },

                onProfileChipClick = {
                    overlayState =
                        LauncherOverlayState.PROFILE_SWITCHER
                },

                onMediaPlayToggle = {
                    nowBarController.toggleMediaPlayback()
                },

                onSearchClick = {
                    overlayState =
                        LauncherOverlayState.SEARCH
                },

                onDrawerClick = {
                    overlayState =
                        LauncherOverlayState.APP_DRAWER
                },

                onSettingsClick = {
                    overlayState =
                        LauncherOverlayState.SETTINGS
                }
            )

            /*
             * Edit-mode controls are temporary.
             * They are not part of the user's home canvas.
             */
            AnimatedVisibility(
                visible =
                    editMode &&
                        overlayState ==
                            LauncherOverlayState.NONE,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            appearance.elevatedSurface.copy(
                                alpha = appearance.surfaceAlpha
                            )
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 6.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    EditAction(
                        text = "Add",
                        appearance = appearance
                    ) {
                        overlayState =
                            LauncherOverlayState.ADD_MENU
                    }

                    EditAction(
                        text = "Profile",
                        appearance = appearance
                    ) {
                        overlayState =
                            LauncherOverlayState.PROFILE_SWITCHER
                    }

                    EditAction(
                        text = "Settings",
                        appearance = appearance
                    ) {
                        overlayState =
                            LauncherOverlayState.SETTINGS
                    }

                    EditAction(
                        text = "Done",
                        appearance = appearance
                    ) {
                        editMode = false
                    }
                }
            }

            /*
             * App drawer.
             */
            AnimatedVisibility(
                visible =
                    overlayState ==
                        LauncherOverlayState.APP_DRAWER,
                enter =
                    slideInVertically(
                        initialOffsetY = { it }
                    ) + fadeIn(),
                exit =
                    slideOutVertically(
                        targetOffsetY = { it }
                    ) + fadeOut()
            ) {
                AppDrawerView(
                    allApps = allApps,
                    categorizedApps = categorizedApps,
                    recentlyLaunched = recentlyLaunched,
                    frequentlyLaunched = frequentlyLaunched,
                    usageTrackingEnabled = usageTrackingEnabled,
                    profile = activeProfile,
                    config = profileConfig,
                    iconShape = state.iconShape,
                    showLabels = state.showIconLabels,

                    onAppClick = { app ->
                        if (addingApp) {
                            homeViewModel.addAppToHome(app)
                            addingApp = false
                            overlayState =
                                LauncherOverlayState.NONE
                            editMode = true
                        } else {
                            homeViewModel.launchApp(
                                context,
                                app
                            )

                            overlayState =
                                LauncherOverlayState.NONE
                        }
                    },

                    onAppLongClick = { app ->
                        if (editMode) {
                            homeViewModel.toggleAppPin(app)
                        }
                    },

                    onCloseDrawer = {
                        addingApp = false
                        overlayState =
                            LauncherOverlayState.NONE
                    }
                )
            }

            /*
             * Universal search.
             */
            AnimatedVisibility(
                visible =
                    overlayState ==
                        LauncherOverlayState.SEARCH,
                enter =
                    slideInVertically(
                        initialOffsetY = { -it / 2 }
                    ) + fadeIn(),
                exit =
                    slideOutVertically(
                        targetOffsetY = { -it / 2 }
                    ) + fadeOut()
            ) {
                UniversalSearchSheet(
                    profile = activeProfile,
                    config = profileConfig,
                    iconShape = state.iconShape,
                    onClose = {
                        overlayState =
                            LauncherOverlayState.NONE
                    }
                )
            }

            /*
             * Settings.
             */
            AnimatedVisibility(
                visible =
                    overlayState ==
                        LauncherOverlayState.SETTINGS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SettingsScreen(
                    onBack = {
                        overlayState =
                            LauncherOverlayState.NONE
                    },
                    onOpenProfileSwitcher = {
                        overlayState =
                            LauncherOverlayState.PROFILE_SWITCHER
                    }
                )
            }

            /*
             * Profile switcher.
             */
            if (
                overlayState ==
                    LauncherOverlayState.PROFILE_SWITCHER
            ) {
                ModalBottomSheet(
                    onDismissRequest = {
                        overlayState =
                            LauncherOverlayState.NONE
                    },
                    sheetState =
                        rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        ),
                    containerColor = appearance.surface,
                    contentColor = appearance.onSurface
                ) {
                    ProfileSwitcherDialog(
                        activeProfile = activeProfile,

                        onSelectProfile = { profile ->
                            homeViewModel.setProfile(profile)
                            overlayState =
                                LauncherOverlayState.NONE
                        },

                        onDismiss = {
                            overlayState =
                                LauncherOverlayState.NONE
                        }
                    )
                }
            }

            /*
             * Add something to the user's canvas.
             */
            if (
                overlayState ==
                    LauncherOverlayState.ADD_MENU
            ) {
                AddHomeItemSheet(
                    appearance = appearance,
                    onDismiss = {
                        overlayState =
                            LauncherOverlayState.NONE
                    },

                    onAddApp = {
                        addingApp = true
                        overlayState =
                            LauncherOverlayState.APP_DRAWER
                    },

                    onAddClock = {
                        homeViewModel.addClockToHome()
                        overlayState =
                            LauncherOverlayState.NONE
                        editMode = true
                    },

                    onAddNowBar = {
                        homeViewModel.addNowBarToHome()
                        overlayState =
                            LauncherOverlayState.NONE
                        editMode = true
                    },

                    onAddWidget = {
                        overlayState =
                            LauncherOverlayState.NONE
                        onAddWidget()
                        editMode = true
                    }
                )
            }

            /*
             * Edit/remove a placed canvas item.
             */
            if (
                overlayState ==
                    LauncherOverlayState.ITEM_MENU &&
                    selectedItem != null
            ) {
                ItemEditorSheet(
                    item = selectedItem!!,
                    appearance = appearance,

                    onDismiss = {
                        selectedItem = null
                        overlayState =
                            LauncherOverlayState.NONE
                    },

                    onRemove = {
                        val item = selectedItem

                        if (item != null) {
                            homeViewModel.removeHomeItem(item.id)

                            if (
                                item.type ==
                                    HomeItemType.WIDGET &&
                                item.appWidgetId != null
                            ) {
                                homeViewModel.removeWidget(
                                    item.appWidgetId
                                )
                            }
                        }

                        selectedItem = null
                        overlayState =
                            LauncherOverlayState.NONE
                    }
                )
            }
        }
    }
}

@Composable
private fun EditAction(
    text: String,
    appearance: com.example.core.appearance.LauncherAppearance,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = appearance.onSurface,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHomeItemSheet(
    appearance: com.example.core.appearance.LauncherAppearance,
    onDismiss: () -> Unit,
    onAddApp: () -> Unit,
    onAddClock: () -> Unit,
    onAddNowBar: () -> Unit,
    onAddWidget: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
        containerColor = appearance.surface,
        contentColor = appearance.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(24.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Add to Home",
                color = appearance.onSurface
            )

            AddOption(
                text = "App",
                appearance = appearance,
                onClick = onAddApp
            )

            AddOption(
                text = "Widget",
                appearance = appearance,
                onClick = onAddWidget
            )

            AddOption(
                text = "Clock",
                appearance = appearance,
                onClick = onAddClock
            )

            AddOption(
                text = "Now Bar",
                appearance = appearance,
                onClick = onAddNowBar
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text =
                    "Home is yours. Nothing is added automatically.",
                color = appearance.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddOption(
    text: String,
    appearance: com.example.core.appearance.LauncherAppearance,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = appearance.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditorSheet(
    item: HomeItem,
    appearance: com.example.core.appearance.LauncherAppearance,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = appearance.surface,
        contentColor = appearance.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text =
                    item.title
                        ?: item.type.name
                            .lowercase()
                            .replaceFirstChar {
                                it.uppercase()
                            },
                color = appearance.onSurface
            )

            Text(
                text =
                    "Position: ${item.x}, ${item.y}",
                color = appearance.onSurfaceVariant
            )

            Text(
                text =
                    "Size: ${item.width} × ${item.height}",
                color = appearance.onSurfaceVariant
            )

            Text(
                text = "Remove",
                color = appearance.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onRemove
                    )
                    .padding(16.dp)
            )
        }
    }
}
