package com.example.core.apps

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.core.data.repository.IntelligenceRepository
import com.example.core.data.repository.LauncherContentRepository
import com.example.core.data.repository.AppUsageRepository
import com.example.core.model.AppCategory
import com.example.core.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AppManager private constructor(
    private val context: Context,
    private val contentRepository: LauncherContentRepository,
    private val intelligenceRepository: IntelligenceRepository,
    private val usageRepository: AppUsageRepository
) {

    private val appContext = context.applicationContext
    private val packageManager: PackageManager =
        appContext.packageManager

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )

    private val _allApps =
        MutableStateFlow<List<AppItem>>(emptyList())

    val allApps: StateFlow<List<AppItem>> =
        _allApps.asStateFlow()

    private val _categorizedApps =
        MutableStateFlow<Map<AppCategory, List<AppItem>>>(
            emptyMap()
        )

    val categorizedApps:
        StateFlow<Map<AppCategory, List<AppItem>>> =
        _categorizedApps.asStateFlow()

    private val _recentlyLaunched =
        MutableStateFlow<List<AppItem>>(emptyList())

    val recentlyLaunched:
        StateFlow<List<AppItem>> =
        _recentlyLaunched.asStateFlow()

    private val _frequentlyLaunched =
        MutableStateFlow<List<AppItem>>(emptyList())

    val frequentlyLaunched:
        StateFlow<List<AppItem>> =
        _frequentlyLaunched.asStateFlow()

    private val launchHistory =
        mutableMapOf<String, Int>()

    private val lastLaunchTimes =
        mutableMapOf<String, Long>()

    init {
        scope.launch {
            hydrateUsageHistory()
            loadInstalledApps()
        }
    }

    private suspend fun hydrateUsageHistory() {
        val usage = usageRepository.getUsage()

        launchHistory.clear()
        lastLaunchTimes.clear()

        usage.forEach { (componentName, item) ->
            launchHistory[componentName] =
                item.launchCount

            lastLaunchTimes[componentName] =
                item.lastLaunched
        }
    }

    fun loadInstalledApps() {
        scope.launch {

            val apps =
                withContext(Dispatchers.IO) {

                    val launcherIntent =
                        Intent(
                            Intent.ACTION_MAIN,
                            null
                        ).apply {
                            addCategory(
                                Intent.CATEGORY_LAUNCHER
                            )
                        }

                    val resolveInfos:
                        List<ResolveInfo> =
                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.TIRAMISU
                        ) {
                            packageManager
                                .queryIntentActivities(
                                    launcherIntent,
                                    PackageManager
                                        .ResolveInfoFlags
                                        .of(0L)
                                )
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager
                                .queryIntentActivities(
                                    launcherIntent,
                                    0
                                )
                        }

                    val pinnedSet =
                        contentRepository
                            .pinnedApps
                            .first()

                    resolveInfos.mapNotNull { info ->

                        val activityInfo =
                            info.activityInfo
                                ?: return@mapNotNull null

                        val packageName =
                            activityInfo.packageName

                        val activityName =
                            activityInfo.name

                        val label =
                            try {
                                info.loadLabel(
                                    packageManager
                                )
                                    ?.toString()
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: packageName
                            } catch (_: Exception) {
                                packageName
                            }

                        val icon =
                            try {
                                info.loadIcon(
                                    packageManager
                                )
                            } catch (_: Exception) {
                                null
                            }

                        val componentName =
                            "$packageName/$activityName"

                        AppItem(
                            packageName = packageName,
                            activityName = activityName,
                            label = label,
                            icon = icon,
                            category = AppCategorizer.categorize(
                                packageName,
                                label
                            ),
                            launchCount =
                                launchHistory[
                                    componentName
                                ] ?: 0,
                            lastLaunched =
                                lastLaunchTimes[
                                    componentName
                                ] ?: 0L,
                            isPinned =
                                componentName in pinnedSet
                        )
                    }
                }
                    .sortedBy {
                        it.label.lowercase(
                            Locale.getDefault()
                        )
                    }

            _allApps.value = apps

            updateCategories(apps)
            updateFrequentAndRecent(apps)
        }
    }

    private fun updateCategories(
        apps: List<AppItem>
    ) {
        val categorized =
            mutableMapOf<
                AppCategory,
                MutableList<AppItem>
            >()

        AppCategory.values().forEach {
            categorized[it] = mutableListOf()
        }

        categorized[AppCategory.ALL]
            ?.addAll(apps)

        apps.forEach { app ->

            if (app.isPinned) {
                categorized[
                    AppCategory.FAVORITES
                ]?.add(app)
            }

            categorized[
                app.category
            ]?.add(app)
        }

        _categorizedApps.value =
            categorized
    }

    private fun updateFrequentAndRecent(
        apps: List<AppItem>
    ) {
        val frequent =
            apps
                .filter {
                    it.launchCount > 0
                }
                .sortedByDescending {
                    it.launchCount
                }
                .take(8)

        _frequentlyLaunched.value = frequent

        val recent =
            apps
                .filter {
                    it.lastLaunched > 0L
                }
                .sortedByDescending {
                    it.lastLaunched
                }
                .take(6)

        _recentlyLaunched.value = recent
    }

    fun launchApp(
        context: Context,
        app: AppItem
    ) {
        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_LAUNCHER
                    )

                    component =
                        ComponentName(
                            app.packageName,
                            app.activityName
                        )

                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }

            context.startActivity(intent)

            scope.launch {

                if (
                    !intelligenceRepository
                        .trackAppUsage
                        .first()
                ) {
                    return@launch
                }

                val key =
                    app.componentNameString

                val count =
                    launchHistory[key] ?: 0

                val newCount = count + 1
                val now = System.currentTimeMillis()

                launchHistory[key] = newCount
                lastLaunchTimes[key] = now

                usageRepository.recordLaunch(key)

                val updated =
                    _allApps.value.map {
                        if (
                            it.componentNameString ==
                            key
                        ) {
                            it.copy(
                                launchCount =
                                    newCount,
                                lastLaunched =
                                    now
                            )
                        } else {
                            it
                        }
                    }

                _allApps.value =
                    updated

                updateFrequentAndRecent(
                    updated
                )
            }

        } catch (e: ActivityNotFoundException) {

            Log.e(
                "AppManager",
                "Activity not found for ${app.label}",
                e
            )

            Toast.makeText(
                context,
                "Cannot open ${app.label}",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Log.e(
                "AppManager",
                "Error launching ${app.label}",
                e
            )

            Toast.makeText(
                context,
                "Unable to launch app",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {

        @Volatile
        private var instance: AppManager? = null

        fun getInstance(
            context: Context,
            contentRepository: LauncherContentRepository,
            intelligenceRepository: IntelligenceRepository,
            usageRepository: AppUsageRepository
        ): AppManager {

            return instance
                ?: synchronized(this) {

                    instance
                        ?: AppManager(
                            context.applicationContext,
                            contentRepository,
                            intelligenceRepository,
                            usageRepository
                        ).also {
                            instance = it
                        }
                }
        }
    }
}
