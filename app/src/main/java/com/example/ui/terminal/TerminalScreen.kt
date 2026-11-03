package com.example.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LineType
import com.example.vm.VmEngine

@Composable
fun TerminalScreen(
    vmEngine: VmEngine,
    onLaunchGui: () -> Unit
) {
    val lines by vmEngine.terminalLines.collectAsState()
    val currentDir by vmEngine.currentDir.collectAsState()
    val isGuiInstalled by vmEngine.isGuiInstalled.collectAsState()
    val hostStats by vmEngine.memoryOptimizer.hostStats.collectAsState()
    val commandHistory by vmEngine.commandHistory.collectAsState()

    var inputCommand by remember { mutableStateOf("") }
    var historyIndex by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        color = Color(0xFF090D16)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Terminal Header / VM Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "NanoVM Console (tty1)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Host RAM badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Host RAM",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Host: ${hostStats.usedMemoryMb}MB",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // GUI Launch Quick Button if installed
                    if (isGuiInstalled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E5FF))
                                .clickable { onLaunchGui() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("quick_gui_launch_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DesktopWindows,
                                    contentDescription = "Run GUI",
                                    tint = Color(0xFF0A0F1D),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "GUI Ready",
                                    color = Color(0xFF0A0F1D),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Command Chips (Easy access for required commands!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131D31))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // If GUI not installed, highlight /install --gui_system_comp
                if (!isGuiInstalled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981))
                            .clickable {
                                inputCommand = "/install --gui_system_comp"
                                vmEngine.executeCommand("/install --gui_system_comp")
                                inputCommand = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("install_gui_quick_chip")
                    ) {
                        Text(
                            text = "⚡ /install --gui_system_comp",
                            color = Color(0xFF0A0F1D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // If installed or user wants to test, /run gui_system
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGuiInstalled) Color(0xFF00E5FF) else Color(0xFF1E293B))
                        .clickable {
                            inputCommand = "/run gui_system"
                            vmEngine.executeCommand("/run gui_system")
                            inputCommand = ""
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("run_gui_quick_chip")
                ) {
                    Text(
                        text = "🚀 /run gui_system",
                        color = if (isGuiInstalled) Color(0xFF0A0F1D) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                listOf("help", "neofetch", "free -m", "top", "memopt", "ls", "clear").forEach { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                vmEngine.executeCommand(cmd)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cmd,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Terminal Logs Output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                items(lines) { line ->
                    val textColor = when (line.type) {
                        LineType.INPUT -> Color(0xFF00E5FF)
                        LineType.SUCCESS -> Color(0xFF10B981)
                        LineType.WARNING -> Color(0xFFFBBF24)
                        LineType.ERROR -> Color(0xFFEF4444)
                        LineType.SYSTEM -> Color(0xFF94A3B8)
                        LineType.OUTPUT -> Color(0xFFE2E8F0)
                    }
                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            // Keyboard Accessory Bar (Tab, Ctrl, Esc, History Up/Down, Clear)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab (Autocomplete)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F2937))
                        .clickable {
                            val trimmed = inputCommand.trim()
                            if (trimmed.startsWith("/ins")) {
                                inputCommand = "/install --gui_system_comp"
                            } else if (trimmed.startsWith("/run")) {
                                inputCommand = "/run gui_system"
                            } else if (trimmed.startsWith("neo")) {
                                inputCommand = "neofetch"
                            } else if (trimmed.startsWith("mem")) {
                                inputCommand = "memopt"
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Tab", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                // History Up
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F2937))
                        .clickable {
                            if (commandHistory.isNotEmpty()) {
                                if (historyIndex < commandHistory.size - 1) {
                                    historyIndex++
                                }
                                val cmd = commandHistory[commandHistory.size - 1 - historyIndex]
                                inputCommand = cmd
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("▲", color = Color.White, fontSize = 11.sp)
                }

                // History Down
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F2937))
                        .clickable {
                            if (historyIndex > 0) {
                                historyIndex--
                                inputCommand = commandHistory[commandHistory.size - 1 - historyIndex]
                            } else if (historyIndex == 0) {
                                historyIndex = -1
                                inputCommand = ""
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("▼", color = Color.White, fontSize = 11.sp)
                }

                // Ctrl+C
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F2937))
                        .clickable { inputCommand = "" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Ctrl+C", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                // Esc
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F2937))
                        .clickable { inputCommand = "" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Esc", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Clear
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF374151))
                        .clickable { vmEngine.executeCommand("clear") }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Clear", color = Color(0xFFF87171), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Interactive Input Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "guest@nanovm:$currentDir$ ",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )

                BasicTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("terminal_command_input"),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(Color(0xFF00E5FF)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputCommand.isNotBlank()) {
                                vmEngine.executeCommand(inputCommand)
                                inputCommand = ""
                                historyIndex = -1
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            vmEngine.executeCommand(inputCommand)
                            inputCommand = ""
                            historyIndex = -1
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("terminal_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Execute Command",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
