package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.core.LauncherDependencies
import com.example.core.engine.ProfileConfig
import com.example.core.model.AppItem
import com.example.core.model.HomeItem
import com.example.core.model.HomeItemType
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import com.example.core.model.MediaPlaybackState
import com.example.core.model.NowBarItem
import com.example.core.nowbar.NowBarController
import com.example.core.widgets.AndroidAppWidgetHostView
import com.example.ui.drawer.AppIconImage
import com.example.ui.nowbar.NowBarView
import com.example.ui.widgets.PersonalityClockWidget
import kotlin.math.roundToInt

private const val GRID_COLUMNS = 5
private const val GRID_ROWS = 8

/**
 * User-owned launcher canvas.
 *
 * The canvas deliberately contains no hard-coded home components.
 * Everything visible on the home screen must exist as a HomeItem.
 *
 * Profiles influence presentation through the ProfileConfig supplied
 * by the parent, but profiles do not inject content into the canvas.
 */
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
    onLaunchApp: (AppItem) -> Unit = {},
    onProfileChipClick: () -> Unit = {},
    onMediaPlayToggle: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onDrawerClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
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
                    onLongPress = {
                        onLongPress()
                    },
                    onTap = {
                        if (editMode) {
                            onEmptyTap()
                        }
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

                val x = item.x.coerceIn(
                    0,
                    (GRID_COLUMNS - safeWidth).coerceAtLeast(0)
                )

                val y = item.y.coerceIn(
                    0,
                    (GRID_ROWS - safeHeight).coerceAtLeast(0)
                )

                val xOffset = cellWidth * x
                val yOffset = cellHeight * y

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = xOffset.roundToPx(),
                                y = yOffset.roundToPx()
                            )
                        }
                        .width(itemWidth)
                        .height(itemHeight)
                        .clip(
                            RoundedCornerShape(
                                if (editMode) 18.dp else 0.dp
                            )
                        )
                        .then(
                            if (editMode) {
                                Modifier.background(
                                    Color.Black.copy(alpha = 0.07f)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .pointerInput(item.id) {
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
                                Modifier.pointerInput(item.id + "-drag") {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()

                                            val columnsMoved =
                                                (
                                                    dragAmount.x /
                                                        cellWidth.toPx()
                                                    ).roundToInt()

                                            val rowsMoved =
                                                (
                                                    dragAmount.y /
                                                        cellHeight.toPx()
                                                    ).roundToInt()

                                            if (
                                                columnsMoved != 0 ||
                                                rowsMoved != 0
                                            ) {
                                                onItemMove(
                                                    item,
                                                    (
                                                        item.x +
                                                            columnsMoved
                                                    ).coerceIn(
                                                        0,
                                                        (
                                                            GRID_COLUMNS -
                                                                safeWidth
                                                        ).coerceAtLeast(0)
                                                    ),
                                                    (
                                                        item.y +
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
                                        }
                                    )
                                }
                            } else {
                                Modifier
                            }
                            )
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
                                onProfileChipClick = onProfileChipClick,
                                onMediaPlayToggle = onMediaPlayToggle,
                                onSearchClick = onSearchClick,
                                onDrawerClick = onDrawerClick,
                                onSettingsClick = onSettingsClick,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        HomeItemType.APP -> {
                            val componentName =
                                if (
                                    !item.packageName.isNullOrBlank() &&
                                    !item.activityName.isNullOrBlank()
                                ) {
                                    "${item.packageName}/${item.activityName}"
                                } else {
                                    null
                                }

                            val app = allApps.firstOrNull {
                                it.componentNameString == componentName
                            }

                            if (app != null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {
                                        AppIconImage(
                                            drawable = app.icon,
                                            label = app.label,
                                            iconShape = iconShape,
                                            size = 48.dp,
                                            accentColor =
                                                profile.primaryAccent
                                        )

                                        if (showIconLabels) {
                                            Text(
                                                text = app.label,
                                                textAlign =
                                                    TextAlign.Center,
                                                color =
                                                    profile.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HomeItemType.WIDGET -> {
                            val appWidgetId = item.appWidgetId

                            if (appWidgetId != null) {
                                AndroidAppWidgetHostView(
                                    appWidgetHost = widgetManager.host,
                                    appWidgetManager =
                                        widgetManager.appWidgetManager,
                                    appWidgetId = appWidgetId,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        HomeItemType.SHORTCUT -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.title ?: "Shortcut",
                                    color = profile.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        HomeItemType.FOLDER -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.title ?: "Folder",
                                    color = profile.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
    }
}
