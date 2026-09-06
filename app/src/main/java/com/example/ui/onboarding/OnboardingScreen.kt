package com.example.ui.onboarding

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
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LauncherProfile
import com.example.ui.theme.LocalLauncherAppearance

@Composable
fun OnboardingScreen(
    onComplete: (LauncherProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appearance = LocalLauncherAppearance.current
    val scrollState = rememberScrollState()

    var selectedProfile by remember {
        mutableStateOf(LauncherProfile.FLUID)
    }

    val selectedAccent = selectedProfile.primaryAccent

    val screenShape = RoundedCornerShape(
        appearance.cornerRadius.dp
    )

    val cardShape = RoundedCornerShape(
        (appearance.cornerRadius * 0.67f).dp
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        appearance.surface,
                        appearance.background
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Brand mark
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                selectedAccent.copy(alpha = 0.95f),
                                selectedAccent.copy(alpha = 0.35f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "λ",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = selectedAccent.contrastColor()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "The Purple Launcher",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = appearance.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Android, in your own frequency.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = selectedAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Built for enthusiasts who wanted Android to feel personal, experimental, and fun again. Choose how your device behaves:",
                fontSize = 13.sp,
                color = appearance.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LauncherProfile.values().forEach { profile ->
                    val isSelected = profile == selectedProfile

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .background(
                                if (isSelected) {
                                    profile.primaryAccent.copy(alpha = 0.12f)
                                } else {
                                    appearance.surface
                                }
                            )
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
                                shape = cardShape
                            )
                            .clickable {
                                selectedProfile = profile
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(profile.primaryAccent)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = profile.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appearance.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = profile.tagline,
                                    fontSize = 12.sp,
                                    color = profile.primaryAccent
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = profile.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = {
                    openDefaultLauncherSettings(context)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = selectedAccent
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = appearance.borderWidth.dp,
                    color = appearance.divider
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Set Default",
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Set as Default Launcher",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onComplete(selectedProfile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = cardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedAccent,
                    contentColor = selectedAccent.contrastColor()
                )
            ) {
                Text(
                    text = "Launch ${selectedProfile.title} Frequency",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Start",
                    tint = selectedAccent.contrastColor(),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
            // Failing silently keeps onboarding stable.
        }
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
