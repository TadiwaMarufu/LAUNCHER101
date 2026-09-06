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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.NowBarStyle
import com.example.core.engine.ProfileConfig
import com.example.core.model.LauncherProfile
import com.example.ui.theme.LocalLauncherAppearance
import com.example.core.model.MediaPlaybackState
import com.example.core.model.NowBarItem
import com.example.core.model.NowBarType

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
                appearance.surface.copy(alpha = 0.9f),
                appearance.primary.copy(alpha = 0.35f)
            )
        )
        else -> Brush.linearGradient(
            colors = listOf(
                appearance.surface.copy(alpha = appearance.surfaceAlpha),
                appearance.surface.copy(alpha = appearance.surfaceAlpha)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .background(backgroundBrush)
            .border(appearance.borderWidth.dp, appearance.divider, barShape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile switcher pill on the left
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(appearance.primary.copy(alpha = 0.25f))
                    .clickable { onProfileChipClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(appearance.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profile.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onSurface
                )
            }

            // Center: Interactive Context / Media / Focus Glance
            if (config.nowBarStyle != NowBarStyle.CALM_ZEN) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMediaPlayToggle() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (mediaState.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                        contentDescription = "Media state",
                        tint = appearance.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (mediaState.isPlaying) mediaState.title else "Purple Frequency",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = appearance.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Right: Primary launcher shortcuts (Search, Drawer, Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(appearance.surface.copy(alpha = 0.4f))
                            .clickable {
                                when (item.type) {
                                    NowBarType.ACTION_SEARCH -> onSearchClick()
                                    NowBarType.ACTION_DRAWER -> onDrawerClick()
                                    NowBarType.ACTION_SETTINGS -> onSettingsClick()
                                    else -> {}
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = appearance.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
