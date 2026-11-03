package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.VmWindow
import kotlin.math.roundToInt

@Composable
fun WindowFrame(
    window: VmWindow,
    isActive: Boolean,
    isLowRamMode: Boolean,
    onBringToFront: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    if (window.isMinimized) return

    var currentX by remember(window.id, window.x) { mutableFloatStateOf(window.x) }
    var currentY by remember(window.id, window.y) { mutableFloatStateOf(window.y) }

    val shape = RoundedCornerShape(10.dp)
    val shadowElevation = if (isLowRamMode) 0.dp else if (isActive) 8.dp else 3.dp

    val modifier = if (window.isMaximized) {
        Modifier
            .fillMaxSize()
            .padding(top = 44.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
            .zIndex(window.zIndex.toFloat())
    } else {
        Modifier
            .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
            .size(window.width.dp, window.height.dp)
            .zIndex(window.zIndex.toFloat())
    }

    Surface(
        modifier = modifier
            .testTag("window_${window.id}")
            .shadow(shadowElevation, shape)
            .clip(shape)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) Color(0xFF00E5FF) else Color(0xFF334155),
                shape = shape
            ),
        color = Color(0xFF0F172A),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(window.id) {
                    onBringToFront()
                }
        ) {
            // Window Header / Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(if (isActive) Color(0xFF1E293B) else Color(0xFF141E33))
                    .pointerInput(window.id) {
                        detectDragGestures(
                            onDragStart = { onBringToFront() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (!window.isMaximized) {
                                    currentX += dragAmount.x
                                    currentY += dragAmount.y
                                    onMove(currentX, currentY)
                                }
                            }
                        )
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Window Title with indicator dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFF00E5FF) else Color(0xFF64748B))
                    )
                    Text(
                        text = window.title,
                        color = if (isActive) Color.White else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Window Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("win_min_${window.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minimize",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    // Maximize
                    IconButton(
                        onClick = onMaximize,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("win_max_${window.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropSquare,
                            contentDescription = "Maximize",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    // Close
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("win_close_${window.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Window Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0F1D))
            ) {
                content()
            }
        }
    }
}
