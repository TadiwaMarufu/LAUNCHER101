package com.example.core.engine

import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.LauncherProfile

/**
 * Layout arrangement style for the home screen.
 *
 * These represent fundamentally different launcher compositions,
 * not merely different colors.
 */
enum class HomeLayoutStyle {
    FLUID_ORGANIC,
    PREMIUM_ARCHITECTURAL,
    CALM_MINIMALIST,
    FOCUS_DASHBOARD,
    EXPRESSIVE_AVANT_GARDE
}

/**
 * Clock presentation style.
 */
enum class ClockStyle {
    FLUID_ROUNDED,
    PREMIUM_SPLIT,
    CALM_INLINE,
    FOCUS_DIGITAL,
    EXPRESSIVE_MASSIVE
}

/**
 * Now Bar visual presentation.
 */
enum class NowBarStyle {
    FLUID_PILL,
    PREMIUM_BORDERED,
    CALM_ZEN,
    FOCUS_ACTIONABLE,
    EXPRESSIVE_CAPSULE
}

/**
 * Information density controls how aggressively the launcher
 * surfaces contextual information.
 */
enum class InformationDensity {
    MINIMAL,
    BALANCED,
    INFORMATIONAL,
    CONTEXTUAL,
    EXPERIMENTAL
}

/**
 * Motion choreography.
 *
 * This intentionally describes different motion personalities,
 * rather than simply changing animation duration.
 */
enum class MotionStyle {
    ORGANIC,
    REFINED,
    RESTRAINED,
    RESPONSIVE,
    PLAYFUL
}

/**
 * How aggressively the Now Bar surfaces contextual information.
 */
enum class NowBarBehavior {
    AMBIENT,
    CURATED,
    QUIET,
    ACTION_FIRST,
    EXPERIMENTAL
}

/**
 * Profile-level behavioral configuration.
 *
 * A profile is therefore a launcher personality, not a theme preset.
 */
data class ProfileConfig(
    val profile: LauncherProfile,

    // Home composition
    val homeLayout: HomeLayoutStyle,
    val clockStyle: ClockStyle,

    // Now Bar
    val nowBarStyle: NowBarStyle,
    val nowBarBehavior: NowBarBehavior,

    // Information and interaction
    val informationDensity: InformationDensity,
    val motionStyle: MotionStyle,

    // Visual tokens
    val cornerRadius: Dp,

    // Motion parameters
    val springDamping: Float,
    val springStiffness: Float,

    // Home defaults
    val showAppLabels: Boolean,
    val iconSize: Dp,
    val gridColumns: Int,
    val headerSpacing: Dp,
)

/**
 * Central personality engine.
 *
 * Keep profile-specific decisions here rather than scattering
 * `if (profile == ...)` throughout the UI and feature modules.
 */
object ProfileEngine {

    fun getConfig(profile: LauncherProfile): ProfileConfig {
        return when (profile) {

            LauncherProfile.FLUID -> ProfileConfig(
                profile = profile,

                homeLayout = HomeLayoutStyle.FLUID_ORGANIC,
                clockStyle = ClockStyle.FLUID_ROUNDED,

                nowBarStyle = NowBarStyle.FLUID_PILL,
                nowBarBehavior = NowBarBehavior.AMBIENT,

                informationDensity = InformationDensity.CONTEXTUAL,
                motionStyle = MotionStyle.ORGANIC,

                cornerRadius = 28.dp,

                springDamping = Spring.DampingRatioMediumBouncy,
                springStiffness = Spring.StiffnessLow,

                showAppLabels = true,
                iconSize = 56.dp,
                gridColumns = 4,
                headerSpacing = 24.dp,
            )

            LauncherProfile.PREMIUM -> ProfileConfig(
                profile = profile,

                homeLayout = HomeLayoutStyle.PREMIUM_ARCHITECTURAL,
                clockStyle = ClockStyle.PREMIUM_SPLIT,

                nowBarStyle = NowBarStyle.PREMIUM_BORDERED,
                nowBarBehavior = NowBarBehavior.CURATED,

                informationDensity = InformationDensity.BALANCED,
                motionStyle = MotionStyle.REFINED,

                cornerRadius = 20.dp,

                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessMedium,

                showAppLabels = true,
                iconSize = 52.dp,
                gridColumns = 4,
                headerSpacing = 32.dp,
            )

            LauncherProfile.CALM -> ProfileConfig(
                profile = profile,

                homeLayout = HomeLayoutStyle.CALM_MINIMALIST,
                clockStyle = ClockStyle.CALM_INLINE,

                nowBarStyle = NowBarStyle.CALM_ZEN,
                nowBarBehavior = NowBarBehavior.QUIET,

                informationDensity = InformationDensity.MINIMAL,
                motionStyle = MotionStyle.RESTRAINED,

                cornerRadius = 16.dp,

                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessMediumLow,

                showAppLabels = false,
                iconSize = 48.dp,
                gridColumns = 4,
                headerSpacing = 48.dp,
            )

            LauncherProfile.FOCUS -> ProfileConfig(
                profile = profile,

                homeLayout = HomeLayoutStyle.FOCUS_DASHBOARD,
                clockStyle = ClockStyle.FOCUS_DIGITAL,

                nowBarStyle = NowBarStyle.FOCUS_ACTIONABLE,
                nowBarBehavior = NowBarBehavior.ACTION_FIRST,

                informationDensity = InformationDensity.INFORMATIONAL,
                motionStyle = MotionStyle.RESPONSIVE,

                cornerRadius = 14.dp,

                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessHigh,

                showAppLabels = true,
                iconSize = 50.dp,
                gridColumns = 4,
                headerSpacing = 16.dp,
            )

            LauncherProfile.EXPRESSIVE -> ProfileConfig(
                profile = profile,

                homeLayout = HomeLayoutStyle.EXPRESSIVE_AVANT_GARDE,
                clockStyle = ClockStyle.EXPRESSIVE_MASSIVE,

                nowBarStyle = NowBarStyle.EXPRESSIVE_CAPSULE,
                nowBarBehavior = NowBarBehavior.EXPERIMENTAL,

                informationDensity = InformationDensity.EXPERIMENTAL,
                motionStyle = MotionStyle.PLAYFUL,

                cornerRadius = 36.dp,

                springDamping = Spring.DampingRatioHighBouncy,
                springStiffness = Spring.StiffnessLow,

                showAppLabels = true,
                iconSize = 60.dp,
                gridColumns = 4,
                headerSpacing = 28.dp,
            )
        }
    }
}
