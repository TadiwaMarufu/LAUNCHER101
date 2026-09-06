package com.example.ui.widgets

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.ClockStyle
import com.example.core.engine.ProfileConfig
import com.example.core.model.BatteryInfoState
import com.example.core.model.LauncherProfile
import com.example.core.model.LauncherTask
import com.example.core.model.MediaPlaybackState
import com.example.ui.theme.LocalLauncherAppearance
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clock Widget supporting all 5 personality styles.
 */
@Composable
fun PersonalityClockWidget(
    config: ProfileConfig,
    modifier: Modifier = Modifier,
    onClockClick: () -> Unit = {}
) {
    val appearance = LocalLauncherAppearance.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
    val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat(
        "EEEE, d MMMM",
        Locale.getDefault()
    )
    val shortDateFormat = SimpleDateFormat(
        "EEE, d MMM",
        Locale.getDefault()
    )

    val shape = RoundedCornerShape(config.cornerRadius)

    when (config.clockStyle) {

        /*
         * FLUID
         *
         * Soft, contextual and alive.
         * The clock is a composed surface rather than a bare number.
         */
        ClockStyle.FLUID_ROUNDED -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(
                        appearance.surface.copy(
                            alpha = appearance.surfaceAlpha
                        )
                    )
                    .border(
                        appearance.borderWidth.dp,
                        appearance.divider.copy(alpha = 0.55f),
                        shape
                    )
                    .clickable { onClockClick() }
                    .padding(
                        horizontal = 22.dp,
                        vertical = 18.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = timeFormat.format(currentTime),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2).sp,
                        color = appearance.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = dateFormat.format(currentTime),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = appearance.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(appearance.primary)
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = "NOW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = appearance.primary
                    )
                }
            }
        }

        /*
         * PREMIUM
         *
         * Architectural typography.
         * Large hour, restrained minute, precise metadata.
         */
        ClockStyle.PREMIUM_SPLIT -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClockClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(1.dp)
                            .background(appearance.primary)
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Text(
                        text = dateFormat
                            .format(currentTime)
                            .uppercase(Locale.getDefault()),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = appearance.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = hourFormat.format(currentTime),
                        fontSize = 70.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-4).sp,
                        color = appearance.onSurface
                    )

                    Text(
                        text = ":",
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Light,
                        color = appearance.primary,
                        modifier = Modifier.padding(
                            horizontal = 3.dp,
                            vertical = 7.dp
                        )
                    )

                    Text(
                        text = minuteFormat.format(currentTime),
                        fontSize = 70.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-4).sp,
                        color = appearance.onSurface.copy(alpha = 0.72f)
                    )
                }
            }
        }

        /*
         * CALM
         *
         * Almost disappears into the wallpaper.
         * No card, no decorative accent, no unnecessary information.
         */
        ClockStyle.CALM_INLINE -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClockClick() },
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = timeFormat.format(currentTime),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp,
                    color = appearance.onSurface
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = shortDateFormat
                        .format(currentTime)
                        .lowercase(Locale.getDefault()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = appearance.onSurfaceVariant.copy(
                        alpha = 0.72f
                    ),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }

        /*
         * FOCUS
         *
         * Information-first.
         * Time is paired with an explicit working-state indicator.
         */
        ClockStyle.FOCUS_DIGITAL -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            (config.cornerRadius * 0.72f)
                        )
                    )
                    .background(
                        appearance.surface.copy(
                            alpha = appearance.surfaceAlpha
                        )
                    )
                    .border(
                        appearance.borderWidth.dp,
                        appearance.divider,
                        RoundedCornerShape(
                            (config.cornerRadius * 0.72f)
                        )
                    )
                    .clickable { onClockClick() }
                    .padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = timeFormat.format(currentTime),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1).sp,
                        color = appearance.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = shortDateFormat.format(currentTime),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = appearance.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "FOCUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = appearance.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(appearance.primary)
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = appearance.onSurfaceVariant
                        )
                    }
                }
            }
        }

        /*
         * EXPRESSIVE
         *
         * Deliberately oversized typography.
         * Composition is the decoration; no card is required.
         */
        ClockStyle.EXPRESSIVE_MASSIVE -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClockClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = hourFormat.format(currentTime),
                        fontSize = 82.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 72.sp,
                        letterSpacing = (-5).sp,
                        color = appearance.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = minuteFormat.format(currentTime),
                        fontSize = 82.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 72.sp,
                        letterSpacing = (-5).sp,
                        color = appearance.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(2.dp)
                            .background(appearance.primary)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = dateFormat.format(currentTime),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appearance.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Interactive Media Player Widget.
 */
@Composable
fun MediaWidgetCard(
    mediaState: MediaPlaybackState,
    profile: LauncherProfile,
    config: ProfileConfig,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(appearance.surface.copy(alpha = appearance.surfaceAlpha))
            .border(appearance.borderWidth.dp, appearance.divider, RoundedCornerShape(config.cornerRadius))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appearance.primary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Media Cover",
                            tint = appearance.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = mediaState.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = appearance.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = mediaState.artist,
                            fontSize = 12.sp,
                            color = appearance.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(appearance.primary)
                    ) {
                        Icon(
                            imageVector = if (mediaState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                            tint = appearance.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Track",
                            tint = appearance.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { mediaState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = appearance.primary,
                trackColor = appearance.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * Focus Checklist & Tasks Widget.
 */
@Composable
fun FocusTasksWidgetCard(
    tasks: List<LauncherTask>,
    profile: LauncherProfile,
    config: ProfileConfig,
    onToggleTask: (String) -> Unit,
    onAddTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current
    var isAddingTask by remember { mutableStateOf(false) }
    var newTaskText by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(appearance.surface.copy(alpha = appearance.surfaceAlpha))
            .border(appearance.borderWidth.dp, appearance.divider, RoundedCornerShape(config.cornerRadius))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FOCUS PRIORITIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = appearance.primary
                )

                IconButton(
                    onClick = { isAddingTask = !isAddingTask },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Task",
                        tint = appearance.primary
                    )
                }
            }

            AnimatedVisibility(visible = isAddingTask) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("Enter task...", color = appearance.onSurfaceVariant) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = appearance.onSurface,
                            unfocusedTextColor = appearance.onSurface
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                onAddTask(newTaskText.trim())
                                newTaskText = ""
                                isAddingTask = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Submit",
                            tint = appearance.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val displayTasks = tasks.take(3)
                if (displayTasks.isEmpty()) {
                    Text(
                        text = "All tasks completed. Deep focus achieved.",
                        fontSize = 12.sp,
                        color = appearance.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    displayTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleTask(task.id) }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (task.isCompleted) "Completed" else "Incomplete",
                                tint = if (task.isCompleted) appearance.primary else appearance.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = task.text,
                                fontSize = 13.sp,
                                color = if (task.isCompleted) appearance.onSurfaceVariant else appearance.onSurface,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Weather & Atmosphere Mini Card.
 */
@Composable
fun WeatherCard(
    profile: LauncherProfile,
    config: ProfileConfig,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(appearance.surface.copy(alpha = appearance.surfaceAlpha))
            .border(appearance.borderWidth.dp, appearance.divider, RoundedCornerShape(config.cornerRadius))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.WbSunny,
            contentDescription = "Sun",
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "24°C",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = appearance.onSurface
            )
            Text(
                text = "Sunny • Calm Breeze",
                fontSize = 11.sp,
                color = appearance.onSurfaceVariant
            )
        }
    }
}

/**
 * System Telemetry Card (RAM & Battery).
 */
@Composable
fun SystemTelemetryCard(
    battery: BatteryInfoState,
    profile: LauncherProfile,
    config: ProfileConfig,
    modifier: Modifier = Modifier
) {
    val appearance = LocalLauncherAppearance.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(appearance.surface.copy(alpha = appearance.surfaceAlpha))
            .border(appearance.borderWidth.dp, appearance.divider, RoundedCornerShape(config.cornerRadius))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (battery.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
            contentDescription = "Battery",
            tint = appearance.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "${battery.percentage}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = appearance.onSurface
            )
            Text(
                text = if (battery.isCharging) "Charging" else "Healthy",
                fontSize = 10.sp,
                color = appearance.onSurfaceVariant
            )
        }
    }
}
