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

@Composable
fun ProfileSwitcherDialog(
    activeProfile: LauncherProfile,
    onSelectProfile: (LauncherProfile) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF100C1B))
            .border(1.dp, Color(0x33A855F7), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
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
                        color = Color(0xFFA855F7)
                    )
                    Text(
                        text = "Five interpretations of Android",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C162A))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(profile.surfaceBase.copy(alpha = 0.9f))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) profile.primaryAccent else Color(0x25FFFFFF),
                                shape = RoundedCornerShape(20.dp)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        color = profile.textPrimary
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
                                color = profile.textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Miniature Representation of the Personality's Layout
                            MiniaturePersonalityPreview(profile)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual miniature preview communicating that these are structurally different experiences.
 */
@Composable
private fun MiniaturePersonalityPreview(profile: LauncherProfile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(profile.backgroundBase)
            .border(0.5.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (profile) {
            LauncherProfile.FLUID -> {
                // Organic pill clock + dynamic pill Now Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(profile.surfaceBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("12:45", fontSize = 10.sp, color = profile.primaryAccent, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(profile.surfaceBase)
                            )
                        }
                    }
                }
            }

            LauncherProfile.PREMIUM -> {
                // Architectural split large clock + high contrast dock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("18 : 54", fontSize = 14.sp, fontWeight = FontWeight.Light, color = profile.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(profile.surfaceBase)
                            )
                        }
                    }
                }
            }

            LauncherProfile.CALM -> {
                // Minimal Zen inline line + generous whitespace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("14:30  •  wednesday", fontSize = 11.sp, color = profile.textSecondary)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(profile.primaryAccent)
                    )
                }
            }

            LauncherProfile.FOCUS -> {
                // Dashboard agenda + task check + action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("09:00  [2 TASKS]", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = profile.primaryAccent)
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(profile.surfaceBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("COMMAND", fontSize = 9.sp, color = Color.White)
                    }
                }
            }

            LauncherProfile.EXPRESSIVE -> {
                // Staggered massive digits + artistic asymmetrical arrangement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("17\n07", fontSize = 16.sp, fontWeight = FontWeight.Black, lineHeight = 13.sp, color = profile.primaryAccent)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(profile.primaryAccent))
                        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(profile.surfaceBase))
                    }
                }
            }
        }
    }
}
