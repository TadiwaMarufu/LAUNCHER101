package com.example.ui.drawer

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.apps.AppManager
import com.example.core.engine.ProfileConfig
import com.example.core.model.AppCategory
import com.example.core.model.AppItem
import com.example.core.model.IconShape
import com.example.core.model.LauncherProfile
import java.util.Locale

@Composable
fun AppIconImage(
    drawable: Drawable?,
    label: String,
    iconShape: IconShape,
    size: Dp,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shapeModifier = when (iconShape) {
        IconShape.CIRCLE -> CircleShape
        IconShape.SQUIRCLE -> RoundedCornerShape(18.dp)
        IconShape.ROUNDED_RECT -> RoundedCornerShape(12.dp)
        IconShape.TEARDROP -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shapeModifier)
            .background(Color(0xFF1F1A2C)),
        contentAlignment = Alignment.Center
    ) {
        if (drawable != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvas = drawContext.canvas.nativeCanvas
                drawable.setBounds(0, 0, size.toPx().toInt(), size.toPx().toInt())
                drawable.draw(canvas)
            }
        } else {
            // Fallback monogram
            Text(
                text = label.take(1).uppercase(Locale.getDefault()),
                fontSize = (size.value * 0.45).sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerView(
    allApps: List<AppItem>,
    categorizedApps: Map<AppCategory, List<AppItem>>,
    profile: LauncherProfile,
    config: ProfileConfig,
    iconShape: IconShape,
    showLabels: Boolean,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppCategory.ALL) }

    val categoryScrollState = rememberScrollState()

    val displayedApps = remember(searchQuery, selectedCategory, allApps, categorizedApps) {
        if (searchQuery.isNotBlank()) {
            allApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        } else {
            categorizedApps[selectedCategory] ?: allApps
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(profile.backgroundBase.copy(alpha = 0.98f))
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        // Top Bar: Search input and Close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(profile.surfaceBase)
                    .border(1.dp, config.cardBorderColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = profile.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search ${allApps.size} apps...",
                                fontSize = 14.sp,
                                color = profile.textSecondary
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = profile.textPrimary,
                            unfocusedTextColor = profile.textPrimary,
                            cursorColor = profile.primaryAccent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = profile.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(profile.surfaceBase)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Drawer",
                    tint = profile.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontally Scrollable Category Bar (MANDATORY SPEC: Categories must be horizontal!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppCategory.values().forEach { category ->
                val isSelected = category == selectedCategory && searchQuery.isBlank()
                val count = categorizedApps[category]?.size ?: 0

                val categoryBg = if (isSelected) profile.primaryAccent else profile.surfaceBase
                val categoryText = if (isSelected) Color.White else profile.textSecondary

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryBg)
                        .clickable {
                            searchQuery = ""
                            selectedCategory = category
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = categoryText
                    )
                    if (count > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else profile.primaryAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Apps
        if (displayedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No apps found for \"$searchQuery\"" else "No apps in this category",
                    color = profile.textSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(config.gridColumns),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedApps, key = { it.componentNameString }) { app ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            AppIconImage(
                                drawable = app.icon,
                                label = app.label,
                                iconShape = iconShape,
                                size = config.iconSize,
                                accentColor = profile.primaryAccent
                            )
                            if (app.isPinned) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(profile.primaryAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = "Pinned",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        if (showLabels) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = app.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = profile.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
