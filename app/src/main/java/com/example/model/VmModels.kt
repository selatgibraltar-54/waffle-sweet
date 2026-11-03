package com.example.model

enum class VmState {
    CLI_SHELL,
    INSTALLING_GUI,
    GUI_DESKTOP,
    SHUTDOWN
}

enum class WindowType {
    TERMINAL,
    FILE_EXPLORER,
    TEXT_EDITOR,
    SYSTEM_MONITOR,
    CALCULATOR,
    BROWSER,
    RETRO_GAME,
    SETTINGS
}

data class VmWindow(
    val id: String,
    val type: WindowType,
    val title: String,
    val x: Float = 40f,
    val y: Float = 80f,
    val width: Float = 320f,
    val height: Float = 360f,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false,
    val zIndex: Int = 1,
    val extraData: String = ""
)

data class VmProcess(
    val pid: Int,
    val user: String,
    val name: String,
    val cpuPercent: Float,
    val memoryMb: Float,
    val state: String = "S"
)

data class HostMemoryStats(
    val usedMemoryMb: Long,
    val freeMemoryMb: Long,
    val totalMemoryMb: Long,
    val maxMemoryMb: Long,
    val isLowRamModeActive: Boolean = false
)

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LineType {
    INPUT,
    OUTPUT,
    SUCCESS,
    WARNING,
    ERROR,
    SYSTEM
}

data class VirtualFile(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val content: String = "",
    val sizeBytes: Long = 0,
    val modifiedTime: Long = System.currentTimeMillis()
)
