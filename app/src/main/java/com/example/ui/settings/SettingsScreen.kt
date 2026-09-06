package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.LauncherDependencies
import com.example.core.model.IconShape
import com.example.core.model.QualityTier
import com.example.core.model.WallpaperPreset
import com.example.ui.theme.LocalLauncherAppearance

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenProfileSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appearance = LocalLauncherAppearance.current

    val dependencies = LauncherDependencies.get(context)

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(dependencies)
    )

    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val activeProfile = state.activeProfile

    val sectionShape = RoundedCornerShape(appearance.cornerRadius.dp)
    val itemShape = RoundedCornerShape((appearance.cornerRadius * 0.66f).dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appearance.background)
            .padding(
                top = 16.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 16.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(appearance.surface)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = appearance.onSurface
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = activeProfile.primaryAccent
                )

                Text(
                    text = "The Purple Launcher",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            SettingsSectionHeader(
                title = "Personality & Identity",
                icon = Icons.Rounded.Tune,
                accent = activeProfile.primaryAccent
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(sectionShape)
                    .background(appearance.surface)
                    .border(
                        width = appearance.borderWidth.dp,
                        color = activeProfile.primaryAccent.copy(alpha = 0.5f),
                        shape = sectionShape
                    )
                    .clickable {
                        onOpenProfileSwitcher()
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeProfile.primaryAccent)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Active: ${activeProfile.title}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = appearance.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = activeProfile.tagline,
                            fontSize = 12.sp,
                            color = activeProfile.primaryAccent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(itemShape)
                            .background(
                                activeProfile.primaryAccent.copy(alpha = 0.2f)
                            )
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                    ) {
                        Text(
                            text = "Switch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeProfile.primaryAccent
                        )
                    }
                }
            }

            SettingsSectionHeader(
                title = "Atmosphere & Icons",
                icon = Icons.Rounded.Wallpaper,
                accent = activeProfile.primaryAccent
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(sectionShape)
                    .background(appearance.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "WALLPAPER PRESET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperPreset.values().forEach { preset ->
                        val isSelected = preset == state.wallpaperPreset

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(itemShape)
                                .background(
                                    if (isSelected) {
                                        activeProfile.primaryAccent
                                    } else {
                                        appearance.elevatedSurface
                                    }
                                )
                                .clickable {
                                    settingsViewModel.setWallpaperPreset(preset)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                                color = if (isSelected) {
                                    activeProfile.primaryAccent.contrastColor()
                                } else {
                                    appearance.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ICON SHAPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconShape.values().forEach { shape ->
                        val isSelected = shape == state.iconShape

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(itemShape)
                                .background(
                                    if (isSelected) {
                                        activeProfile.primaryAccent
                                    } else {
                                        appearance.elevatedSurface
                                    }
                                )
                                .clickable {
                                    settingsViewModel.setIconShape(shape)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shape.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                                color = if (isSelected) {
                                    activeProfile.primaryAccent.contrastColor()
                                } else {
                                    appearance.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Icon Labels",
                        fontSize = 14.sp,
                        color = appearance.onSurface
                    )

                    Switch(
                        checked = state.showIconLabels,
                        onCheckedChange = {
                            settingsViewModel.setShowIconLabels(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = activeProfile.primaryAccent.contrastColor(),
                            checkedTrackColor = activeProfile.primaryAccent,
                            uncheckedThumbColor = appearance.onSurfaceVariant,
                            uncheckedTrackColor = appearance.elevatedSurface,
                            uncheckedBorderColor = appearance.divider
                        )
                    )
                }
            }

            SettingsSectionHeader(
                title = "Hardware & Performance",
                icon = Icons.Rounded.Speed,
                accent = activeProfile.primaryAccent
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(sectionShape)
                    .background(appearance.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QualityTier.values().forEach { tier ->
                    val isSelected = tier == state.qualityTier

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(itemShape)
                            .background(
                                if (isSelected) {
                                    activeProfile.primaryAccent.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                settingsViewModel.setQualityTier(tier)
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = tier.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        activeProfile.primaryAccent
                                    } else {
                                        appearance.onSurface
                                    }
                                )

                                Text(
                                    text = tier.description,
                                    fontSize = 11.sp,
                                    color = appearance.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = activeProfile.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            SettingsSectionHeader(
                title = "Privacy & Local Intelligence",
                icon = Icons.Rounded.Security,
                accent = activeProfile.primaryAccent
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(sectionShape)
                    .background(appearance.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Local App Usage Tracking",
                            fontSize = 14.sp,
                            color = appearance.onSurface
                        )

                        Text(
                            text = "100% on-device. Used to sort frequent apps.",
                            fontSize = 11.sp,
                            color = appearance.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = state.trackAppUsage,
                        onCheckedChange = {
                            settingsViewModel.setTrackAppUsage(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = activeProfile.primaryAccent.contrastColor(),
                            checkedTrackColor = activeProfile.primaryAccent,
                            uncheckedThumbColor = appearance.onSurfaceVariant,
                            uncheckedTrackColor = appearance.elevatedSurface,
                            uncheckedBorderColor = appearance.divider
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Contextual Suggestions",
                            fontSize = 14.sp,
                            color = appearance.onSurface
                        )

                        Text(
                            text = "Adapts Now Bar and search suggestions locally.",
                            fontSize = 11.sp,
                            color = appearance.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = state.contextualSuggestions,
                        onCheckedChange = {
                            settingsViewModel.setContextualSuggestions(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = activeProfile.primaryAccent.contrastColor(),
                            checkedTrackColor = activeProfile.primaryAccent,
                            uncheckedThumbColor = appearance.onSurfaceVariant,
                            uncheckedTrackColor = appearance.elevatedSurface,
                            uncheckedBorderColor = appearance.divider
                        )
                    )
                }
            }

            DefaultLauncherCard(
                context = context,
                accent = activeProfile.primaryAccent,
                appearance = appearance
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "The Purple Launcher v0.1",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onSurface
                )

                Text(
                    text = "Android, in your own frequency.",
                    fontSize = 12.sp,
                    color = activeProfile.primaryAccent
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "No ads • No subscriptions • Enthusiast built",
                    fontSize = 10.sp,
                    color = appearance.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DefaultLauncherCard(
    context: Context,
    accent: Color,
    appearance: com.example.core.appearance.LauncherAppearance
) {
    val shape = RoundedCornerShape(appearance.cornerRadius.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(appearance.surface)
            .clickable {
                openDefaultLauncherSettings(context)
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Home,
                contentDescription = "Default Launcher",
                tint = accent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Set Default Launcher",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.onSurface
                )

                Text(
                    text = "Open Android default apps settings",
                    fontSize = 11.sp,
                    color = appearance.onSurfaceVariant
                )
            }
        }
    }
}

private fun openDefaultLauncherSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (_: Exception) {
            // Some OEMs expose neither settings activity.
            // Failing silently keeps the launcher home stable.
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = accent
        )
    }
}

private fun Color.contrastColor(): Color {
    val luminance = (0.299f * red) +
        (0.587f * green) +
        (0.114f * blue)

    return if (luminance > 0.55f) {
        Color.Black
    } else {
        Color.White
    }
}
