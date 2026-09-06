package com.example.core

import android.content.Context
import com.example.core.apps.AppManager
import com.example.core.data.repository.AppearanceRepository
import com.example.core.data.repository.AppUsageRepository
import com.example.core.data.repository.GestureRepository
import com.example.core.data.repository.IntelligenceRepository
import com.example.core.data.repository.LauncherContentRepository
import com.example.core.data.repository.LauncherStateRepository
import com.example.core.data.repository.ProfileRepository
import com.example.core.data.store.LauncherDataStore
import com.example.core.widgets.WidgetManager

class LauncherDependencies private constructor(context: Context) {

    private val appContext = context.applicationContext

    val dataStore =
        LauncherDataStore(appContext)

    val profileRepository =
        ProfileRepository(dataStore)

    val appearanceRepository =
        AppearanceRepository(dataStore)

    val gestureRepository =
        GestureRepository(dataStore)

    val intelligenceRepository =
        IntelligenceRepository(dataStore)

    val launcherStateRepository =
        LauncherStateRepository(dataStore)

    val launcherContentRepository =
        LauncherContentRepository(dataStore)

    val appUsageRepository =
        AppUsageRepository(dataStore)

    val appManager =
        AppManager.getInstance(
            context = appContext,
            contentRepository =
                launcherContentRepository,
            intelligenceRepository =
                intelligenceRepository,
            usageRepository =
                appUsageRepository
        )

    /**
     * Single real Android App Widget host.
     *
     * The same manager is shared by Activity/Home so widget IDs,
     * persistence and lifecycle all belong to one launcher graph.
     */
    val widgetManager =
        WidgetManager(
            context = appContext,
            dataStore = dataStore
        )

    companion object {

        @Volatile
        private var instance:
            LauncherDependencies? = null

        fun get(
            context: Context
        ): LauncherDependencies {

            return instance
                ?: synchronized(this) {

                    instance
                        ?: LauncherDependencies(
                            context.applicationContext
                        ).also {
                            instance = it
                        }
                }
        }
    }
}
