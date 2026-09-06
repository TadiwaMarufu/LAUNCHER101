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
import com.example.core.LauncherDependencies
import com.example.core.apps.AppManager
import com.example.core.data.repository.ProfileRepository
import com.example.core.model.AppItem
import com.example.core.model.LauncherProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

/**
 * Local-first universal launcher search.
 *
 * Supported v0.1 sources:
 * - installed applications
 * - launcher personalities
 * - Android settings
 * - timer commands
 * - calculator expressions
 * - explicit web fallback
 */
class SearchEngine(
    context: Context,
    private val profileRepository: ProfileRepository =
        LauncherDependencies
            .get(context)
            .profileRepository,
    private val appManager: AppManager =
        LauncherDependencies
            .get(context)
            .appManager
) {

    private val commandScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main.immediate
        )

    fun executeSearch(
        rawQuery: String
    ): List<SearchResultItem> {

        val trimmedQuery =
            rawQuery.trim()

        if (trimmedQuery.isEmpty()) {
            return getSuggestions()
        }

        val query =
            trimmedQuery.lowercase(
                Locale.getDefault()
            )

        val results =
            mutableListOf<SearchResultItem>()

        addProfileCommands(
            query,
            results
        )

        addTimerCommand(
            query,
            results
        )

        addSettingsCommand(
            query,
            results
        )

        addCalculatorResult(
            query,
            results
        )

        addAppResults(
            query,
            results
        )

        results.add(
            SearchResultItem.CommandResult(
                title =
                    "Search \"$trimmedQuery\" on Google",
                subtitle = "Web Search",
                icon = Icons.Rounded.Language,
                onExecute = { context ->
                    openWebSearch(
                        context,
                        trimmedQuery
                    )
                }
            )
        )

        return results
    }

    private fun addProfileCommands(
        query: String,
        results: MutableList<SearchResultItem>
    ) {
        for (profile in LauncherProfile.entries) {
            val name =
                profile.name.lowercase(
                    Locale.getDefault()
                )

            if (
                query == name ||
                query == "profile $name" ||
                query == "switch $name"
            ) {
                results.add(
                    SearchResultItem.CommandResult(
                        title =
                            "Switch to ${profile.title} Personality",
                        subtitle =
                            profile.tagline,
                        icon =
                            Icons.Rounded.Palette,
                        onExecute = {
                            commandScope.launch {
                                profileRepository
                                    .setProfile(profile)
                            }
                        }
                    )
                )
            }
        }
    }

    private fun addTimerCommand(
        query: String,
        results: MutableList<SearchResultItem>
    ) {
        val minutes =
            parseTimerMinutes(query)
                ?: return

        results.add(
            SearchResultItem.CommandResult(
                title =
                    "Set $minutes-Minute Timer",
                subtitle =
                    "System Clock Action",
                icon =
                    Icons.Rounded.Timer,
                onExecute = { context ->
                    openTimer(
                        context,
                        minutes
                    )
                }
            )
        )
    }

    private fun parseTimerMinutes(
        query: String
    ): Int? {
        val match =
            Regex(
                """^(?:timer|t)\s+(\d+)\s*(?:m|min|mins|minute|minutes)?$"""
            ).find(query)
                ?: return null

        return match
            .groupValues[1]
            .toIntOrNull()
            ?.takeIf {
                it in 1..24 * 60
            }
    }

    private fun addSettingsCommand(
        query: String,
        results: MutableList<SearchResultItem>
    ) {
        val command =
            when (query) {

                "wifi",
                "wi-fi",
                "internet" ->
                    SearchCommand(
                        title = "Wi-Fi Settings",
                        subtitle = "System Network",
                        action =
                            Settings.ACTION_WIFI_SETTINGS
                    )

                "bluetooth",
                "bt" ->
                    SearchCommand(
                        title =
                            "Bluetooth Settings",
                        subtitle =
                            "System Wireless",
                        action =
                            Settings.ACTION_BLUETOOTH_SETTINGS
                    )

                "display",
                "brightness",
                "screen" ->
                    SearchCommand(
                        title =
                            "Display Settings",
                        subtitle =
                            "System Display & Brightness",
                        action =
                            Settings.ACTION_DISPLAY_SETTINGS
                    )

                "battery",
                "power" ->
                    SearchCommand(
                        title =
                            "Battery Settings",
                        subtitle =
                            "System Battery & Usage",
                        action =
                            Intent.ACTION_POWER_USAGE_SUMMARY
                    )

                "settings" ->
                    SearchCommand(
                        title =
                            "System Settings",
                        subtitle =
                            "Open Android Settings",
                        action =
                            Settings.ACTION_SETTINGS
                    )

                else -> null
            }

        command ?: return

        results.add(
            SearchResultItem.CommandResult(
                title = command.title,
                subtitle = command.subtitle,
                icon = Icons.Rounded.Settings,
                onExecute = { context ->
                    openSettings(
                        context,
                        command.action
                    )
                }
            )
        )
    }

    private fun addCalculatorResult(
        query: String,
        results: MutableList<SearchResultItem>
    ) {
        val mathMatch =
            Regex(
                """^(?:calc\s+)?([0-9.\+\-*/()\s]+)$"""
            ).find(query)
                ?: return

        val expression =
            mathMatch.groupValues[1]
                .trim()

        if (
            !query.contains("+") &&
            !query.contains("-") &&
            !query.contains("*") &&
            !query.contains("/") &&
            !query.startsWith("calc ")
        ) {
            return
        }

        val result =
            evaluateSimpleMath(expression)
                ?: return

        results.add(
            SearchResultItem.CommandResult(
                title = "= $result",
                subtitle =
                    "Calculation: $expression",
                icon =
                    Icons.Rounded.Calculate,
                onExecute = {}
            )
        )
    }

    private fun addAppResults(
        query: String,
        results: MutableList<SearchResultItem>
    ) {
        val matchingApps =
            appManager.allApps.value
                .asSequence()
                .filter { app ->
                    app.label
                        .lowercase(
                            Locale.getDefault()
                        )
                        .contains(query) ||
                        app.packageName
                            .lowercase(
                                Locale.getDefault()
                            )
                            .contains(query)
                }
                .sortedWith(
                    compareByDescending<AppItem> {
                        it.label
                            .lowercase(
                                Locale.getDefault()
                            ) == query
                    }.thenBy {
                        it.label
                            .lowercase(
                                Locale.getDefault()
                            )
                    }
                )
                .take(8)
                .toList()

        matchingApps.forEach { app ->
            results.add(
                SearchResultItem.AppResult(app)
            )
        }
    }

    private fun getSuggestions():
        List<SearchResultItem> {

        val suggestions =
            mutableListOf<SearchResultItem>()

        suggestions.add(
            SearchResultItem.CommandResult(
                title =
                    "Switch Personality",
                subtitle =
                    "Try fluid, calm, focus, premium, or expressive",
                icon =
                    Icons.Rounded.Palette,
                onExecute = {
                    commandScope.launch {
                        profileRepository
                            .cycleProfile()
                    }
                }
            )
        )

        suggestions.add(
            SearchResultItem.CommandResult(
                title =
                    "Start 25m Focus Timer",
                subtitle =
                    "Type \"timer 25m\"",
                icon =
                    Icons.Rounded.Timer,
                onExecute = { context ->
                    openTimer(
                        context,
                        25
                    )
                }
            )
        )

        appManager
            .frequentlyLaunched
            .value
            .take(4)
            .forEach { app ->
                suggestions.add(
                    SearchResultItem.AppResult(app)
                )
            }

        return suggestions
    }

    private fun openTimer(
        context: Context,
        minutes: Int
    ) {
        try {
            val intent =
                Intent(
                    AlarmClock.ACTION_SET_TIMER
                ).apply {
                    putExtra(
                        AlarmClock.EXTRA_LENGTH,
                        minutes * 60
                    )

                    putExtra(
                        AlarmClock.EXTRA_MESSAGE,
                        "Purple Launcher Focus"
                    )

                    putExtra(
                        AlarmClock.EXTRA_SKIP_UI,
                        false
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

        } catch (_: Exception) {

            try {
                context.startActivity(
                    Intent(
                        AlarmClock.ACTION_SHOW_ALARMS
                    ).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                )
            } catch (_: Exception) {
                // Unsupported clock provider.
            }
        }
    }

    private fun openSettings(
        context: Context,
        action: String
    ) {
        try {
            context.startActivity(
                Intent(action).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            )
        } catch (_: Exception) {

            try {
                context.startActivity(
                    Intent(
                        Settings.ACTION_SETTINGS
                    ).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                )
            } catch (_: Exception) {
                // Settings unavailable.
            }
        }
    }

    private fun openWebSearch(
        context: Context,
        query: String
    ) {
        try {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.google.com/search?q=${Uri.encode(query)}"
                    )
                ).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)
        } catch (_: Exception) {
            // Browser unavailable.
        }
    }

    private fun evaluateSimpleMath(
        expr: String
    ): String? {
        return try {
            val clean =
                expr.replace(" ", "")

            when {
                "+" in clean -> {
                    val parts =
                        clean.split("+")

                    if (parts.size != 2) {
                        return null
                    }

                    (
                        parts[0].toDouble() +
                            parts[1].toDouble()
                        ).formatNumber()
                }

                "*" in clean -> {
                    val parts =
                        clean.split("*")

                    if (parts.size != 2) {
                        return null
                    }

                    (
                        parts[0].toDouble() *
                            parts[1].toDouble()
                        ).formatNumber()
                }

                "/" in clean -> {
                    val parts =
                        clean.split("/")

                    if (parts.size != 2) {
                        return null
                    }

                    val denominator =
                        parts[1].toDouble()

                    if (denominator == 0.0) {
                        "Cannot divide by 0"
                    } else {
                        (
                            parts[0].toDouble() /
                                denominator
                            ).formatNumber()
                    }
                }

                "-" in clean -> {
                    val parts =
                        clean.split("-")

                    if (parts.size != 2) {
                        return null
                    }

                    (
                        parts[0].toDouble() -
                            parts[1].toDouble()
                        ).formatNumber()
                }

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun Double.formatNumber():
        String {
        return toString()
            .removeSuffix(".0")
    }

    private data class SearchCommand(
        val title: String,
        val subtitle: String,
        val action: String
    )
}
