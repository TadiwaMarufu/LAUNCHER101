package com.example.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.HomeLayoutStyle
import com.example.core.engine.ProfileConfig
import com.example.core.model.LauncherProfile
import com.example.ui.theme.LocalLauncherAppearance

@Composable
fun LauncherHomeStage(
    config: ProfileConfig,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val appearance = LocalLauncherAppearance.current

    val horizontalInset = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 12.dp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 18.dp
        HomeLayoutStyle.CALM_MINIMALIST -> 22.dp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 14.dp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 8.dp
    }

    val topInset = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 18.dp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 26.dp
        HomeLayoutStyle.CALM_MINIMALIST -> 34.dp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 20.dp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 12.dp
    }

    val bottomInset = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 18.dp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 24.dp
        HomeLayoutStyle.CALM_MINIMALIST -> 30.dp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 16.dp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 12.dp
    }

    val stageAlpha = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 1f
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> .98f
        HomeLayoutStyle.CALM_MINIMALIST -> .92f
        HomeLayoutStyle.FOCUS_DASHBOARD -> 1f
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 1f
    }

    val glowAlpha by animateFloatAsState(
        targetValue = when (config.homeLayout) {
            HomeLayoutStyle.FLUID_ORGANIC -> .10f
            HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> .055f
            HomeLayoutStyle.CALM_MINIMALIST -> .025f
            HomeLayoutStyle.FOCUS_DASHBOARD -> .075f
            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> .13f
        },
        animationSpec = tween(500),
        label = "stage-glow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .alpha(stageAlpha)
            .padding(
                start = horizontalInset,
                end = horizontalInset,
                top = topInset,
                bottom = bottomInset
            )
    ) {
        HomeIdentityHeader(
            profile = config.profile,
            config = config,
            onSearch = onSearch
        )

        Spacer(
            modifier = Modifier.height(
                when (config.homeLayout) {
                    HomeLayoutStyle.FLUID_ORGANIC -> 10.dp
                    HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 14.dp
                    HomeLayoutStyle.CALM_MINIMALIST -> 18.dp
                    HomeLayoutStyle.FOCUS_DASHBOARD -> 10.dp
                    HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 8.dp
                }
            )
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            when (config.homeLayout) {
                                HomeLayoutStyle.FLUID_ORGANIC -> 28.dp
                                HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 24.dp
                                HomeLayoutStyle.CALM_MINIMALIST -> 18.dp
                                HomeLayoutStyle.FOCUS_DASHBOARD -> 20.dp
                                HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 34.dp
                            }
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                appearance.primary.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            content()
        }
    }
}

@Composable
fun HomeIdentityHeader(
    profile: LauncherProfile,
    config: ProfileConfig,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {}
) {
    val appearance = LocalLauncherAppearance.current

    val title = when (profile) {
        LauncherProfile.FLUID -> "In motion"
        LauncherProfile.PREMIUM -> "Your space"
        LauncherProfile.CALM -> "Breathe"
        LauncherProfile.FOCUS -> "Focus"
        LauncherProfile.EXPRESSIVE -> "Your frequency"
    }

    val subtitle = when (profile) {
        LauncherProfile.FLUID -> "Everything where you left it."
        LauncherProfile.PREMIUM -> "Precise. Personal. Yours."
        LauncherProfile.CALM -> "Only what matters."
        LauncherProfile.FOCUS -> "Ready when you are."
        LauncherProfile.EXPRESSIVE -> "Make the screen yours."
    }

    val titleSize = when (config.homeLayout) {
        HomeLayoutStyle.FLUID_ORGANIC -> 23.sp
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 21.sp
        HomeLayoutStyle.CALM_MINIMALIST -> 19.sp
        HomeLayoutStyle.FOCUS_DASHBOARD -> 22.sp
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 27.sp
    }

    val titleWeight = when (config.homeLayout) {
        HomeLayoutStyle.CALM_MINIMALIST -> FontWeight.Normal
        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> FontWeight.Medium
        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> FontWeight.Bold
        else -> FontWeight.SemiBold
    }

    val searchScale by animateFloatAsState(
        targetValue = when (config.homeLayout) {
            HomeLayoutStyle.CALM_MINIMALIST -> .86f
            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 1.08f
            else -> 1f
        },
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "search-scale"
    )

    val searchContainer by animateColorAsState(
        targetValue = appearance.surface.copy(
            alpha = when (config.homeLayout) {
                HomeLayoutStyle.CALM_MINIMALIST -> .35f
                HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> .72f
                HomeLayoutStyle.FOCUS_DASHBOARD -> .85f
                HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> .65f
                else -> .72f
            }
        ),
        animationSpec = tween(350),
        label = "search-container"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    (fadeIn(tween(250)) + scaleIn(
                        initialScale = .96f,
                        animationSpec = tween(250)
                    )).togetherWith(
                        fadeOut(tween(150)) + scaleOut(
                            targetScale = 1.02f,
                            animationSpec = tween(150)
                        )
                    )
                },
                label = "identity-title"
            ) { animatedTitle ->
                Text(
                    text = animatedTitle,
                    color = appearance.onSurface,
                    fontSize = titleSize,
                    fontWeight = titleWeight,
                    letterSpacing = when (config.homeLayout) {
                        HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> (-.7).sp
                        HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> (-.25).sp
                        else -> 0.sp
                    }
                )
            }

            Text(
                text = subtitle,
                color = appearance.onSurfaceVariant,
                fontSize = when (config.homeLayout) {
                    HomeLayoutStyle.CALM_MINIMALIST -> 11.sp
                    HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 12.sp
                    else -> 12.sp
                },
                fontWeight = FontWeight.Normal
            )
        }

        if (config.homeLayout != HomeLayoutStyle.CALM_MINIMALIST) {
            Box(
                modifier = Modifier
                    .size(
                        when (config.homeLayout) {
                            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 46.dp
                            HomeLayoutStyle.PREMIUM_ARCHITECTURAL -> 42.dp
                            else -> 40.dp
                        }
                    )
                    .scale(searchScale)
                    .clip(
                        when (config.homeLayout) {
                            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE ->
                                RoundedCornerShape(16.dp)

                            HomeLayoutStyle.PREMIUM_ARCHITECTURAL ->
                                RoundedCornerShape(13.dp)

                            else ->
                                CircleShape
                        }
                    )
                    .background(searchContainer)
                    .clickable(onClick = onSearch),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = appearance.onSurface,
                    modifier = Modifier.size(
                        when (config.homeLayout) {
                            HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE -> 22.dp
                            else -> 20.dp
                        }
                    )
                )
            }
        }
    }
}
