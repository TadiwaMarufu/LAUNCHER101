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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.apps.AppManager
import com.example.core.data.LauncherPreferences
import com.example.core.engine.HomeLayoutStyle
import com.example.core.engine.ProfileConfig
import com.example.core.engine.ProfileEngine
import com.example.core.model.AppCategory
import com.example.core.model.AppItem
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.LauncherTask
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
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { LauncherPreferences.getInstance(context) }
    val appManager = remember { AppManager.getInstance(context) }
    val nowBarController = remember { NowBarController.getInstance(context) }
    val scope = rememberCoroutineScope()

    val activeProfile by preferences.activeProfile.collectAsState()
    val wallpaperPreset by preferences.wallpaperPreset.collectAsState()
    val iconShape by preferences.iconShape.collectAsState()
    val showIconLabels by preferences.showIconLabels.collectAsState()
    val tasks by preferences.tasks.collectAsState()

    val allApps by appManager.allApps.collectAsState()
    val categorizedApps by appManager.categorizedApps.collectAsState()
    val frequentlyLaunched by appManager.frequentlyLaunched.collectAsState()

    val nowBarItems by nowBarController.items.collectAsState()
    val mediaState by nowBarController.mediaState.collectAsState()
    val batteryState by nowBarController.batteryState.collectAsState()

    val profileConfig = remember(activeProfile) { ProfileEngine.getConfig(activeProfile) }

    var overlayState by remember { mutableStateOf(LauncherOverlayState.NONE) }
    var dragAccumulatorY by remember { mutableStateOf(0f) }

    WallpaperBackground(preset = wallpaperPreset) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            preferences.cycleNextProfile()
                        },
                        onLongPress = {
                            overlayState = LauncherOverlayState.SETTINGS
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragAccumulatorY < -80f) {
                                // Swiped up -> App Drawer
                                overlayState = LauncherOverlayState.APP_DRAWER
                            } else if (dragAccumulatorY > 80f) {
                                // Swiped down -> Universal Search
                                overlayState = LauncherOverlayState.SEARCH
                            }
                            dragAccumulatorY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulatorY += dragAmount.y
                        }
                    )
                }
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top & Center Content: Profile-Specific Layout
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(profileConfig.headerSpacing))

                    // Clock Widget (Personality-specific)
                    PersonalityClockWidget(
                        config = profileConfig,
                        onClockClick = { overlayState = LauncherOverlayState.PROFILE_SWITCHER }
                    )

                    // Profile-specific content arrangement
                    when (profileConfig.homeLayout) {
                        HomeLayoutStyle.FLUID_ORGANIC -> {
                            // Fluid profile: Dynamic media card + ambient widgets + flowing quick shelf
                            MediaWidgetCard(
                                mediaState = mediaState,
                                profile = activeProfile,
                                config = profileConfig,
                                onTogglePlay = { nowBarController.togglePlayPause() },
                                onNext = { nowBarController.nextTrack() }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            // Premium profile: Restrained status line + curated high-density app layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                            // Calm profile: Generous whitespace, zero clutter, peaceful presence
                            Spacer(modifier = Modifier.height(48.dp))
                        }

                        HomeLayoutStyle.FOCUS_DASHBOARD -> {
                            // Focus profile: Interactive task checklist + quick timer trigger
                            FocusTasksWidgetCard(
                                tasks = tasks,
                                profile = activeProfile,
                                config = profileConfig,
                                onToggleTask = { preferences.toggleTask(it) },
                                onAddTask = { preferences.addTask(it) }
                            )
                        }

                        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> {
                            // Expressive profile: Bold visual cards + creative asymmetry
                            MediaWidgetCard(
                                mediaState = mediaState,
                                profile = activeProfile,
                                config = profileConfig,
                                onTogglePlay = { nowBarController.togglePlayPause() },
                                onNext = { nowBarController.nextTrack() }
                            )
                        }
                    }

                    // Home Screen Quick Applications Section
                    val homeApps = frequentlyLaunched.take(if (profileConfig.homeLayout == HomeLayoutStyle.CALM_MINIMALIST) 4 else 8)
                    if (homeApps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(profileConfig.gridColumns),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (homeApps.size > 4) 180.dp else 90.dp)
                        ) {
                            items(homeApps, key = { it.componentNameString }) { app ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { appManager.launchApp(context, app) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AppIconImage(
                                        drawable = app.icon,
                                        label = app.label,
                                        iconShape = iconShape,
                                        size = profileConfig.iconSize,
                                        accentColor = activeProfile.primaryAccent
                                    )
                                    if (showIconLabels && profileConfig.showAppLabels) {
                                        Spacer(modifier = Modifier.height(4.dp))
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Now Bar (Replaces traditional app dock)
                NowBarView(
                    items = nowBarItems,
                    mediaState = mediaState,
                    profile = activeProfile,
                    config = profileConfig,
                    onProfileChipClick = { overlayState = LauncherOverlayState.PROFILE_SWITCHER },
                    onMediaPlayToggle = { nowBarController.togglePlayPause() },
                    onSearchClick = { overlayState = LauncherOverlayState.SEARCH },
                    onDrawerClick = { overlayState = LauncherOverlayState.APP_DRAWER },
                    onSettingsClick = { overlayState = LauncherOverlayState.SETTINGS }
                )
            }

            // Overlays: App Drawer
            AnimatedVisibility(
                visible = overlayState == LauncherOverlayState.APP_DRAWER,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AppDrawerView(
                    allApps = allApps,
                    categorizedApps = categorizedApps,
                    profile = activeProfile,
                    config = profileConfig,
                    iconShape = iconShape,
                    showLabels = showIconLabels,
                    onAppClick = { app ->
                        appManager.launchApp(context, app)
                        overlayState = LauncherOverlayState.NONE
                    },
                    onAppLongClick = { app ->
                        preferences.toggleAppPin(app.componentNameString)
                    },
                    onCloseDrawer = { overlayState = LauncherOverlayState.NONE }
                )
            }

            // Overlays: Universal Search & Commands
            AnimatedVisibility(
                visible = overlayState == LauncherOverlayState.SEARCH,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
            ) {
                UniversalSearchSheet(
                    profile = activeProfile,
                    config = profileConfig,
                    iconShape = iconShape,
                    onClose = { overlayState = LauncherOverlayState.NONE }
                )
            }

            // Overlays: Settings
            AnimatedVisibility(
                visible = overlayState == LauncherOverlayState.SETTINGS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SettingsScreen(
                    onBack = { overlayState = LauncherOverlayState.NONE },
                    onOpenProfileSwitcher = { overlayState = LauncherOverlayState.PROFILE_SWITCHER }
                )
            }

            // Overlays: Profile Switcher Dialog
            if (overlayState == LauncherOverlayState.PROFILE_SWITCHER) {
                ModalBottomSheet(
                    onDismissRequest = { overlayState = LauncherOverlayState.NONE },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Color.Transparent,
                    dragHandle = null
                ) {
                    ProfileSwitcherDialog(
                        activeProfile = activeProfile,
                        onSelectProfile = { selectedProfile ->
                            preferences.setActiveProfile(selectedProfile)
                            overlayState = LauncherOverlayState.NONE
                        },
                        onDismiss = { overlayState = LauncherOverlayState.NONE }
                    )
                }
            }
        }
    }
}
