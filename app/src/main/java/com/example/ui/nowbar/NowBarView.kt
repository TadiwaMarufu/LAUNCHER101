package com.example.ui.nowbar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.NowBarStyle
import com.example.core.engine.ProfileConfig
import com.example.core.model.LauncherProfile
import com.example.core.model.MediaPlaybackState
import com.example.core.model.NowBarItem
import com.example.core.model.NowBarType
import com.example.ui.theme.LocalLauncherAppearance

@Composable
fun NowBarView(
    items: List<NowBarItem>,
    mediaState: MediaPlaybackState,
    profile: LauncherProfile,
    config: ProfileConfig,
    onProfileChipClick: () -> Unit,
    onMediaPlayToggle: () -> Unit,
    onSearchClick: () -> Unit,
    onDrawerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current

    val barShape = when (config.nowBarStyle) {
        NowBarStyle.FLUID_PILL -> RoundedCornerShape(32.dp)
        NowBarStyle.PREMIUM_BORDERED -> RoundedCornerShape(20.dp)
        NowBarStyle.CALM_ZEN -> RoundedCornerShape(16.dp)
        NowBarStyle.FOCUS_ACTIONABLE -> RoundedCornerShape(14.dp)
        NowBarStyle.EXPRESSIVE_CAPSULE -> RoundedCornerShape(40.dp)
    }

    val backgroundBrush = when (config.nowBarStyle) {
        NowBarStyle.FLUID_PILL -> Brush.horizontalGradient(
            colors = listOf(
                appearance.surface.copy(alpha = appearance.surfaceAlpha),
                appearance.primary.copy(alpha = 0.20f),
                appearance.surface.copy(alpha = appearance.surfaceAlpha)
            )
        )

        NowBarStyle.EXPRESSIVE_CAPSULE -> Brush.linearGradient(
            colors = listOf(
                appearance.surface.copy(alpha = 0.90f),
                appearance.primary.copy(alpha = 0.35f)
            )
        )

        NowBarStyle.FOCUS_ACTIONABLE -> Brush.horizontalGradient(
            colors = listOf(
                appearance.surface.copy(alpha = appearance.surfaceAlpha),
                appearance.primary.copy(alpha = 0.08f),
                appearance.surface.copy(alpha = appearance.surfaceAlpha)
            )
        )

        else -> Brush.linearGradient(
            colors = listOf(
                appearance.surface.copy(alpha = appearance.surfaceAlpha),
                appearance.surface.copy(alpha = appearance.surfaceAlpha)
            )
        )
    }

    val horizontalPadding = when (config.nowBarStyle) {
        NowBarStyle.FLUID_PILL -> 14.dp
        NowBarStyle.PREMIUM_BORDERED -> 16.dp
        NowBarStyle.CALM_ZEN -> 18.dp
        NowBarStyle.FOCUS_ACTIONABLE -> 12.dp
        NowBarStyle.EXPRESSIVE_CAPSULE -> 16.dp
    }

    val verticalPadding = when (config.nowBarStyle) {
        NowBarStyle.FLUID_PILL -> 10.dp
        NowBarStyle.PREMIUM_BORDERED -> 9.dp
        NowBarStyle.CALM_ZEN -> 8.dp
        NowBarStyle.FOCUS_ACTIONABLE -> 8.dp
        NowBarStyle.EXPRESSIVE_CAPSULE -> 12.dp
    }

    val actionSize = when (config.nowBarStyle) {
        NowBarStyle.FLUID_PILL -> 38.dp
        NowBarStyle.PREMIUM_BORDERED -> 36.dp
        NowBarStyle.CALM_ZEN -> 34.dp
        NowBarStyle.FOCUS_ACTIONABLE -> 40.dp
        NowBarStyle.EXPRESSIVE_CAPSULE -> 42.dp
    }

    val actionSpacing = when (config.nowBarStyle) {
        NowBarStyle.FLUID_PILL -> 4.dp
        NowBarStyle.PREMIUM_BORDERED -> 6.dp
        NowBarStyle.CALM_ZEN -> 8.dp
        NowBarStyle.FOCUS_ACTIONABLE -> 4.dp
        NowBarStyle.EXPRESSIVE_CAPSULE -> 6.dp
    }

    val centerTitle = when (profile) {
        LauncherProfile.FLUID -> "In your flow"
        LauncherProfile.PREMIUM -> "Your space"
        LauncherProfile.CALM -> "Quiet"
        LauncherProfile.FOCUS -> "Command"
        LauncherProfile.EXPRESSIVE -> "Frequency"
    }

    val centerSubtitle = when (profile) {
        LauncherProfile.FLUID -> "Everything in motion"
        LauncherProfile.PREMIUM -> "Launcher ready"
        LauncherProfile.CALM -> "Nothing extra"
        LauncherProfile.FOCUS -> "Search • Launch • Go"
        LauncherProfile.EXPRESSIVE -> "Make it yours"
    }

    val profileChipShape = when (config.nowBarStyle) {
        NowBarStyle.PREMIUM_BORDERED ->
            RoundedCornerShape(12.dp)

        NowBarStyle.FOCUS_ACTIONABLE ->
            RoundedCornerShape(10.dp)

        NowBarStyle.EXPRESSIVE_CAPSULE ->
            RoundedCornerShape(14.dp)

        else ->
            CircleShape
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .background(backgroundBrush)
            .border(
                appearance.borderWidth.dp,
                appearance.divider,
                barShape
            )
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            )
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            /*
             * PROFILE
             *
             * This remains the entry point for switching personalities.
             */
            Row(
                modifier = Modifier
                    .clip(profileChipShape)
                    .background(
                        appearance.primary.copy(
                            alpha = when (config.nowBarStyle) {
                                NowBarStyle.CALM_ZEN -> 0.08f
                                NowBarStyle.PREMIUM_BORDERED -> 0.08f
                                NowBarStyle.FOCUS_ACTIONABLE -> 0.14f
                                NowBarStyle.EXPRESSIVE_CAPSULE -> 0.20f
                                NowBarStyle.FLUID_PILL -> 0.16f
                            }
                        )
                    )
                    .clickable { onProfileChipClick() }
                    .padding(
                        horizontal = when (config.nowBarStyle) {
                            NowBarStyle.CALM_ZEN -> 10.dp
                            NowBarStyle.PREMIUM_BORDERED -> 11.dp
                            else -> 12.dp
                        },
                        vertical = when (config.nowBarStyle) {
                            NowBarStyle.CALM_ZEN -> 6.dp
                            else -> 8.dp
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            when (config.nowBarStyle) {
                                NowBarStyle.EXPRESSIVE_CAPSULE -> 9.dp
                                else -> 8.dp
                            }
                        )
                        .clip(CircleShape)
                        .background(appearance.primary)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = profile.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onSurface
                )
            }

            /*
             * PERSONALITY CENTER
             *
             * This is no longer a generic "Purple Frequency" placeholder.
             * The center communicates what the current launcher personality
             * is trying to make the home feel like.
             */
            if (config.nowBarStyle != NowBarStyle.CALM_ZEN) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(
                            when (config.nowBarStyle) {
                                NowBarStyle.EXPRESSIVE_CAPSULE ->
                                    RoundedCornerShape(18.dp)

                                NowBarStyle.PREMIUM_BORDERED ->
                                    RoundedCornerShape(12.dp)

                                else ->
                                    RoundedCornerShape(14.dp)
                            }
                        )
                        .clickable { onMediaPlayToggle() }
                        .padding(
                            horizontal = when {
                                mediaState.isPlaying -> 10.dp
                                else -> 8.dp
                            },
                            vertical = 5.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                when (config.nowBarStyle) {
                                    NowBarStyle.EXPRESSIVE_CAPSULE -> 8.dp
                                    else -> 7.dp
                                }
                            )
                            .clip(CircleShape)
                            .background(appearance.primary)
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (mediaState.isPlaying) {
                                mediaState.title
                            } else {
                                centerTitle
                            },
                            fontSize = when (config.nowBarStyle) {
                                NowBarStyle.FOCUS_ACTIONABLE -> 12.sp
                                NowBarStyle.EXPRESSIVE_CAPSULE -> 13.sp
                                else -> 12.sp
                            },
                            fontWeight = when (config.nowBarStyle) {
                                NowBarStyle.EXPRESSIVE_CAPSULE ->
                                    FontWeight.Bold

                                else ->
                                    FontWeight.Medium
                            },
                            color = appearance.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = if (mediaState.isPlaying) {
                                "Now playing"
                            } else {
                                centerSubtitle
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            color = appearance.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (mediaState.isPlaying) {
                        Spacer(modifier = Modifier.width(7.dp))

                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Playing",
                            tint = appearance.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (config.nowBarStyle == NowBarStyle.FOCUS_ACTIONABLE) {
                        Spacer(modifier = Modifier.width(7.dp))

                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = appearance.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                /*
                 * CALM deliberately refuses to compete for attention.
                 */
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = centerTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = appearance.onSurfaceVariant
                    )
                }
            }

            /*
             * ACTIONS
             *
             * Existing launcher actions remain intact.
             */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(actionSpacing)
            ) {
                items.filter {
                    it.type in listOf(
                        NowBarType.ACTION_SEARCH,
                        NowBarType.ACTION_DRAWER,
                        NowBarType.ACTION_SETTINGS
                    )
                }.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(actionSize)
                            .clip(CircleShape)
                            .background(
                                when (config.nowBarStyle) {
                                    NowBarStyle.CALM_ZEN ->
                                        appearance.surface.copy(alpha = 0.22f)

                                    NowBarStyle.PREMIUM_BORDERED ->
                                        appearance.surface.copy(alpha = 0.34f)

                                    NowBarStyle.FOCUS_ACTIONABLE ->
                                        appearance.primary.copy(alpha = 0.08f)

                                    NowBarStyle.EXPRESSIVE_CAPSULE ->
                                        appearance.primary.copy(alpha = 0.12f)

                                    NowBarStyle.FLUID_PILL ->
                                        appearance.surface.copy(alpha = 0.40f)
                                }
                            )
                            .clickable {
                                when (item.type) {
                                    NowBarType.ACTION_SEARCH ->
                                        onSearchClick()

                                    NowBarType.ACTION_DRAWER ->
                                        onDrawerClick()

                                    NowBarType.ACTION_SETTINGS ->
                                        onSettingsClick()

                                    else -> Unit
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = appearance.onSurface,
                            modifier = Modifier.size(
                                when (config.nowBarStyle) {
                                    NowBarStyle.EXPRESSIVE_CAPSULE -> 21.dp
                                    NowBarStyle.FOCUS_ACTIONABLE -> 20.dp
                                    else -> 19.dp
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
