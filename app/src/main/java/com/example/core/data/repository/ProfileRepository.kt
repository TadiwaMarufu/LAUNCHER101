package com.example.core.data.repository

import com.example.core.data.store.LauncherDataStore
import com.example.core.model.LauncherProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

/**
 * Owns profile selection and persistence.
 *
 * This repository deliberately contains no UI logic and no profile styling.
 * Profile behavior is resolved by ProfileEngine.
 */
class ProfileRepository(
    private val dataStore: LauncherDataStore
) {

    val activeProfile: Flow<LauncherProfile> =
        dataStore.activeProfile.distinctUntilChanged()

    suspend fun setProfile(profile: LauncherProfile) {
        dataStore.setActiveProfile(profile)
    }

    suspend fun cycleProfile(): LauncherProfile {
        val current = activeProfile.first()
        val profiles = LauncherProfile.entries

        val currentIndex = profiles.indexOf(current)
        val nextIndex =
            if (currentIndex < 0) {
                0
            } else {
                (currentIndex + 1) % profiles.size
            }

        val next = profiles[nextIndex]
        setProfile(next)

        return next
    }
}
