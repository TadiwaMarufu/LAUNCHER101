package com.example.core.engine

import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.LauncherProfile

/**
 * Describes the spatial philosophy of a personality.
 *
 * This does NOT decide what the user puts on the home screen.
 */
enum class HomeLayoutStyle {
    FREEFORM,
    STRUCTURED,
    MINIMAL,
    SEARCH_FIRST,
    EXPERIMENTAL
}

/**
 * How strongly the launcher emphasizes information.
 */
enum class InformationDensity {
    MINIMAL,
    BALANCED,
    HIGH
}

/**
 * Motion choreography.
 *
 * These are behavioral differences, not simply animation speeds.
 */
enum class MotionStyle {
    ORGANIC,
    REFINED,
    RESTRAINED,
    RESPONSIVE,
    PLAYFUL
}

/**
 * Primary navigation philosophy.
 */
enum class NavigationStyle {
    SPATIAL,
    PRECISE,
    QUIET,
    SEARCH_FIRST,
    EXPLORATORY
}

/**
 * How the Now Bar behaves when enabled by the user.
 */
enum class NowBarBehavior {
    AMBIENT,
    CURATED,
    QUIET,
    ACTION_FIRST,
    EXPERIMENTAL
}

/**
 * Profile defaults.
 *
 * Important:
 * These are behavioral defaults only.
 * They must never force home content into existence.
 */
data class ProfileConfig(
    val profile: LauncherProfile,

    val homeLayout: HomeLayoutStyle,
    val informationDensity: InformationDensity,
    val motionStyle: MotionStyle,
    val navigationStyle: NavigationStyle,
    val nowBarBehavior: NowBarBehavior,

    val defaultGridColumns: Int,
    val defaultGridRows: Int,

    val showLabelsByDefault: Boolean,

    val springDamping: Float,
    val springStiffness: Float,

    val defaultQualityTier: com.example.core.model.QualityTier
)

object ProfileEngine {

    fun getConfig(profile: LauncherProfile): ProfileConfig {
        return when (profile) {

            LauncherProfile.FLUID -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.FREEFORM,
                informationDensity = InformationDensity.BALANCED,
                motionStyle = MotionStyle.ORGANIC,
                navigationStyle = NavigationStyle.SPATIAL,
                nowBarBehavior = NowBarBehavior.AMBIENT,
                defaultGridColumns = 5,
                defaultGridRows = 10,
                showLabelsByDefault = true,
                springDamping = Spring.DampingRatioMediumBouncy,
                springStiffness = Spring.StiffnessLow,
                defaultQualityTier = com.example.core.model.QualityTier.MEDIUM
            )

            LauncherProfile.PREMIUM -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.STRUCTURED,
                informationDensity = InformationDensity.BALANCED,
                motionStyle = MotionStyle.REFINED,
                navigationStyle = NavigationStyle.PRECISE,
                nowBarBehavior = NowBarBehavior.CURATED,
                defaultGridColumns = 5,
                defaultGridRows = 10,
                showLabelsByDefault = true,
                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessMedium,
                defaultQualityTier = com.example.core.model.QualityTier.HIGH
            )

            LauncherProfile.CALM -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.MINIMAL,
                informationDensity = InformationDensity.MINIMAL,
                motionStyle = MotionStyle.RESTRAINED,
                navigationStyle = NavigationStyle.QUIET,
                nowBarBehavior = NowBarBehavior.QUIET,
                defaultGridColumns = 5,
                defaultGridRows = 10,
                showLabelsByDefault = false,
                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessMediumLow,
                defaultQualityTier = com.example.core.model.QualityTier.LOW
            )

            LauncherProfile.FOCUS -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.SEARCH_FIRST,
                informationDensity = InformationDensity.HIGH,
                motionStyle = MotionStyle.RESPONSIVE,
                navigationStyle = NavigationStyle.SEARCH_FIRST,
                nowBarBehavior = NowBarBehavior.ACTION_FIRST,
                defaultGridColumns = 5,
                defaultGridRows = 10,
                showLabelsByDefault = true,
                springDamping = Spring.DampingRatioNoBouncy,
                springStiffness = Spring.StiffnessHigh,
                defaultQualityTier = com.example.core.model.QualityTier.MEDIUM
            )

            LauncherProfile.EXPRESSIVE -> ProfileConfig(
                profile = profile,
                homeLayout = HomeLayoutStyle.EXPERIMENTAL,
                informationDensity = InformationDensity.BALANCED,
                motionStyle = MotionStyle.PLAYFUL,
                navigationStyle = NavigationStyle.EXPLORATORY,
                nowBarBehavior = NowBarBehavior.EXPERIMENTAL,
                defaultGridColumns = 5,
                defaultGridRows = 10,
                showLabelsByDefault = true,
                springDamping = Spring.DampingRatioHighBouncy,
                springStiffness = Spring.StiffnessLow,
                defaultQualityTier = com.example.core.model.QualityTier.HIGH
            )
        }
    }
}
