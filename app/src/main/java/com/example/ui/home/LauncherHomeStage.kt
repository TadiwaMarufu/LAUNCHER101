package com.example.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.engine.HomeLayoutStyle
import com.example.core.engine.ProfileConfig

@Composable
fun LauncherHomeStage(
    config: ProfileConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val horizontalInset = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 6.dp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 10.dp
        HomeLayoutStyle.CALM_MINIMALIST -> 16.dp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 6.dp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 3.dp
    }

    val topInset = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 8.dp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 14.dp
        HomeLayoutStyle.CALM_MINIMALIST -> 22.dp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 8.dp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 4.dp
    }

    val bottomInset = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 8.dp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 12.dp
        HomeLayoutStyle.CALM_MINIMALIST -> 18.dp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 8.dp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 4.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = horizontalInset,
                end = horizontalInset,
                top = topInset,
                bottom = bottomInset
            )
    ) {
        content()
    }
}
