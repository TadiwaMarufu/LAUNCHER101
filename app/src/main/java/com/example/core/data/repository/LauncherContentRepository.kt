package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import com.example.core.model.LauncherTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistence for launcher-owned content:
 * pinned apps, tasks and other user-created launcher state.
 */
class LauncherContentRepository(
    private val dataStore: LauncherDataStore
) {

    val pinnedApps: Flow<Set<String>> =
        dataStore.pinnedApps.distinctUntilChanged()

    val tasks: Flow<List<LauncherTask>> =
        dataStore.tasksJson
            .map(::decodeTasks)
            .distinctUntilChanged()

    suspend fun toggleAppPin(componentName: String) {
        val current = dataStore.pinnedApps.first()
        val updated = current.toMutableSet()

        if (!updated.add(componentName)) {
            updated.remove(componentName)
        }

        dataStore.setPinnedApps(updated)
    }

    suspend fun addTask(text: String) {
        val cleanText = text.trim()

        if (cleanText.isEmpty()) {
            return
        }

        val tasks = dataStore.tasksJson
            .first()
            .let(::decodeTasks)
            .toMutableList()

        tasks.add(
            0,
            LauncherTask(
                id = System.currentTimeMillis().toString(),
                text = cleanText,
                isCompleted = false
            )
        )

        saveTasks(tasks)
    }

    suspend fun toggleTask(id: String) {
        val tasks = dataStore.tasksJson
            .first()
            .let(::decodeTasks)
            .map { task ->
                if (task.id == id) {
                    task.copy(isCompleted = !task.isCompleted)
                } else {
                    task
                }
            }

        saveTasks(tasks)
    }

    suspend fun removeTask(id: String) {
        val tasks = dataStore.tasksJson
            .first()
            .let(::decodeTasks)
            .filterNot { it.id == id }

        saveTasks(tasks)
    }

    private suspend fun saveTasks(
        tasks: List<LauncherTask>
    ) {
        val array = JSONArray()

        tasks.forEach { task ->
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("text", task.text)
                    put("isCompleted", task.isCompleted)
                    put("timestamp", task.timestamp)
                }
            )
        }

        dataStore.setTasksJson(array.toString())
    }

    private fun decodeTasks(
        raw: String?
    ): List<LauncherTask> {

        if (raw.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val array = JSONArray(raw)

            buildList {
                for (index in 0 until array.length()) {

                    val item =
                        array.getJSONObject(index)

                    add(
                        LauncherTask(
                            id = item.getString("id"),
                            text = item.getString("text"),
                            isCompleted =
                                item.optBoolean(
                                    "isCompleted",
                                    false
                                ),
                            timestamp =
                                item.optLong(
                                    "timestamp",
                                    System.currentTimeMillis()
                                )
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
