package com.example.ui.desktop.windows

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NanoBrowserContent() {
    var currentUrl by remember { mutableStateOf("nano://docs/welcome") }

    val pages = mapOf(
        "nano://docs/welcome" to """
# Welcome to NanoOS Micro-Browser

This built-in reader is designed with extreme memory optimization, consuming less than 1MB of memory compared to standard 150MB+ WebViews.

## Quick Links:
• nano://docs/commands - Shell Commands Manual
• nano://docs/memory - Low-RAM Architecture
• nano://docs/gui - GUI Compositor Details
• nano://docs/specs - System Specifications

Zero JavaScript overhead. Zero external tracking. Pure high-speed documentation.
        """.trimIndent(),
        "nano://docs/commands" to """
# NanoOS Shell Commands Reference

• /install --gui_system_comp
  Installs the graphical user interface component suite.

• /run gui_system
  Boots the lightweight X-Nano window manager.

• memopt
  Triggers immediate JVM host garbage collection and trims buffers.

• free -m
  Shows simulated virtual RAM alongside actual real host RAM footprint.

• top
  Displays low-overhead process monitor.
        """.trimIndent(),
        "nano://docs/memory" to """
# Memory Optimization Architecture

NanoVM achieves negligible host RAM consumption (~30-40MB) through:

1. Lightweight State Pruning:
   Only active visible windows preserve rendered subtrees.

2. Minimal Draw Buffer:
   Avoids heavyweight multi-pass blurs and expensive GPU composition layers.

3. Explicit GC Pipeline:
   Runs background finalization to maintain a tiny working set on budget Android devices.
        """.trimIndent(),
        "nano://docs/specs" to """
# Hardware Specifications

• Emulated CPU: 2 Virtualized RISC Cores (1.2 GHz)
• Memory Allocation: 256MB / 512MB / 1024MB selectable
• Virtual Swap: 128MB zram
• Host Target: Sub-50MB Android JVM Heap
• Display Pipeline: Double-Buffered 60 FPS compositor
        """.trimIndent()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Address Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "URL",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = currentUrl,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bookmarks Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "Welcome" to "nano://docs/welcome",
                "Commands" to "nano://docs/commands",
                "Memory" to "nano://docs/memory",
                "Specs" to "nano://docs/specs"
            ).forEach { (title, url) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentUrl == url) Color(0xFF00E5FF) else Color(0xFF131D31))
                        .clickable { currentUrl = url }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = title,
                        color = if (currentUrl == url) Color(0xFF0A0F1D) else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        val pageText = pages[currentUrl] ?: "Page not found: $currentUrl"
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0D1424))
                .padding(10.dp)
        ) {
            item {
                Text(
                    text = pageText,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
