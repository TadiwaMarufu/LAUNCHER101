package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import com.example.core.model.AppUsage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistent local usage history for launcher apps.
 *
 * Only launcher-owned usage metadata is stored:
 * component name, launch count, and last launch timestamp.
 */
class AppUsageRepository(
    private val dataStore: LauncherDataStore
) {

    val usage: Flow<Map<String, AppUsage>> =
        dataStore.appUsageJson.map(::decode)

    suspend fun recordLaunch(componentName: String) {
        val current = usage.first().toMutableMap()
        val previous = current[componentName]

        current[componentName] = AppUsage(
            componentName = componentName,
            launchCount = (previous?.launchCount ?: 0) + 1,
            lastLaunched = System.currentTimeMillis()
        )

        dataStore.setAppUsageJson(encode(current))
    }

    suspend fun getUsage(): Map<String, AppUsage> =
        usage.first()

    private fun encode(
        usage: Map<String, AppUsage>
    ): String {
        val array = JSONArray()

        usage.values.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("componentName", item.componentName)
                    put("launchCount", item.launchCount)
                    put("lastLaunched", item.lastLaunched)
                }
            )
        }

        return array.toString()
    }

    private fun decode(
        raw: String?
    ): Map<String, AppUsage> {
        if (raw.isNullOrBlank()) {
            return emptyMap()
        }

        return try {
            val array = JSONArray(raw)
            val result = mutableMapOf<String, AppUsage>()

            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index)
                    ?: continue

                val componentName =
                    item.optString("componentName")
                        .takeIf { it.isNotBlank() }
                        ?: continue

                result[componentName] = AppUsage(
                    componentName = componentName,
                    launchCount = item.optInt(
                        "launchCount",
                        0
                    ).coerceAtLeast(0),
                    lastLaunched = item.optLong(
                        "lastLaunched",
                        0L
                    ).coerceAtLeast(0L)
                )
            }

            result
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
