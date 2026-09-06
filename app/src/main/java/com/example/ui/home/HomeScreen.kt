package com.example.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.LauncherDependencies
import com.example.core.engine.HomeLayoutStyle
import com.example.core.engine.ProfileEngine
import com.example.core.model.AppItem
import com.example.core.model.LauncherProfile
import com.example.core.nowbar.NowBarController
import com.example.ui.drawer.AppDrawerView
import com.example.ui.drawer.AppIconImage
import com.example.ui.nowbar.NowBarView
import com.example.ui.profileswitcher.ProfileSwitcherDialog
import com.example.ui.search.UniversalSearchSheet
import com.example.ui.settings.SettingsScreen
import com.example.ui.widgets.FocusTasksWidgetCard
import com.example.ui.widgets.MediaWidgetCard
import com.example.ui.widgets.PersonalityClockWidget
import com.example.ui.widgets.SystemTelemetryCard
import com.example.ui.widgets.WeatherCard
import com.example.core.widgets.AndroidAppWidgetHostView

enum class LauncherOverlayState {
    NONE,
    APP_DRAWER,
    SEARCH,
    PROFILE_SWITCHER,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddWidget: () -> Unit = {}
) {
    val context = LocalContext.current

    val dependencies = remember {
        LauncherDependencies.get(context)
    }

    val widgetManager =
        dependencies.widgetManager

    val placedWidgets by widgetManager
        .placedWidgets
        .collectAsState(initial = emptyList())

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(dependencies)
    )

    val state by homeViewModel.uiState.collectAsState()

    val appManager = homeViewModel.appManager

    val nowBarController = remember {
        NowBarController.getInstance(context)
    }

    val allApps by appManager.allApps.collectAsState()
    val categorizedApps by appManager.categorizedApps.collectAsState()
    val recentlyLaunched by appManager.recentlyLaunched.collectAsState()
    val frequentlyLaunched by appManager.frequentlyLaunched.collectAsState()
    val usageTrackingEnabled by homeViewModel.usageTrackingEnabled.collectAsState(initial = true)

    val nowBarItems by nowBarController.items.collectAsState()
    val mediaState by nowBarController.mediaState.collectAsState()
    val batteryState by nowBarController.batteryState.collectAsState()

    val activeProfile = state.activeProfile

    val profileConfig = remember(activeProfile) {
        ProfileEngine.getConfig(activeProfile)
    }

    var overlayState by remember {
        mutableStateOf(LauncherOverlayState.NONE)
    }

    var dragAccumulatorY by remember {
        mutableStateOf(0f)
    }

    WallpaperBackground(
        preset = state.wallpaperPreset
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(activeProfile) {
                    detectTapGestures(
                        onDoubleTap = {
                            homeViewModel.cycleProfile()
                        },
                        onLongPress = {
                            overlayState =
                                LauncherOverlayState.SETTINGS
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                dragAccumulatorY < -80f -> {
                                    overlayState =
                                        LauncherOverlayState.APP_DRAWER
                                }

                                dragAccumulatorY > 80f -> {
                                    overlayState =
                                        LauncherOverlayState.SEARCH
                                }
                            }

                            dragAccumulatorY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulatorY += dragAmount.y
                        }
                    )
                }
                .padding(
                    horizontal = 20.dp,
                    vertical = 24.dp
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(
                        modifier = Modifier.height(
                            profileConfig.headerSpacing
                        )
                    )

                    PersonalityClockWidget(
                        config = profileConfig,
                        onClockClick = {
                            overlayState =
                                LauncherOverlayState.PROFILE_SWITCHER
                        }
                    )

                    when (profileConfig.homeLayout) {
                        HomeLayoutStyle.FLUID_ORGANIC -> {
                            MediaWidgetCard(
                                mediaState = mediaState,
                                profile = activeProfile,
                                config = profileConfig,
                                onTogglePlay = {
                                    nowBarController.togglePlayPause()
                                },
                                onNext = {
                                    nowBarController.nextTrack()
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                WeatherCard(
                                    profile = activeProfile,
                                    config = profileConfig,
                                    modifier = Modifier.weight(1f)
                                )

                                SystemTelemetryCard(
                                    battery = batteryState,
                                    profile = activeProfile,
                                    config = profileConfig,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                SystemTelemetryCard(
                                    battery = batteryState,
                                    profile = activeProfile,
                                    config = profileConfig
                                )

                                WeatherCard(
                                    profile = activeProfile,
                                    config = profileConfig
                                )
                            }
                        }

                        HomeLayoutStyle.CALM_MINIMALIST -> {
                            Spacer(
                                modifier = Modifier.height(48.dp)
                            )
                        }

                        HomeLayoutStyle.FOCUS_DASHBOARD -> {
                            FocusTasksWidgetCard(
                                tasks = state.tasks,
                                profile = activeProfile,
                                config = profileConfig,
                                onToggleTask =
                                    homeViewModel::toggleTask,
                                onAddTask =
                                    homeViewModel::addTask
                            )
                        }

                        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> {
                            MediaWidgetCard(
                                mediaState = mediaState,
                                profile = activeProfile,
                                config = profileConfig,
                                onTogglePlay = {
                                    nowBarController.togglePlayPause()
                                },
                                onNext = {
                                    nowBarController.nextTrack()
                                }
                            )
                        }
                    }

                    /*
                     * Real Android App Widgets.
                     *
                     * Native Purple widgets remain profile-driven cards above.
                     * These are actual AppWidgetHost views supplied by installed
                     * Android applications.
                     */
                    if (placedWidgets.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            placedWidgets
                                .sortedBy { it.position }
                                .forEach { widget ->
                                    val hostView =
                                        remember(widget.appWidgetId) {
                                            widgetManager.createHostView(
                                                context,
                                                widget.appWidgetId
                                            )
                                        }

                                    if (hostView != null) {
                                        Surface(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(
                                                        (widget.rowSpan * 140)
                                                            .coerceAtLeast(100)
                                                            .dp
                                                    )
                                                    .pointerInput(
                                                        widget.appWidgetId
                                                    ) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                // Remove through the
                                                                // launcher-owned
                                                                // persistence boundary.
                                                                homeViewModel.removeWidget(
                                                                    widget.appWidgetId
                                                                )
                                                            }
                                                        )
                                                    },
                                            tonalElevation = 1.dp
                                        ) {
                                            AndroidAppWidgetHostView(
                                                appWidgetHost =
                                                    widgetManager.host,
                                                appWidgetManager =
                                                    widgetManager.appWidgetManager,
                                                appWidgetId =
                                                    widget.appWidgetId,
                                                modifier =
                                                    Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.End
                    ) {
                        Text(
                            text = "+ Widget",
                            modifier =
                                Modifier
                                    .clickable(
                                        onClick = onAddWidget
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    val homeAppLimit =
                        if (
                            profileConfig.homeLayout ==
                            HomeLayoutStyle.CALM_MINIMALIST
                        ) {
                            4
                        } else {
                            8
                        }

                    // Personalize the home row only when local usage
                    // tracking is enabled and real usage history exists.
                    // Otherwise fall back to deterministic alphabetical
                    // installed-app defaults rather than fabricating usage.
                    val homeApps =
                        if (
                            usageTrackingEnabled &&
                            frequentlyLaunched.isNotEmpty()
                        ) {
                            frequentlyLaunched.take(homeAppLimit)
                        } else {
                            allApps.take(homeAppLimit)
                        }

                    if (homeApps.isNotEmpty()) {
                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(
                                profileConfig.gridColumns
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(16.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    if (homeApps.size > 4) {
                                        180.dp
                                    } else {
                                        90.dp
                                    }
                                )
                        ) {
                            items(
                                homeApps,
                                key = {
                                    it.componentNameString
                                }
                            ) { app ->
                                HomeAppItem(
                                    context = context,
                                    app = app,
                                    iconShape = state.iconShape,
                                    showIconLabels =
                                        state.showIconLabels,
                                    activeProfile =
                                        activeProfile,
                                    profileConfig =
                                        profileConfig,
                                    onLaunch =
                                        homeViewModel::launchApp
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                NowBarView(
                    items = nowBarItems,
                    mediaState = mediaState,
                    profile = activeProfile,
                    config = profileConfig,
                    onProfileChipClick = {
                        overlayState =
                            LauncherOverlayState.PROFILE_SWITCHER
                    },
                    onMediaPlayToggle = {
                        nowBarController.togglePlayPause()
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
            }

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
                        homeViewModel.launchApp(
                            context,
                            app
                        )

                        overlayState =
                            LauncherOverlayState.NONE
                    },
                    onAppLongClick = { app ->
                        homeViewModel.toggleAppPin(
                            app.componentNameString
                        )
                    },
                    onCloseDrawer = {
                        overlayState =
                            LauncherOverlayState.NONE
                    }
                )
            }

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
                    containerColor = Color.Transparent,
                    dragHandle = null
                ) {
                    ProfileSwitcherDialog(
                        activeProfile = activeProfile,
                        onSelectProfile = { selectedProfile ->
                            homeViewModel.setProfile(
                                selectedProfile
                            )

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
        }
    }
}

@Composable
private fun HomeAppItem(
    context: Context,
    app: AppItem,
    iconShape: com.example.core.model.IconShape,
    showIconLabels: Boolean,
    activeProfile: LauncherProfile,
    profileConfig: com.example.core.engine.ProfileConfig,
    onLaunch: (Context, AppItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onLaunch(
                    context,
                    app
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconImage(
            drawable = app.icon,
            label = app.label,
            iconShape = iconShape,
            size = profileConfig.iconSize,
            accentColor =
                activeProfile.primaryAccent
        )

        if (
            showIconLabels &&
            profileConfig.showAppLabels
        ) {
            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = app.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = activeProfile.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
