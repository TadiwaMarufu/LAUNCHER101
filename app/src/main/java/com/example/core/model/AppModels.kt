package com.example.core.model

import android.graphics.drawable.Drawable

/**
 * Categorization for the horizontally scrollable app drawer.
 */
enum class AppCategory(val title: String) {
    ALL("All"),
    FAVORITES("Pinned"),
    COMMUNICATION("Communication"),
    SOCIAL("Social"),
    MEDIA("Media"),
    WORK("Work"),
    GAMES("Games"),
    TOOLS("Tools"),
    SYSTEM("System")
}

/**
 * Represents an installed Android application.
 */
data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.ALL,
    val launchCount: Int = 0,
    val lastLaunched: Long = 0L,
    val isPinned: Boolean = false
) {
    val componentNameString: String
        get() = "$packageName/$activityName"
}
