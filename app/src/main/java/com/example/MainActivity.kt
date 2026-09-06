package com.example

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.LauncherDependencies
import com.example.ui.home.HomeScreen
import com.example.ui.launcher.LauncherViewModel
import com.example.ui.launcher.LauncherViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity() {

    private val dependencies by lazy {
        LauncherDependencies.get(applicationContext)
    }

    private val launcherViewModel: LauncherViewModel by viewModels {
        LauncherViewModelFactory(dependencies)
    }

    private val widgetScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main.immediate
        )

    private var pendingWidgetId: Int = INVALID_WIDGET_ID

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)


        setContent {
            val state by launcherViewModel.uiState
                .collectAsStateWithLifecycle()

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!state.onboardingCompleted) {
                        com.example.ui.onboarding.OnboardingScreen(
                            onComplete =
                                launcherViewModel::completeOnboarding
                        )
                    } else {
                        HomeScreen(
                            onAddWidget =
                                ::beginWidgetPicker
                        )
                    }
                }
            }
        }
    }

    /**
     * Starts Android's real widget picker.
     *
     * No fake widget catalogue is created by Purple Launcher.
     */
    private fun beginWidgetPicker() {
        if (pendingWidgetId != INVALID_WIDGET_ID) {
            return
        }

        val manager =
            dependencies.widgetManager

        val appWidgetId =
            manager.allocateWidgetId()

        pendingWidgetId = appWidgetId

        try {
            startActivityForResult(
                manager.createPickerIntent(
                    appWidgetId
                ),
                REQUEST_PICK_WIDGET
            )
        } catch (_: Exception) {
            manager.releaseWidgetId(appWidgetId)
            pendingWidgetId = INVALID_WIDGET_ID
        }
    }

    @Deprecated(
        "Use Activity Result APIs when migrating this launcher flow"
    )
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode != REQUEST_PICK_WIDGET &&
            requestCode != REQUEST_CONFIGURE_WIDGET
        ) {
            return
        }

        val widgetId =
            data?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                pendingWidgetId
            ) ?: pendingWidgetId

        if (
            resultCode != Activity.RESULT_OK ||
            widgetId == INVALID_WIDGET_ID
        ) {
            cleanupWidget(widgetId)
            return
        }

        if (
            requestCode == REQUEST_PICK_WIDGET
        ) {
            handlePickedWidget(widgetId)
        } else {
            persistConfiguredWidget(widgetId)
        }
    }

    private fun handlePickedWidget(
        appWidgetId: Int
    ) {
        val manager =
            dependencies.widgetManager

        val info =
            manager.getWidgetInfo(
                appWidgetId
            )

        if (info == null) {
            cleanupWidget(appWidgetId)
            return
        }

        /*
         * The system picker normally binds the selected provider.
         * Verify that the provider is usable. If it is not already
         * bound, attempt an allowed bind.
         */
        val bound =
            manager.appWidgetManager
                .getAppWidgetInfo(appWidgetId) != null ||
                manager.bindWidgetIfAllowed(
                    appWidgetId,
                    info.provider
                )

        if (!bound) {
            cleanupWidget(appWidgetId)
            return
        }

        val configurationIntent =
            manager.createConfigurationIntent(
                appWidgetId,
                info
            )

        if (configurationIntent != null) {
            try {
                startActivityForResult(
                    configurationIntent,
                    REQUEST_CONFIGURE_WIDGET
                )
                return
            } catch (_: Exception) {
                cleanupWidget(appWidgetId)
                return
            }
        }

        persistConfiguredWidget(
            appWidgetId
        )
    }

    private fun persistConfiguredWidget(
        appWidgetId: Int
    ) {
        widgetScope.launch {
            dependencies.widgetManager
                .persistWidget(
                    appWidgetId = appWidgetId,
                    position = Int.MAX_VALUE,
                    colSpan = 2,
                    rowSpan = 1
                )

            pendingWidgetId = INVALID_WIDGET_ID
        }
    }

    private fun cleanupWidget(
        appWidgetId: Int
    ) {
        if (
            appWidgetId != INVALID_WIDGET_ID
        ) {
            dependencies.widgetManager
                .releaseWidgetId(
                    appWidgetId
                )
        }

        pendingWidgetId = INVALID_WIDGET_ID
    }

    override fun onStart() {
        super.onStart()
        dependencies.widgetManager.startListening()
    }

    override fun onStop() {
        dependencies.widgetManager.stopListening()
        super.onStop()
    }

    override fun onDestroy() {
        widgetScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_PICK_WIDGET = 4101
        private const val REQUEST_CONFIGURE_WIDGET = 4102
        private const val INVALID_WIDGET_ID = -1
    }
}
