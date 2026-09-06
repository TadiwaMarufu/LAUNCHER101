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

private const val GRID_COLUMNS = 5
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
        val cellWidth = maxWidth / GRID_COLUMNS
        val cellHeight = maxHeight / GRID_ROWS

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
                    (GRID_COLUMNS - safeWidth).coerceAtLeast(0)
                )

                val safeY = item.y.coerceIn(
                    0,
                    (GRID_ROWS - safeHeight).coerceAtLeast(0)
                )

                val xOffset = cellWidth * safeX
                val yOffset = cellHeight * safeY

                var dragX by remember(item.id) {
                    mutableFloatStateOf(0f)
                }

                var dragY by remember(item.id) {
                    mutableFloatStateOf(0f)
                }

                val scale by animateFloatAsState(
                    targetValue = if (editMode) 0.96f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "home-item-scale"
                )

                val itemModifier = Modifier
                    .offset {
                        IntOffset(
                            x = xOffset.roundToPx() + dragX.roundToInt(),
                            y = yOffset.roundToPx() + dragY.roundToInt()
                        )
                    }
                    .width(itemWidth)
                    .height(itemHeight)
                    .scale(scale)
                    .clip(
                        RoundedCornerShape(
                            config.cornerRadius
                        )
                    )
                    .then(
                        if (editMode) {
                            Modifier.background(
                                appearance.primary.copy(alpha = 0.08f)
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
                                                        GRID_COLUMNS -
                                                            safeWidth
                                                    ).coerceAtLeast(0)
                                                ),
                                                (
                                                    safeY +
                                                        rowsMoved
                                                ).coerceIn(
                                                    0,
                                                    (
                                                        GRID_ROWS -
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
                            PersonalityClockWidget(
                                config = config,
                                modifier = Modifier.fillMaxSize(),
                                onClockClick = {
                                    onItemClick(item)
                                }
                            )
                        }

                        HomeItemType.NOW_BAR -> {
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
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current

    Box(
        modifier = modifier.padding(
            horizontal = 6.dp,
            vertical = 8.dp
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            AppIconImage(
                drawable = app.icon,
                label = app.label,
                iconShape = iconShape,
                size = 54.dp,
                accentColor = appearance.primary
            )

            if (showLabel) {
                Text(
                    text = app.label,
                    color = appearance.onSurface,
                    fontWeight = FontWeight.Medium,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                appearance.onSurface.copy(alpha = 0.045f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(
                        accent.copy(alpha = 0.12f)
                    )
            )

            Text(
                text = title,
                color = appearance.onSurface,
                fontWeight = FontWeight.Medium
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
