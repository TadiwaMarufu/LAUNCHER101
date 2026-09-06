package com.example.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.LauncherDependencies
import com.example.core.model.LauncherProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val profile: LauncherProfile = LauncherProfile.FLUID,
    val onboardingCompleted: Boolean = false
)

class LauncherViewModel(
    private val dependencies: LauncherDependencies
) : ViewModel() {

    private val profile =
        dependencies.profileRepository.activeProfile

    private val onboarding =
        dependencies.launcherStateRepository.onboardingCompleted

    val uiState: StateFlow<LauncherUiState> =
        kotlinx.coroutines.flow.combine(
            profile,
            onboarding
        ) { activeProfile, onboardingCompleted ->
            LauncherUiState(
                profile = activeProfile,
                onboardingCompleted = onboardingCompleted
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LauncherUiState()
        )

    fun completeOnboarding(profile: LauncherProfile) {
        viewModelScope.launch {
            dependencies.profileRepository.setProfile(profile)
            dependencies.launcherStateRepository
                .setOnboardingCompleted(true)
        }
    }

    fun setProfile(profile: LauncherProfile) {
        viewModelScope.launch {
            dependencies.profileRepository.setProfile(profile)
        }
    }

    fun cycleProfile() {
        viewModelScope.launch {
            dependencies.profileRepository.cycleProfile()
        }
    }
}

class LauncherViewModelFactory(
    private val dependencies: LauncherDependencies
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
            return LauncherViewModel(dependencies) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
