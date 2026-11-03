package com.example.ui.desktop

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.model.VmWindow
import com.example.model.WindowType
import com.example.ui.components.WindowFrame
import com.example.ui.desktop.windows.CalculatorContent
import com.example.ui.desktop.windows.FileExplorerContent
import com.example.ui.desktop.windows.NanoBrowserContent
import com.example.ui.desktop.windows.RetroGameContent
import com.example.ui.desktop.windows.SettingsContent
import com.example.ui.desktop.windows.SystemMonitorContent
import com.example.ui.desktop.windows.TerminalWindowContent
import com.example.ui.desktop.windows.TextEditorContent
import com.example.vm.VmEngine
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DesktopScreen(
    vmEngine: VmEngine,
    onReturnToTerminal: () -> Unit
) {
    val windows by vmEngine.windows.collectAsState()
    val activeWindowId by vmEngine.activeWindowId.collectAsState()
    val hostStats by vmEngine.memoryOptimizer.hostStats.collectAsState()

    var isStartMenuOpen by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }

    // Live clock
    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = format.format(Date())
            delay(1000)
        }
    }

    // Android back button handling: close active window first, or exit to terminal if no window
    BackHandler {
        if (isStartMenuOpen) {
            isStartMenuOpen = false
        } else if (activeWindowId != null) {
            vmEngine.closeWindow(activeWindowId!!)
        } else {
            onReturnToTerminal()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF0A0E1A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Desktop Wallpaper
            Image(
                painter = painterResource(id = R.drawable.nano_os_wallpaper),
                contentDescription = "Desktop Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Subtle dark overlay to maximize text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090D16).copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isStartMenuOpen) isStartMenuOpen = false
                    }
            )

            // Window Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp)
            ) {
                windows.forEach { window ->
                    WindowFrame(
                        window = window,
                        isActive = window.id == activeWindowId,
                        isLowRamMode = hostStats.isLowRamModeActive,
                        onBringToFront = { vmEngine.bringWindowToFront(window.id) },
                        onMove = { newX, newY -> vmEngine.updateWindowPosition(window.id, newX, newY) },
                        onMinimize = { vmEngine.toggleMinimizeWindow(window.id) },
                        onMaximize = { vmEngine.toggleMaximizeWindow(window.id) },
                        onClose = { vmEngine.closeWindow(window.id) }
                    ) {
                        when (window.type) {
                            WindowType.FILE_EXPLORER -> {
                                FileExplorerContent(
                                    fileSystem = vmEngine.fileSystem,
                                    onOpenFile = { path ->
                                        vmEngine.openWindow(WindowType.TEXT_EDITOR, extraData = path)
                                    }
                                )
                            }
                            WindowType.TEXT_EDITOR -> {
                                TextEditorContent(
                                    filePath = window.extraData,
                                    fileSystem = vmEngine.fileSystem
                                )
                            }
                            WindowType.SYSTEM_MONITOR -> {
                                SystemMonitorContent(
                                    vmEngine = vmEngine,
                                    memoryOptimizer = vmEngine.memoryOptimizer
                                )
                            }
                            WindowType.CALCULATOR -> CalculatorContent()
                            WindowType.BROWSER -> NanoBrowserContent()
                            WindowType.RETRO_GAME -> RetroGameContent()
                            WindowType.SETTINGS -> {
                                SettingsContent(
                                    vmEngine = vmEngine,
                                    memoryOptimizer = vmEngine.memoryOptimizer,
                                    fileSystem = vmEngine.fileSystem
                                )
                            }
                            WindowType.TERMINAL -> TerminalWindowContent(vmEngine = vmEngine)
                        }
                    }
                }
            }

            // Top Taskbar (Panel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(width = 1.dp, color = Color(0xFF1E293B))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Start Menu Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isStartMenuOpen) Color(0xFF00E5FF) else Color(0xFF1E293B))
                            .clickable { isStartMenuOpen = !isStartMenuOpen }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("start_menu_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isStartMenuOpen) Color(0xFF0A0F1D) else Color(0xFF00E5FF))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "NanoOS",
                                color = if (isStartMenuOpen) Color(0xFF0A0F1D) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Running Tasks / Windows list
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        windows.forEach { win ->
                            val isActive = win.id == activeWindowId && !win.isMinimized
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isActive) Color(0xFF334155) else Color(0xFF1E293B))
                                    .clickable {
                                        if (isActive) {
                                            vmEngine.toggleMinimizeWindow(win.id)
                                        } else {
                                            vmEngine.bringWindowToFront(win.id)
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = win.title,
                                    color = if (isActive) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Right: System HUD & Quick Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Host RAM badge (Low-overhead monitor)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { vmEngine.openWindow(WindowType.SYSTEM_MONITOR) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("host_ram_hud")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "RAM",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${hostStats.usedMemoryMb}M",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Return to CLI Terminal (tty1)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { onReturnToTerminal() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("return_to_terminal_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "CLI",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "tty1",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Clock
                    Text(
                        text = currentTime,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Start Menu Popup
            AnimatedVisibility(
                visible = isStartMenuOpen,
                enter = slideInVertically { -it / 2 } + fadeIn(),
                exit = slideOutVertically { -it / 2 } + fadeOut(),
                modifier = Modifier
                    .padding(top = 44.dp, start = 8.dp)
                    .zIndex(100f)
            ) {
                Surface(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
                    color = Color(0xFF0F172A),
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // User Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VM",
                                    color = Color(0xFF0A0F1D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "guest@nanovm",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "NanoOS Light v2.4",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // App Items
                        val appItems = listOf(
                            Triple("File Explorer", Icons.Default.Folder, WindowType.FILE_EXPLORER),
                            Triple("Text Editor", Icons.Default.Edit, WindowType.TEXT_EDITOR),
                            Triple("System Monitor", Icons.Default.Speed, WindowType.SYSTEM_MONITOR),
                            Triple("Terminal Shell", Icons.Default.Terminal, WindowType.TERMINAL),
                            Triple("Calculator", Icons.Default.Calculate, WindowType.CALCULATOR),
                            Triple("Nano Browser", Icons.Default.Public, WindowType.BROWSER),
                            Triple("Retro Snake", Icons.Default.Gamepad, WindowType.RETRO_GAME),
                            Triple("VM Settings", Icons.Default.Settings, WindowType.SETTINGS)
                        )

                        appItems.forEach { (title, icon, type) ->
                            StartMenuItem(
                                title = title,
                                icon = icon,
                                onClick = {
                                    vmEngine.openWindow(type)
                                    isStartMenuOpen = false
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF1E293B))
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Flush RAM Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    vmEngine.memoryOptimizer.optimizeMemory()
                                    isStartMenuOpen = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Trim RAM",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Trim Host RAM",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Exit to Shell Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    isStartMenuOpen = false
                                    onReturnToTerminal()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Exit to Shell",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exit to Terminal (CLI)",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
