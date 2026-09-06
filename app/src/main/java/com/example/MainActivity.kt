package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.LauncherDependencies
import com.example.ui.home.HomeScreen
import com.example.ui.launcher.LauncherViewModel
import com.example.ui.launcher.LauncherViewModelFactory
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val dependencies by lazy {
        LauncherDependencies.get(applicationContext)
    }

    private val launcherViewModel: LauncherViewModel by viewModels {
        LauncherViewModelFactory(dependencies)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by launcherViewModel.uiState
                .collectAsStateWithLifecycle()

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!state.onboardingCompleted) {
                        OnboardingScreen(
                            onComplete = launcherViewModel::completeOnboarding
                        )
                    } else {
                        HomeScreen()
                    }
                }
            }
        }
    }
}
