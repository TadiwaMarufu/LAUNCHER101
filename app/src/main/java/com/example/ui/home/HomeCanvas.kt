package com.example.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.example.core.model.AppItem
import com.example.core.model.HomeItem
import com.example.core.model.HomeItemType
import com.example.core.model.IconShape
import com.example.core.nowbar.NowBarController
import com.example.core.widgets.AndroidAppWidgetHostView
import com.example.ui.drawer.AppIconImage
import com.example.ui.nowbar.NowBarView
import com.example.ui.widgets.PersonalityClockWidget
import kotlin.math.roundToInt

private const val GRID_COLUMNS = 5
private const val GRID_ROWS = 8

@Composable
fun HomeCanvas(
    items: List<HomeItem>,
    allApps: List<AppItem>,
    iconShape: IconShape,
    showIconLabels: Boolean,
    nowBarController: NowBarController,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    onEmptyTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onItemClick: (HomeItem) -> Unit = {},
    onItemLongPress: (HomeItem) -> Unit = {},
    onItemMove: (HomeItem, Int, Int) -> Unit = {},
    onLaunchApp: (AppItem) -> Unit = {}
) {
    val context = LocalContext.current

    val dependencies = remember {
        LauncherDependencies.get(context)
    }

    val widgetManager = dependencies.widgetManager

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
            .filter {
                it.visible && it.page == 0
            }
            .forEach { item ->

                val itemWidth =
                    cellWidth * item.width

                val itemHeight =
                    cellHeight * item.height

                val xOffset =
                    cellWidth * item.x

                val yOffset =
                    cellHeight * item.y

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
                                    Color.Black.copy(
                                        alpha = 0.07f
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                        .pointerInput(
                            item.id
                        ) {
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
                                        onDrag = { change, dragAmount ->
                                            change.consume()

                                            val columnsMoved =
                                                (
                                                    dragAmount.x /
                                                        cellWidth
                                                            .toPx()
                                                    ).roundToInt()

                                            val rowsMoved =
                                                (
                                                    dragAmount.y /
                                                        cellHeight
                                                            .toPx()
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
                                                        GRID_COLUMNS -
                                                            item.width
                                                    ),
                                                    (
                                                        item.y +
                                                            rowsMoved
                                                    ).coerceIn(
                                                        0,
                                                        GRID_ROWS -
                                                            item.height
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
                                modifier =
                                    Modifier.fillMaxSize()
                            )
                        }

                        HomeItemType.NOW_BAR -> {
                            NowBarView(
                                controller =
                                    nowBarController,
                                modifier =
                                    Modifier.fillMaxSize()
                            )
                        }

                        HomeItemType.APP -> {
                            val app =
                                allApps.firstOrNull {
                                    it.componentNameString ==
                                        "${item.packageName}/${item.activityName}"
                                }

                            if (app != null) {
                                Box(
                                    modifier =
                                        Modifier.fillMaxSize(),
                                    contentAlignment =
                                        Alignment.Center
                                ) {
                                    androidx.compose.foundation.layout.Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {
                                        AppIconImage(
                                            drawable = app.icon,
                                            label = app.label,
                                            iconShape = iconShape,
                                            size = 48.dp
                                        )

                                        if (showIconLabels) {
                                            Text(
                                                text = app.label,
                                                textAlign =
                                                    TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HomeItemType.WIDGET -> {
                            val appWidgetId =
                                item.appWidgetId

                            if (appWidgetId != null) {
                                AndroidAppWidgetHostView(
                                    appWidgetHost =
                                        widgetManager.host,
                                    appWidgetManager =
                                        widgetManager.appWidgetManager,
                                    appWidgetId =
                                        appWidgetId,
                                    modifier =
                                        Modifier.fillMaxSize()
                                )
                            }
                        }

                        HomeItemType.SHORTCUT,
                        HomeItemType.FOLDER -> {
                            Box(
                                modifier =
                                    Modifier.fillMaxSize(),
                                contentAlignment =
                                    Alignment.Center
                            ) {
                                Text(
                                    text =
                                        item.title
                                            ?: item.type.name
                                                .lowercase()
                                                .replaceFirstChar {
                                                    it.uppercase()
                                                }
                                )
                            }
                        }
                    }
                }
            }
    }
}

private fun Modifier.thenIf(
    condition: Boolean,
    modifier: Modifier
): Modifier {
    return if (condition) {
        this.then(modifier)
    } else {
        this
    }
}
