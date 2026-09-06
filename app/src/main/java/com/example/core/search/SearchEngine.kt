package com.example.core.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.apps.AppManager
import com.example.core.data.LauncherPreferences
import com.example.core.model.AppItem
import com.example.core.model.LauncherProfile
import java.util.Locale

sealed class SearchResultItem {
    abstract val title: String
    abstract val subtitle: String
    abstract val icon: ImageVector

    data class AppResult(
        val app: AppItem,
        override val title: String = app.label,
        override val subtitle: String = "Application",
        override val icon: ImageVector = Icons.Rounded.Search
    ) : SearchResultItem()

    data class CommandResult(
        override val title: String,
        override val subtitle: String,
        override val icon: ImageVector,
        val onExecute: (Context) -> Unit
    ) : SearchResultItem()
}

class SearchEngine(private val context: Context) {

    private val appManager = AppManager.getInstance(context)
    private val preferences = LauncherPreferences.getInstance(context)

    fun executeSearch(rawQuery: String): List<SearchResultItem> {
        val query = rawQuery.trim().lowercase(Locale.getDefault())
        if (query.isEmpty()) {
            return getSuggestions()
        }

        val results = mutableListOf<SearchResultItem>()

        // 1. Profile Switch Command
        for (profile in LauncherProfile.values()) {
            val name = profile.name.lowercase(Locale.getDefault())
            if (query == name || query == "profile $name" || query == "switch $name") {
                results.add(
                    SearchResultItem.CommandResult(
                        title = "Switch to ${profile.title} Personality",
                        subtitle = profile.tagline,
                        icon = Icons.Rounded.Palette,
                        onExecute = {
                            preferences.setActiveProfile(profile)
                        }
                    )
                )
            }
        }

        // 2. Timer / Alarm command
        if (query.startsWith("timer ") || query.startsWith("t ")) {
            val minutesStr = query.replace("timer ", "").replace("t ", "").replace("m", "").trim()
            val minutes = minutesStr.toIntOrNull() ?: 10
            results.add(
                SearchResultItem.CommandResult(
                    title = "Set $minutes-Minute Timer",
                    subtitle = "System Clock Action",
                    icon = Icons.Rounded.Timer,
                    onExecute = { ctx ->
                        try {
                            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                                putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                                putExtra(AlarmClock.EXTRA_MESSAGE, "Purple Launcher Focus")
                                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            ctx.startActivity(intent)
                        } catch (_: Exception) {
                            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            ctx.startActivity(clockIntent)
                        }
                    }
                )
            )
        }

        // 3. System Settings shortcuts
        when (query) {
            "wifi", "wi-fi", "internet" -> results.add(
                SearchResultItem.CommandResult(
                    title = "Wi-Fi Settings",
                    subtitle = "System Network",
                    icon = Icons.Rounded.Settings,
                    onExecute = { it.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )
            )
            "bluetooth", "bt" -> results.add(
                SearchResultItem.CommandResult(
                    title = "Bluetooth Settings",
                    subtitle = "System Wireless",
                    icon = Icons.Rounded.Settings,
                    onExecute = { it.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )
            )
            "display", "brightness", "screen" -> results.add(
                SearchResultItem.CommandResult(
                    title = "Display Settings",
                    subtitle = "System Display & Brightness",
                    icon = Icons.Rounded.Settings,
                    onExecute = { it.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )
            )
            "battery", "power" -> results.add(
                SearchResultItem.CommandResult(
                    title = "Battery Settings",
                    subtitle = "System Battery & Usage",
                    icon = Icons.Rounded.Settings,
                    onExecute = { it.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )
            )
            "settings" -> results.add(
                SearchResultItem.CommandResult(
                    title = "System Settings",
                    subtitle = "Open Android Settings",
                    icon = Icons.Rounded.Settings,
                    onExecute = { it.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                )
            )
        }

        // 4. Calculator expression evaluator
        val mathMatch = Regex("""^(?:calc\s+)?([0-9\.\+\-\*\/\s\(\)]+)$""").find(query)
        if (mathMatch != null && (query.contains("+") || query.contains("-") || query.contains("*") || query.contains("/") || query.startsWith("calc "))) {
            val expr = mathMatch.groupValues[1].trim()
            val mathResult = evaluateSimpleMath(expr)
            if (mathResult != null) {
                results.add(
                    SearchResultItem.CommandResult(
                        title = "= $mathResult",
                        subtitle = "Calculation: $expr",
                        icon = Icons.Rounded.Calculate,
                        onExecute = { }
                    )
                )
            }
        }

        // 5. Installed Applications
        val matchingApps = appManager.allApps.value.filter {
            it.label.lowercase(Locale.getDefault()).contains(query) ||
                    it.packageName.lowercase(Locale.getDefault()).contains(query)
        }
        for (app in matchingApps.take(8)) {
            results.add(SearchResultItem.AppResult(app = app))
        }

        // 6. Web Search Fallback
        results.add(
            SearchResultItem.CommandResult(
                title = "Search \"$rawQuery\" on Google",
                subtitle = "Web Search",
                icon = Icons.Rounded.Language,
                onExecute = { ctx ->
                    try {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(rawQuery))).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        ctx.startActivity(webIntent)
                    } catch (_: Exception) { }
                }
            )
        )

        return results
    }

    private fun getSuggestions(): List<SearchResultItem> {
        val list = mutableListOf<SearchResultItem>()
        // Quick personality triggers
        list.add(
            SearchResultItem.CommandResult(
                title = "Switch Personality",
                subtitle = "Type 'fluid', 'calm', 'focus', 'premium', or 'expressive'",
                icon = Icons.Rounded.Palette,
                onExecute = { preferences.cycleNextProfile() }
            )
        )
        list.add(
            SearchResultItem.CommandResult(
                title = "Start 25m Focus Timer",
                subtitle = "Type 'timer 25m'",
                icon = Icons.Rounded.Timer,
                onExecute = { ctx ->
                    try {
                        ctx.startActivity(Intent(AlarmClock.ACTION_SET_TIMER).apply {
                            putExtra(AlarmClock.EXTRA_LENGTH, 25 * 60)
                            putExtra(AlarmClock.EXTRA_MESSAGE, "Purple Focus")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } catch (_: Exception) { }
                }
            )
        )
        // Top 4 apps
        for (app in appManager.frequentlyLaunched.value.take(4)) {
            list.add(SearchResultItem.AppResult(app))
        }
        return list
    }

    private fun evaluateSimpleMath(expr: String): String? {
        return try {
            val clean = expr.replace(" ", "")
            when {
                "+" in clean -> {
                    val p = clean.split("+")
                    (p[0].toDouble() + p[1].toDouble()).toString().removeSuffix(".0")
                }
                "*" in clean -> {
                    val p = clean.split("*")
                    (p[0].toDouble() * p[1].toDouble()).toString().removeSuffix(".0")
                }
                "/" in clean -> {
                    val p = clean.split("/")
                    val denom = p[1].toDouble()
                    if (denom != 0.0) (p[0].toDouble() / denom).toString().removeSuffix(".0") else "Cannot divide by 0"
                }
                "-" in clean -> {
                    val p = clean.split("-")
                    (p[0].toDouble() - p[1].toDouble()).toString().removeSuffix(".0")
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
