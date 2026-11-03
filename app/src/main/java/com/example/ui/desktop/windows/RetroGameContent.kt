package com.example.ui.desktop.windows

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

@Composable
fun RetroGameContent() {
    val gridSize = 16
    var snake by remember { mutableStateOf(listOf(Pair(8, 8), Pair(8, 9), Pair(8, 10))) }
    var food by remember { mutableStateOf(Pair(4, 4)) }
    var direction by remember { mutableStateOf(Direction.UP) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }

    fun spawnFood(): Pair<Int, Int> {
        var p: Pair<Int, Int>
        do {
            p = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
        } while (p in snake)
        return p
    }

    fun restart() {
        snake = listOf(Pair(8, 8), Pair(8, 9), Pair(8, 10))
        direction = Direction.UP
        score = 0
        food = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
        isGameOver = false
        isRunning = true
    }

    LaunchedEffect(isRunning, isGameOver) {
        while (isRunning && !isGameOver) {
            delay(170)
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> Pair(head.first, (head.second - 1 + gridSize) % gridSize)
                Direction.DOWN -> Pair(head.first, (head.second + 1) % gridSize)
                Direction.LEFT -> Pair((head.first - 1 + gridSize) % gridSize, head.second)
                Direction.RIGHT -> Pair((head.first + 1) % gridSize, head.second)
            }

            if (newHead in snake) {
                isGameOver = true
            } else {
                val newSnake = mutableListOf(newHead)
                if (newHead == food) {
                    newSnake.addAll(snake)
                    score += 10
                    food = spawnFood()
                } else {
                    newSnake.addAll(snake.dropLast(1))
                }
                snake = newSnake
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RETRO SNAKE VM",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Score: $score",
                color = Color(0xFF10B981),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Game Canvas
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0B132B)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / gridSize
                val cellH = size.height / gridSize

                // Draw food
                drawRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(food.first * cellW + 1f, food.second * cellH + 1f),
                    size = Size(cellW - 2f, cellH - 2f)
                )

                // Draw snake
                snake.forEachIndexed { index, segment ->
                    val color = if (index == 0) Color(0xFF00E5FF) else Color(0xFF10B981)
                    drawRect(
                        color = color,
                        topLeft = Offset(segment.first * cellW + 1f, segment.second * cellH + 1f),
                        size = Size(cellW - 2f, cellH - 2f)
                    )
                }
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GAME OVER",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { restart() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Play Again", color = Color(0xFF0A0F1D), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // D-Pad Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // UP
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable { if (direction != Direction.DOWN) direction = Direction.UP },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                // LEFT
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .clickable { if (direction != Direction.RIGHT) direction = Direction.LEFT },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White)
                }

                // RIGHT
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .clickable { if (direction != Direction.LEFT) direction = Direction.RIGHT },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Right", tint = Color.White)
                }
            }

            // DOWN
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable { if (direction != Direction.UP) direction = Direction.DOWN },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White)
            }
        }
    }
}
