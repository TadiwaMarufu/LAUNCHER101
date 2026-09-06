package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import kotlinx.coroutines.flow.Flow

/**
 * Launcher-wide state that doesn't belong to a feature-specific repository.
 */
class LauncherStateRepository(
    private val dataStore: LauncherDataStore
) {

    val onboardingCompleted: Flow<Boolean> =
        dataStore.onboardingCompleted

    val homePage: Flow<Int> =
        dataStore.homePage

    suspend fun setOnboardingCompleted(completed: Boolean) =
        dataStore.setOnboardingCompleted(completed)

    suspend fun setHomePage(page: Int) =
        dataStore.setHomePage(page)
}
