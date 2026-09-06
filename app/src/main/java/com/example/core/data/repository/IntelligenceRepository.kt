package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import kotlinx.coroutines.flow.Flow

/**
 * Stores explicit user controls for adaptive launcher behavior.
 *
 * No data collection is performed by this repository.
 */
class IntelligenceRepository(
    private val dataStore: LauncherDataStore
) {

    val trackAppUsage: Flow<Boolean> =
        dataStore.trackAppUsage

    val contextualSuggestions: Flow<Boolean> =
        dataStore.contextualSuggestions

    suspend fun setTrackAppUsage(enabled: Boolean) =
        dataStore.setTrackAppUsage(enabled)

    suspend fun setContextualSuggestions(enabled: Boolean) =
        dataStore.setContextualSuggestions(enabled)
}
