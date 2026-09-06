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

class LauncherDependencies private constructor(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Single persistent data source for the entire launcher.
     *
     * Every repository and manager must use this instance.
     * This prevents multiple DataStore graphs from being created.
     */
    val dataStore = LauncherDataStore(appContext)

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
            contentRepository = launcherContentRepository,
            intelligenceRepository = intelligenceRepository,
            usageRepository = appUsageRepository
        )

    companion object {

        @Volatile
        private var instance: LauncherDependencies? = null

        fun get(context: Context): LauncherDependencies {
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
