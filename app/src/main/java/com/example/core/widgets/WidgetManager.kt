package com.example.core.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * AppWidgetHost instance for Purple Launcher to host real Android widgets.
 */
class PurpleAppWidgetHost(context: Context, hostId: Int = 1024) : AppWidgetHost(context, hostId) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return super.onCreateView(context, appWidgetId, appWidget)
    }
}

/**
 * Native Purple Launcher Widget Types (Layer 2).
 */
enum class NativeWidgetType(val title: String, val subtitle: String) {
    CLOCK("Frequency Clock", "Dynamic time & date presentation"),
    WEATHER("Atmosphere Weather", "Glanceable conditions and temperature"),
    CALENDAR("Agenda & Flow", "Daily schedule and upcoming events"),
    MUSIC("Audio Visualizer", "Interactive playback controller"),
    BATTERY("Power Gauge", "Battery level and charging status"),
    TASKS("Focus Checklist", "Actionable task management"),
    SCREEN_TIME("Digital Rhythm", "Screen time & mindful usage"),
    SYSTEM_INFO("Engine Stats", "RAM, storage, and device telemetry")
}

/**
 * Model for an active placed widget on the home screen.
 */
data class PlacedWidget(
    val id: String,
    val isAndroidAppWidget: Boolean = false,
    val appWidgetId: Int = -1,
    val nativeType: NativeWidgetType? = null,
    val colSpan: Int = 2,
    val rowSpan: Int = 1
)

/**
 * Composable for embedding real Android AppWidgets.
 */
@Composable
fun AndroidAppWidgetHostView(
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
            if (appWidgetInfo != null) {
                appWidgetHost.createView(context, appWidgetId, appWidgetInfo).apply {
                    setAppWidget(appWidgetId, appWidgetInfo)
                }
            } else {
                View(context)
            }
        },
        modifier = modifier
    )
}
