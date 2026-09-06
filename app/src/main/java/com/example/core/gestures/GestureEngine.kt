package com.example.core.gestures

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Reusable Compose gesture handler for launcher interactions.
 */
fun Modifier.launcherGestures(
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
): Modifier = this.pointerInput(Unit) {
    var totalDragY = 0f
    var isDragging = false

    detectTapGestures(
        onDoubleTap = { onDoubleTap() },
        onLongPress = { onLongPress() }
    )
}
