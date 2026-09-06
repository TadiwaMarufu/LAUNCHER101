package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.core.model.WallpaperPreset

@Composable
fun WallpaperBackground(
    preset: WallpaperPreset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bgModifier = when (preset) {
        WallpaperPreset.SYSTEM -> {
            // Let the system wallpaper shine through via android:windowShowWallpaper
            Modifier.background(Color.Transparent)
        }
        else -> {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = preset.gradientColors
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(bgModifier)
    ) {
        content()
    }
}
