package com.example.core.widgets

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.data.store.LauncherDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Real Android App Widget host used by Purple Launcher.
 *
 * Lifecycle:
 * picker -> allocate -> bind -> configure -> persist -> render
 * remove -> delete -> release
 */
class PurpleAppWidgetHost(
    context: Context,
    hostId: Int = HOST_ID
) : AppWidgetHost(context.applicationContext, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return super.onCreateView(
            context,
            appWidgetId,
            appWidget
        )
    }

    companion object {
        const val HOST_ID = 1024
    }
}

data class PlacedWidget(
    val id: String,
    val appWidgetId: Int,
    val providerPackage: String,
    val providerClass: String,
    val label: String,
    val position: Int = 0,
    val colSpan: Int = 2,
    val rowSpan: Int = 1
) {
    val provider: ComponentName
        get() = ComponentName(
            providerPackage,
            providerClass
        )
}

class WidgetManager(
    context: Context,
    private val dataStore: LauncherDataStore
) {

    private val appContext = context.applicationContext

    val appWidgetManager: AppWidgetManager =
        AppWidgetManager.getInstance(appContext)

    val host = PurpleAppWidgetHost(appContext)

    val placedWidgets: Flow<List<PlacedWidget>> =
        dataStore.widgetPlacements

    fun startListening() {
        host.startListening()
    }

    fun stopListening() {
        host.stopListening()
    }

    /**
     * Allocate a real Android widget ID.
     */
    fun allocateWidgetId(): Int {
        return host.allocateAppWidgetId()
    }

    /**
     * Release a widget ID that is no longer used.
     */
    fun releaseWidgetId(appWidgetId: Int) {
        host.deleteAppWidgetId(appWidgetId)
    }

    /**
     * Check whether the launcher is allowed to bind this provider.
     */
    fun bindWidgetIfAllowed(
        appWidgetId: Int,
        provider: ComponentName
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appWidgetManager.bindAppWidgetIdIfAllowed(
                appWidgetId,
                provider
            )
        } else {
            true
        }
    }

    /**
     * Build the system widget picker intent.
     */
    fun createPickerIntent(
        appWidgetId: Int
    ): Intent {
        return Intent(
            AppWidgetManager.ACTION_APPWIDGET_PICK
        ).apply {
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId
            )
        }
    }

    /**
     * Build the provider-specific configuration intent.
     *
     * Returns null when the provider has no configuration activity.
     */
    fun createConfigurationIntent(
        appWidgetId: Int,
        info: AppWidgetProviderInfo
    ): Intent? {
        val configure = info.configure ?: return null

        return Intent().apply {
            component = configure
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId
            )
        }
    }

    fun getWidgetInfo(
        appWidgetId: Int
    ): AppWidgetProviderInfo? {
        return appWidgetManager.getAppWidgetInfo(
            appWidgetId
        )
    }

    fun createHostView(
        context: Context,
        appWidgetId: Int
    ): AppWidgetHostView? {
        val info = getWidgetInfo(appWidgetId)
            ?: return null

        return host.createView(
            context,
            appWidgetId,
            info
        ).apply {
            setAppWidget(
                appWidgetId,
                info
            )
        }
    }

    suspend fun persistWidget(
        appWidgetId: Int,
        position: Int = 0,
        colSpan: Int = 2,
        rowSpan: Int = 1
    ): PlacedWidget? {

        val info = getWidgetInfo(appWidgetId)
            ?: return null

        val widget = PlacedWidget(
            id = "android-$appWidgetId",
            appWidgetId = appWidgetId,
            providerPackage =
                info.provider.packageName,
            providerClass =
                info.provider.className,
            label =
                info.loadLabel(appContext.packageManager)
                    .toString(),
            position = position,
            colSpan = colSpan,
            rowSpan = rowSpan
        )

        val current =
            dataStore.widgetPlacements.first()

        val updated =
            current
                .filterNot {
                    it.appWidgetId == appWidgetId
                }
                .toMutableList()

        updated.add(widget)

        dataStore.setWidgetPlacements(updated)

        return widget
    }

    suspend fun removeWidget(
        appWidgetId: Int
    ) {
        val current =
            dataStore.widgetPlacements.first()

        dataStore.setWidgetPlacements(
            current.filterNot {
                it.appWidgetId == appWidgetId
            }
        )

        releaseWidgetId(appWidgetId)
    }

    suspend fun updateWidgetPlacement(
        appWidgetId: Int,
        position: Int,
        colSpan: Int,
        rowSpan: Int
    ) {
        val current =
            dataStore.widgetPlacements.first()

        val updated =
            current.map { widget ->
                if (widget.appWidgetId == appWidgetId) {
                    widget.copy(
                        position = position,
                        colSpan = colSpan,
                        rowSpan = rowSpan
                    )
                } else {
                    widget
                }
            }

        dataStore.setWidgetPlacements(updated)
    }
}

enum class NativeWidgetType(
    val title: String,
    val subtitle: String
) {
    CLOCK(
        "Frequency Clock",
        "Dynamic time & date presentation"
    ),
    WEATHER(
        "Atmosphere Weather",
        "Glanceable conditions and temperature"
    ),
    CALENDAR(
        "Agenda & Flow",
        "Daily schedule and upcoming events"
    ),
    MUSIC(
        "Audio Visualizer",
        "Interactive playback controller"
    ),
    BATTERY(
        "Power Gauge",
        "Battery level and charging status"
    ),
    TASKS(
        "Focus Checklist",
        "Actionable task management"
    ),
    SCREEN_TIME(
        "Digital Rhythm",
        "Screen time & mindful usage"
    ),
    SYSTEM_INFO(
        "Engine Stats",
        "RAM, storage, and device telemetry"
    )
}

@Composable
fun AndroidAppWidgetHostView(
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            val info =
                appWidgetManager.getAppWidgetInfo(
                    appWidgetId
                )

            if (info != null) {
                appWidgetHost
                    .createView(
                        context,
                        appWidgetId,
                        info
                    )
                    .apply {
                        setAppWidget(
                            appWidgetId,
                            info
                        )
                    }
            } else {
                View(context)
            }
        },
        modifier = modifier
    )
}
