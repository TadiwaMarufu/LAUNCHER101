package com.example.core.model

enum class HomeItemType {
    APP,
    WIDGET,
    CLOCK,
    NOW_BAR,
    SHORTCUT,
    FOLDER
}

data class HomeItem(
    val id: String,
    val type: HomeItemType,
    val page: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 1,
    val height: Int = 1,
    val packageName: String? = null,
    val activityName: String? = null,
    val appWidgetId: Int? = null,
    val title: String? = null,
    val visible: Boolean = true
)
