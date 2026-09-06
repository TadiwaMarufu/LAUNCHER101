package com.example.core.engine

import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LauncherProfile

/**
 * Layout arrangement style for home screen.
 */
enum class HomeLayoutStyle {
    FLUID_ORGANIC,
    PREMIUM_ARCHITECTURAL,
    CALM_MINIMALIST,
    FOCUS_DASHBOARD,
    EXPRESSIVE_AVANT_GARDE
}

/**
 * Clock presentation style for home screen.
 */
enum class ClockStyle {
    FLUID_ROUNDED,
    PREMIUM_SPLIT,
    CALM_INLINE,
    FOCUS_DIGITAL,
    EXPRESSIVE_MASSIVE
}

/**
 * Now Bar visual styling.
 */
enum class NowBarStyle {
    FLUID_PILL,
    PREMIUM_BORDERED,
    CALM_ZEN,
    FOCUS_ACTIONABLE,
    EXPRESSIVE_CAPSULE
}

/**
 * Structured configuration that defines how a profile expresses itself across the launcher.
 */
data class ProfileConfig(
    val profile: LauncherProfile,
    val homeLayout: HomeLayoutStyle,
    val clockStyle: ClockStyle,
    val nowBarStyle: NowBarStyle,
    val cornerRadius: Dp,
    val cardBackgroundAlpha: Float,
    val cardBorderWidth: Dp,
    val cardBorderColor: Color,
    val springDamping: Float,
    val springStiffness: Float,
    val showAppLabels: Boolean,
    val iconSize: Dp,
    val gridColumns: Int,
    val headerSpacing: Dp,
    val fontLetterSpacing: Float,
    val wallpaperBlurRadius: Dp
)

/**
 * Engine that provides unified profile configurations and tokens.
 */
object ProfileEngine {

    fun getConfig(profile: LauncherProfile): ProfileConfig {
        return when (profile) {
            LauncherProfile.FLUID -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.FLUID_ORGANIC,
                clockStyle = ClockStyle.FLUID_ROUNDED,
                nowBarStyle = NowBarStyle.FLUID_PILL,
                cornerRadius = 28.dp,
                cardBackgroundAlpha = 0.65f,
                cardBorderWidth = 1.dp,
                cardBorderColor = Color(0x33A855F7),
                springDamping = Spring.DampingRatioMediumBouncy,
                springStiffness = Spring.StiffnessLow,
                showAppLabels = true,
                iconSize = 56.dp,
                gridColumns = 4,
                headerSpacing = 24.dp,
                fontLetterSpacing = 0.02f,
                wallpaperBlurRadius = 16.dp
            )

            LauncherProfile.PREMIUM -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.PREMIUM_ARCHITECTURAL,
                clockStyle = ClockStyle.PREMIUM_SPLIT,
                nowBarStyle = NowBarStyle.PREMIUM_BORDERED,
                cornerRadius = 20.dp,
                cardBackgroundAlpha = 0.85f,
                cardBorderWidth = 1.5.dp,
                cardBorderColor = Color(0x40E2E8F0),
                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessMedium,
                showAppLabels = true,
                iconSize = 52.dp,
                gridColumns = 4,
                headerSpacing = 32.dp,
                fontLetterSpacing = 0.08f,
                wallpaperBlurRadius = 8.dp
            )

            LauncherProfile.CALM -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.CALM_MINIMALIST,
                clockStyle = ClockStyle.CALM_INLINE,
                nowBarStyle = NowBarStyle.CALM_ZEN,
                cornerRadius = 16.dp,
                cardBackgroundAlpha = 0.35f,
                cardBorderWidth = 0.5.dp,
                cardBorderColor = Color(0x20FFFFFF),
                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessMediumLow,
                showAppLabels = false,
                iconSize = 48.dp,
                gridColumns = 4,
                headerSpacing = 48.dp,
                fontLetterSpacing = 0.04f,
                wallpaperBlurRadius = 0.dp
            )

            LauncherProfile.FOCUS -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.FOCUS_DASHBOARD,
                clockStyle = ClockStyle.FOCUS_DIGITAL,
                nowBarStyle = NowBarStyle.FOCUS_ACTIONABLE,
                cornerRadius = 14.dp,
                cardBackgroundAlpha = 0.90f,
                cardBorderWidth = 1.dp,
                cardBorderColor = Color(0x309333EA),
                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessHigh,
                showAppLabels = true,
                iconSize = 50.dp,
                gridColumns = 4,
                headerSpacing = 16.dp,
                fontLetterSpacing = 0.01f,
                wallpaperBlurRadius = 4.dp
            )

            LauncherProfile.EXPRESSIVE -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE,
                clockStyle = ClockStyle.EXPRESSIVE_MASSIVE,
                nowBarStyle = NowBarStyle.EXPRESSIVE_CAPSULE,
                cornerRadius = 36.dp,
                cardBackgroundAlpha = 0.75f,
                cardBorderWidth = 2.dp,
                cardBorderColor = Color(0x50D946EF),
                springDamping = Spring.DampingRatioHighBouncy,
                springStiffness = Spring.StiffnessLow,
                showAppLabels = true,
                iconSize = 60.dp,
                gridColumns = 4,
                headerSpacing = 28.dp,
                fontLetterSpacing = -0.02f,
                wallpaperBlurRadius = 24.dp
            )
        }
    }
}
