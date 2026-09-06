package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.core.apps.AppManager
import com.example.core.data.LauncherPreferences
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferences: LauncherPreferences
    private lateinit var appManager: AppManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = LauncherPreferences.getInstance(this)
        appManager = AppManager.getInstance(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    LauncherApp(preferences = preferences)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure installed apps list is up-to-date whenever returning to home
        appManager.loadInstalledApps()
    }
}

@Composable
fun LauncherApp(preferences: LauncherPreferences) {
    val onboardingCompleted by preferences.onboardingCompleted.collectAsState()

    if (!onboardingCompleted) {
        OnboardingScreen(
            onComplete = { selectedProfile ->
                preferences.setActiveProfile(selectedProfile)
                preferences.setOnboardingCompleted(true)
            }
        )
    } else {
        HomeScreen()
    }
}
