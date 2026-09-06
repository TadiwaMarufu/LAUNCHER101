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
import com.example.core.data.LauncherPreferences
import com.example.core.model.AppCategory
import com.example.core.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AppManager private constructor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val preferences = LauncherPreferences.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _allApps = MutableStateFlow<List<AppItem>>(emptyList())
    val allApps: StateFlow<List<AppItem>> = _allApps.asStateFlow()

    private val _categorizedApps = MutableStateFlow<Map<AppCategory, List<AppItem>>>(emptyMap())
    val categorizedApps: StateFlow<Map<AppCategory, List<AppItem>>> = _categorizedApps.asStateFlow()

    private val _recentlyLaunched = MutableStateFlow<List<AppItem>>(emptyList())
    val recentlyLaunched: StateFlow<List<AppItem>> = _recentlyLaunched.asStateFlow()

    private val _frequentlyLaunched = MutableStateFlow<List<AppItem>>(emptyList())
    val frequentlyLaunched: StateFlow<List<AppItem>> = _frequentlyLaunched.asStateFlow()

    private val launchHistory = mutableMapOf<String, Int>()

    init {
        loadInstalledApps()
    }

    /**
     * Discovers all launchable applications installed on the Android device.
     */
    fun loadInstalledApps() {
        scope.launch {
            val apps = withContext(Dispatchers.IO) {
                val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentActivities(
                        launcherIntent,
                        PackageManager.ResolveInfoFlags.of(0L)
                    )
                } else {
                    packageManager.queryIntentActivities(launcherIntent, 0)
                }

                val currentPackage = context.packageName
                val pinnedSet = preferences.pinnedApps.value

                val appList = mutableListOf<AppItem>()
                for (info in resolveInfos) {
                    val pkgName = info.activityInfo.packageName
                    // Skip the launcher itself in drawer to avoid confusion, or keep it if needed
                    val activityName = info.activityInfo.name
                    val label = try {
                        info.loadLabel(packageManager).toString()
                    } catch (_: Exception) {
                        pkgName
                    }
                    val icon = try {
                        info.loadIcon(packageManager)
                    } catch (_: Exception) {
                        null
                    }
                    val componentName = "$pkgName/$activityName"
                    val isPinned = pinnedSet.contains(componentName)
                    val category = categorizeApp(pkgName, label)

                    appList.add(
                        AppItem(
                            packageName = pkgName,
                            activityName = activityName,
                            label = label.ifBlank { pkgName },
                            icon = icon,
                            category = category,
                            launchCount = launchHistory[componentName] ?: 0,
                            isPinned = isPinned
                        )
                    )
                }

                appList.sortedBy { it.label.lowercase(Locale.getDefault()) }
            }

            _allApps.value = apps
            updateCategories(apps)
            updateFrequentAndRecent(apps)
        }
    }

    private fun updateCategories(apps: List<AppItem>) {
        val categorized = mutableMapOf<AppCategory, MutableList<AppItem>>()
        for (category in AppCategory.values()) {
            categorized[category] = mutableListOf()
        }

        // All apps always in ALL
        categorized[AppCategory.ALL]?.addAll(apps)

        for (app in apps) {
            if (app.isPinned) {
                categorized[AppCategory.FAVORITES]?.add(app)
            }
            categorized[app.category]?.add(app)
        }

        _categorizedApps.value = categorized
    }

    private fun updateFrequentAndRecent(apps: List<AppItem>) {
        val frequent = apps.filter { it.launchCount > 0 }
            .sortedByDescending { it.launchCount }
            .take(8)
        _frequentlyLaunched.value = if (frequent.isNotEmpty()) frequent else apps.take(8)

        val recent = apps.filter { it.lastLaunched > 0L }
            .sortedByDescending { it.lastLaunched }
            .take(6)
        _recentlyLaunched.value = if (recent.isNotEmpty()) recent else apps.take(6)
    }

    /**
     * Intelligently classifies an application into a functional category based on package & label.
     */
    private fun categorizeApp(packageName: String, label: String): AppCategory {
        val pkg = packageName.lowercase(Locale.ROOT)
        val name = label.lowercase(Locale.ROOT)

        return when {
            pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("contacts") ||
            pkg.contains("message") || pkg.contains("sms") || pkg.contains("whatsapp") ||
            pkg.contains("telegram") || pkg.contains("signal") || pkg.contains("messenger") ||
            pkg.contains("chat") || name.contains("phone") || name.contains("contacts") ||
            name.contains("messages") || name.contains("whatsapp") || name.contains("chat") -> AppCategory.COMMUNICATION

            pkg.contains("instagram") || pkg.contains("tiktok") || pkg.contains("twitter") ||
            pkg.contains("facebook") || pkg.contains("reddit") || pkg.contains("snapchat") ||
            pkg.contains("linkedin") || pkg.contains("threads") || pkg.contains("bereal") ||
            pkg.contains("pinterest") || name.contains("instagram") || name.contains("tiktok") ||
            name.contains("reddit") || name.contains("twitter") || name.contains("facebook") -> AppCategory.SOCIAL

            pkg.contains("spotify") || pkg.contains("music") || pkg.contains("youtube") ||
            pkg.contains("video") || pkg.contains("player") || pkg.contains("netflix") ||
            pkg.contains("camera") || pkg.contains("gallery") || pkg.contains("photo") ||
            pkg.contains("twitch") || pkg.contains("podcast") || pkg.contains("sound") ||
            name.contains("camera") || name.contains("gallery") || name.contains("music") ||
            name.contains("photos") || name.contains("youtube") || name.contains("spotify") -> AppCategory.MEDIA

            pkg.contains("docs") || pkg.contains("sheets") || pkg.contains("slides") ||
            pkg.contains("drive") || pkg.contains("keep") || pkg.contains("note") ||
            pkg.contains("word") || pkg.contains("excel") || pkg.contains("pdf") ||
            pkg.contains("office") || pkg.contains("mail") || pkg.contains("gmail") ||
            pkg.contains("outlook") || pkg.contains("calendar") || pkg.contains("notion") ||
            pkg.contains("trello") || pkg.contains("zoom") || pkg.contains("teams") ||
            name.contains("notes") || name.contains("calendar") || name.contains("mail") ||
            name.contains("gmail") || name.contains("clock") || name.contains("drive") -> AppCategory.WORK

            pkg.contains("game") || pkg.contains("arcade") || pkg.contains("puzzle") ||
            pkg.contains("craft") || pkg.contains("roblox") || pkg.contains("chess") ||
            name.contains("game") -> AppCategory.GAMES

            pkg.contains("calc") || pkg.contains("browser") || pkg.contains("chrome") ||
            pkg.contains("firefox") || pkg.contains("maps") || pkg.contains("weather") ||
            pkg.contains("vending") || pkg.contains("store") || pkg.contains("clock") ||
            pkg.contains("recorder") || pkg.contains("file") || pkg.contains("settings") ||
            name.contains("calculator") || name.contains("chrome") || name.contains("browser") ||
            name.contains("maps") || name.contains("weather") || name.contains("files") ||
            name.contains("settings") -> AppCategory.TOOLS

            else -> AppCategory.SYSTEM
        }
    }

    /**
     * Safely launches an application intent without crashing the launcher.
     */
    fun launchApp(context: Context, app: AppItem) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)

            // Record telemetry locally for adaptive intelligence if permitted
            if (preferences.trackAppUsage.value) {
                val componentKey = app.componentNameString
                val currentCount = launchHistory[componentKey] ?: 0
                launchHistory[componentKey] = currentCount + 1

                val updatedList = _allApps.value.map {
                    if (it.componentNameString == componentKey) {
                        it.copy(launchCount = currentCount + 1, lastLaunched = System.currentTimeMillis())
                    } else it
                }
                _allApps.value = updatedList
                updateFrequentAndRecent(updatedList)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e("AppManager", "Activity not found for ${app.label}", e)
            Toast.makeText(context, "Cannot open ${app.label}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("AppManager", "Error launching ${app.label}", e)
            Toast.makeText(context, "Unable to launch app", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppManager? = null

        fun getInstance(context: Context): AppManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
