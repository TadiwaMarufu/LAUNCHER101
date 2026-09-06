package com.example.ui.profileswitcher

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LauncherProfile
import com.example.ui.theme.LocalLauncherAppearance

@Composable
fun ProfileSwitcherDialog(
    activeProfile: LauncherProfile,
    onSelectProfile: (LauncherProfile) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current

    val dialogShape = RoundedCornerShape(
        topStart = appearance.cornerRadius.dp,
        topEnd = appearance.cornerRadius.dp
    )

    val itemShape = RoundedCornerShape(
        (appearance.cornerRadius * 0.8f).dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(dialogShape)
            .background(appearance.background)
            .border(
                width = appearance.borderWidth.dp,
                color = appearance.divider,
                shape = dialogShape
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CHOOSE PERSONALITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = activeProfile.primaryAccent
                    )

                    Text(
                        text = "Five interpretations of Android",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appearance.onBackground
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(appearance.surface)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = appearance.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(LauncherProfile.values()) { profile ->
                    val isSelected = profile == activeProfile

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(itemShape)
                            .background(appearance.surface)
                            .border(
                                width = if (isSelected) {
                                    2.dp
                                } else {
                                    appearance.borderWidth.dp
                                },
                                color = if (isSelected) {
                                    profile.primaryAccent
                                } else {
                                    appearance.divider
                                },
                                shape = itemShape
                            )
                            .clickable {
                                onSelectProfile(profile)
                                onDismiss()
                            }
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(profile.primaryAccent)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = profile.title.uppercase(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = appearance.onSurface
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Active",
                                        tint = profile.primaryAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = profile.tagline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = profile.primaryAccent
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = profile.description,
                                fontSize = 12.sp,
                                color = appearance.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            MiniaturePersonalityPreview(
                                profile = profile,
                                appearance = appearance
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Small structural preview of each personality.
 *
 * The five personalities intentionally remain different:
 * - FLUID       = organic / reactive
 * - PREMIUM     = precise / architectural
 * - CALM        = quiet / minimal
 * - FOCUS       = information / command oriented
 * - EXPRESSIVE  = experimental / asymmetric
 *
 * The shared appearance controls the neutral surfaces and content hierarchy.
 * Profile accent remains personality-specific.
 */
@Composable
private fun MiniaturePersonalityPreview(
    profile: LauncherProfile,
    appearance: com.example.core.appearance.LauncherAppearance
) {
    val previewShape = RoundedCornerShape(
        (appearance.cornerRadius * 0.5f).dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(previewShape)
            .background(appearance.elevatedSurface)
            .border(
                width = (appearance.borderWidth * 0.5f).dp,
                color = appearance.divider,
                shape = previewShape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (profile) {
            LauncherProfile.FLUID -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appearance.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "12:45",
                            fontSize = 10.sp,
                            color = profile.primaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(appearance.surface)
                            )
                        }
                    }
                }
            }

            LauncherProfile.PREMIUM -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "18 : 54",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = appearance.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(appearance.surface)
                            )
                        }
                    }
                }
            }

            LauncherProfile.CALM -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "14:30  •  wednesday",
                        fontSize = 11.sp,
                        color = appearance.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(profile.primaryAccent)
                    )
                }
            }

            LauncherProfile.FOCUS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "09:00  [2 TASKS]",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = profile.primaryAccent
                    )

                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(appearance.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "COMMAND",
                            fontSize = 9.sp,
                            color = appearance.onSurface
                        )
                    }
                }
            }

            LauncherProfile.EXPRESSIVE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "17\n07",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 13.sp,
                        color = profile.primaryAccent
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(profile.primaryAccent)
                        )

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(appearance.surface)
                        )
                    }
                }
            }
        }
    }
}
