package com.example.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.core.LauncherDependencies
import com.example.core.engine.HomeLayoutStyle
import com.example.core.engine.ProfileConfig
import com.example.core.model.AppItem
import com.example.core.model.HomeItem
import com.example.core.model.HomeItemType
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.nowbar.NowBarController
import com.example.core.widgets.AndroidAppWidgetHostView
import com.example.ui.drawer.AppIconImage
import com.example.ui.nowbar.NowBarView
import com.example.ui.theme.LocalLauncherAppearance
import com.example.ui.widgets.PersonalityClockWidget
import kotlin.math.roundToInt

private const val GRID_ROWS = 10

@Composable
fun HomeCanvas(
    items: List<HomeItem>,
    allApps: List<AppItem>,
    iconShape: IconShape,
    showIconLabels: Boolean,
    nowBarController: NowBarController,
    profile: LauncherProfile,
    config: ProfileConfig,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    onEmptyTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onItemClick: (HomeItem) -> Unit = {},
    onItemLongPress: (HomeItem) -> Unit = {},
    onItemMove: (HomeItem, Int, Int) -> Unit = { _, _, _ -> },
    onProfileChipClick: () -> Unit = {},
    onMediaPlayToggle: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onDrawerClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val appearance = LocalLauncherAppearance.current
    val context = LocalContext.current

    val dependencies = remember(context) {
        LauncherDependencies.get(context)
    }

    val widgetManager = dependencies.widgetManager

    // The profile owns horizontal home density.
    // Keep vertical capacity stable while preserving persisted item coordinates.
    val gridColumns = config.gridColumns.coerceIn(3, 6)
    val gridRows = GRID_ROWS

    val nowBarItems = nowBarController.items.value
    val mediaState = nowBarController.mediaState.value

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(editMode) {
                detectTapGestures(
                    onTap = {
                        if (editMode) {
                            onEmptyTap()
                        }
                    },
                    onLongPress = {
                        onLongPress()
                    }
                )
            }
    ) {
        val cellWidth = maxWidth / gridColumns
        val cellHeight = maxHeight / gridRows

        /*
         * Profile composition:
         *
         * FLUID       -> generous, organic breathing room
         * PREMIUM     -> structured and balanced
         * CALM        -> spacious and quiet
         * FOCUS       -> compact and information-dense
         * EXPRESSIVE  -> asymmetric-feeling visual rhythm
         *
         * Coordinates remain user-owned. These values only influence
         * the visual treatment of the existing canvas.
         */
        /*
         * The profile changes composition, never persisted geometry.
         *
         * This is the visual bridge between Smart Launcher-style
         * density and One UI-style spatial hierarchy.
         */
        val compositionScale = when (config.homeLayout) {
            HomeLayoutStyle.FLUID_ORGANIC -> 1.00f
            HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 0.985f
            HomeLayoutStyle.CALM_MINIMALIST -> 0.955f
            HomeLayoutStyle.FOCUS_DASHBOARD -> 1.00f
            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 1.035f
        }

        val compositionAlpha = when (config.homeLayout) {
            HomeLayoutStyle.FLUID_ORGANIC -> 1.00f
            HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 0.97f
            HomeLayoutStyle.CALM_MINIMALIST -> 0.88f
            HomeLayoutStyle.FOCUS_DASHBOARD -> 1.00f
            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 1.00f
        }

        val canvasInset = when (config.homeLayout) {
            HomeLayoutStyle.FLUID_ORGANIC -> 2.dp
            HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 4.dp
            HomeLayoutStyle.CALM_MINIMALIST -> 7.dp
            HomeLayoutStyle.FOCUS_DASHBOARD -> 1.dp
            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 0.dp
        }

        val itemCornerRadius = when (config.homeLayout) {
            HomeLayoutStyle.FLUID_ORGANIC ->
                config.cornerRadius

            HomeLayoutStyle.PREMIUM_ARCHITECTURAL ->
                config.cornerRadius * 0.82f

            HomeLayoutStyle.CALM_MINIMALIST ->
                config.cornerRadius * 0.70f

            HomeLayoutStyle.FOCUS_DASHBOARD ->
                config.cornerRadius * 0.62f

            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE ->
                config.cornerRadius * 1.18f
        }

        val editSurfaceAlpha = when (config.homeLayout) {
            HomeLayoutStyle.FLUID_ORGANIC -> 0.055f
            HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 0.045f
            HomeLayoutStyle.CALM_MINIMALIST -> 0.025f
            HomeLayoutStyle.FOCUS_DASHBOARD -> 0.065f
            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 0.075f
        }

        items
            .asSequence()
            .filter { it.visible && it.page == 0 }
            .forEach { item ->

                val safeWidth = item.width.coerceAtLeast(1)
                val safeHeight = item.height.coerceAtLeast(1)

                val itemWidth = cellWidth * safeWidth
                val itemHeight = cellHeight * safeHeight

                val safeX = item.x.coerceIn(
                    0,
                    (gridColumns - safeWidth).coerceAtLeast(0)
                )

                val safeY = item.y.coerceIn(
                    0,
                    (GRID_ROWS - safeHeight).coerceAtLeast(0)
                )

                val xOffset = cellWidth * safeX
                val yOffset = cellHeight * safeY

                /*
                 * Personality-aware composition.
                 *
                 * We deliberately do not rewrite persisted coordinates.
                 * The user's layout remains authoritative; profiles only
                 * influence how each item breathes inside that layout.
                 */
                val itemContentScale = when (config.homeLayout) {
                    HomeLayoutStyle.FLUID_ORGANIC -> {
                        when (item.type) {
                            HomeItemType.CLOCK -> 1.02f
                            HomeItemType.NOW_BAR -> 1.00f
                            else -> 0.96f
                        }
                    }

                    HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> {
                        when (item.type) {
                            HomeItemType.CLOCK -> 0.96f
                            HomeItemType.NOW_BAR -> 0.94f
                            else -> 0.94f
                        }
                    }

                    HomeLayoutStyle.CALM_MINIMALIST -> {
                        when (item.type) {
                            HomeItemType.CLOCK -> 0.92f
                            HomeItemType.NOW_BAR -> 0.90f
                            else -> 0.90f
                        }
                    }

                    HomeLayoutStyle.FOCUS_DASHBOARD -> {
                        when (item.type) {
                            HomeItemType.CLOCK -> 0.94f
                            HomeItemType.NOW_BAR -> 0.98f
                            else -> 0.98f
                        }
                    }

                    HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> {
                        when (item.type) {
                            HomeItemType.CLOCK -> 1.06f
                            HomeItemType.NOW_BAR -> 1.02f
                            else -> 1.00f
                        }
                    }
                }

                var dragX by remember(item.id) {
                    mutableFloatStateOf(0f)
                }

                var dragY by remember(item.id) {
                    mutableFloatStateOf(0f)
                }

                val scale by animateFloatAsState(
                    targetValue =
                        if (editMode) config.editScale else 1f,
                    animationSpec = spring(
                        dampingRatio = config.springDamping,
                        stiffness = config.springStiffness
                    ),
                    label = "home-item-scale"
                )

                val itemModifier = Modifier
                    .padding(canvasInset)
                    .offset {
                        IntOffset(
                            x = xOffset.roundToPx() + dragX.roundToInt(),
                            y = yOffset.roundToPx() + dragY.roundToInt()
                        )
                    }
                    .width(itemWidth)
                    .height(itemHeight)
                    .scale(
                        scale *
                            compositionScale *
                            itemContentScale
                    )
                    .clip(
                        RoundedCornerShape(itemCornerRadius)
                    )
                    .then(
                        if (editMode) {
                            Modifier.background(
                                appearance.primary.copy(
                                    alpha =
                                        editSurfaceAlpha *
                                            compositionAlpha
                                )
                            )
                        } else {
                            Modifier
                        }
                    )
                    .pointerInput(item.id, editMode) {
                        detectTapGestures(
                            onTap = {
                                onItemClick(item)
                            },
                            onLongPress = {
                                onItemLongPress(item)
                            }
                        )
                    }
                    .then(
                        if (editMode) {
                            Modifier.pointerInput(
                                item.id + "-drag"
                            ) {
                                detectDragGestures(
                                    onDragStart = {
                                        dragX = 0f
                                        dragY = 0f
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()

                                        dragX += amount.x
                                        dragY += amount.y
                                    },
                                    onDragEnd = {
                                        val columnsMoved =
                                            (
                                                dragX /
                                                    cellWidth.toPx()
                                                ).roundToInt()

                                        val rowsMoved =
                                            (
                                                dragY /
                                                    cellHeight.toPx()
                                                ).roundToInt()

                                        if (
                                            columnsMoved != 0 ||
                                            rowsMoved != 0
                                        ) {
                                            onItemMove(
                                                item,
                                                (
                                                    safeX +
                                                        columnsMoved
                                                ).coerceIn(
                                                    0,
                                                    (
                                                        gridColumns -
                                                            safeWidth
                                                    ).coerceAtLeast(0)
                                                ),
                                                (
                                                    safeY +
                                                        rowsMoved
                                                ).coerceIn(
                                                    0,
                                                    (
                                                        gridRows -
                                                            safeHeight
                                                    ).coerceAtLeast(0)
                                                )
                                            )
                                        }

                                        dragX = 0f
                                        dragY = 0f
                                    },
                                    onDragCancel = {
                                        dragX = 0f
                                        dragY = 0f
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    )

                Box(
                    modifier = itemModifier
                ) {
                    when (item.type) {

                        HomeItemType.CLOCK -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal =
                                            config.itemHorizontalPadding,
                                        vertical =
                                            config.itemVerticalPadding
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                PersonalityClockWidget(
                                    config = config,
                                    modifier = Modifier.fillMaxSize(),
                                    onClockClick = {
                                        onItemClick(item)
                                    }
                                )
                            }
                        }

                        HomeItemType.NOW_BAR -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal =
                                            config.itemHorizontalPadding,
                                        vertical =
                                            config.itemVerticalPadding
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                NowBarView(
                                    items = nowBarItems,
                                    mediaState = mediaState,
                                    profile = profile,
                                    config = config,
                                    onProfileChipClick =
                                        onProfileChipClick,
                                    onMediaPlayToggle =
                                        onMediaPlayToggle,
                                    onSearchClick =
                                        onSearchClick,
                                    onDrawerClick =
                                        onDrawerClick,
                                    onSettingsClick =
                                        onSettingsClick,
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }
                        }

                        HomeItemType.APP -> {
                            val component =
                                if (
                                    !item.packageName
                                        .isNullOrBlank() &&
                                    !item.activityName
                                        .isNullOrBlank()
                                ) {
                                    "${item.packageName}/${item.activityName}"
                                } else {
                                    null
                                }

                            val app = allApps.firstOrNull {
                                it.componentNameString == component
                            }

                            if (app != null) {
                                AppCanvasItem(
                                    app = app,
                                    iconShape = iconShape,
                                    showLabel =
                                        showIconLabels &&
                                            config.showAppLabels,
                                    profile = profile,
                                    config = config,
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }
                        }

                        HomeItemType.WIDGET -> {
                            item.appWidgetId?.let { widgetId ->
                                AndroidAppWidgetHostView(
                                    appWidgetHost =
                                        widgetManager.host,
                                    appWidgetManager =
                                        widgetManager.appWidgetManager,
                                    appWidgetId = widgetId,
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }
                        }

                        HomeItemType.SHORTCUT -> {
                            CanvasPlaceholder(
                                title =
                                    item.title ?: "Shortcut",
                                accent =
                                    appearance.primary,
                                profile = profile
                            )
                        }

                        HomeItemType.FOLDER -> {
                            CanvasPlaceholder(
                                title =
                                    item.title ?: "Folder",
                                accent =
                                    appearance.primary,
                                profile = profile
                            )
                        }
                    }

                    if (editMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        config.cornerRadius
                                    )
                                )
                                .background(
                                    appearance.primary.copy(
                                        alpha = 0.045f
                                    )
                                )
                        )
                    }
                }
            }

        if (editMode && items.isEmpty()) {
            EmptyCanvasHint(
                profile = profile,
                layout = config.homeLayout
            )
        }
    }
}

@Composable
private fun AppCanvasItem(
    app: AppItem,
    iconShape: IconShape,
    showLabel: Boolean,
    profile: LauncherProfile,
    config: ProfileConfig,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current

    val iconScale = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 1.00f
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 0.94f
        HomeLayoutStyle.CALM_MINIMALIST -> 0.88f
        HomeLayoutStyle.FOCUS_DASHBOARD -> 0.92f
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 1.08f
    }

    val labelAlpha = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 0.92f
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 0.82f
        HomeLayoutStyle.CALM_MINIMALIST -> 0.64f
        HomeLayoutStyle.FOCUS_DASHBOARD -> 0.90f
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 1.00f
    }

    val labelWeight = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> FontWeight.Medium
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> FontWeight.Medium
        HomeLayoutStyle.CALM_MINIMALIST -> FontWeight.Normal
        HomeLayoutStyle.FOCUS_DASHBOARD -> FontWeight.SemiBold
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> FontWeight.Bold
    }

    Box(
        modifier = modifier
            .padding(
                horizontal = config.itemHorizontalPadding,
                vertical = config.itemVerticalPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(config.itemSpacing)
        ) {
            Box(
                modifier = Modifier
                    .scale(iconScale)
            ) {
                AppIconImage(
                    drawable = app.icon,
                    label = app.label,
                    iconShape = iconShape,
                    size = config.iconSize,
                    accentColor = appearance.primary
                )
            }

            if (showLabel) {
                Text(
                    text = app.label,
                    color = appearance.onSurface.copy(
                        alpha = labelAlpha
                    ),
                    fontWeight = labelWeight,
                    fontSize = when (config.homeLayout) {
                        HomeLayoutStyle.FLUID_ORGANIC -> 13.sp
                        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 12.sp
                        HomeLayoutStyle.CALM_MINIMALIST -> 12.sp
                        HomeLayoutStyle.FOCUS_DASHBOARD -> 12.sp
                        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 13.sp
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CanvasPlaceholder(
    title: String,
    accent: Color,
    profile: LauncherProfile
) {
    val appearance = LocalLauncherAppearance.current

    val radius = when (profile) {
        LauncherProfile.FLUID -> 24.dp
        LauncherProfile.PREMIUM -> 18.dp
        LauncherProfile.CALM -> 16.dp
        LauncherProfile.FOCUS -> 14.dp
        LauncherProfile.EXPRESSIVE -> 30.dp
    }

    val surfaceAlpha = when (profile) {
        LauncherProfile.FLUID -> 0.055f
        LauncherProfile.PREMIUM -> 0.045f
        LauncherProfile.CALM -> 0.025f
        LauncherProfile.FOCUS -> 0.065f
        LauncherProfile.EXPRESSIVE -> 0.075f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .clip(RoundedCornerShape(radius))
            .background(
                appearance.onSurface.copy(
                    alpha = surfaceAlpha
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(
                        when (profile) {
                            LauncherProfile.EXPRESSIVE -> 44.dp
                            else -> 36.dp
                        }
                    )
                    .height(
                        when (profile) {
                            LauncherProfile.EXPRESSIVE -> 44.dp
                            else -> 36.dp
                        }
                    )
                    .clip(
                        if (profile == LauncherProfile.PREMIUM) {
                            RoundedCornerShape(10.dp)
                        } else {
                            CircleShape
                        }
                    )
                    .background(
                        accent.copy(
                            alpha = when (profile) {
                                LauncherProfile.CALM -> 0.06f
                                LauncherProfile.PREMIUM -> 0.08f
                                LauncherProfile.FOCUS -> 0.10f
                                LauncherProfile.EXPRESSIVE -> 0.16f
                                LauncherProfile.FLUID -> 0.12f
                            }
                        )
                    )
            )

            Text(
                text = title,
                color = appearance.onSurface.copy(
                    alpha = when (profile) {
                        LauncherProfile.CALM -> 0.58f
                        LauncherProfile.PREMIUM -> 0.72f
                        else -> 0.86f
                    }
                ),
                fontWeight = when (profile) {
                    LauncherProfile.CALM -> FontWeight.Normal
                    LauncherProfile.PREMIUM -> FontWeight.Medium
                    LauncherProfile.FOCUS -> FontWeight.SemiBold
                    LauncherProfile.EXPRESSIVE -> FontWeight.Bold
                    LauncherProfile.FLUID -> FontWeight.Medium
                }
            )
        }
    }
}

@Composable
private fun EmptyCanvasHint(
    profile: LauncherProfile,
    layout: HomeLayoutStyle
) {
    val appearance = LocalLauncherAppearance.current

    val text = when (layout) {
        HomeLayoutStyle.FLUID_ORGANIC ->
            "Long press to shape your space"

        HomeLayoutStyle.PREMIUM_ARCHITECTURAL ->
            "Long press to customize"

        HomeLayoutStyle.CALM_MINIMALIST ->
            "Long press"

        HomeLayoutStyle.FOCUS_DASHBOARD ->
            "Long press to configure"

        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE ->
            "Long press to create"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = appearance.onSurface.copy(alpha = 0.34f),
            fontWeight = FontWeight.Medium
        )
    }
}
