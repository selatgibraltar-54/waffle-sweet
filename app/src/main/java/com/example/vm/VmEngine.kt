package com.example.vm

import android.content.Context
import com.example.model.LineType
import com.example.model.TerminalLine
import com.example.model.VmProcess
import com.example.model.VmState
import com.example.model.VmWindow
import com.example.model.WindowType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VmEngine(private val context: Context) {

    private val prefs = context.getSharedPreferences("nanovm_engine", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main)

    val fileSystem = VirtualFileSystem(context)
    val memoryOptimizer = MemoryOptimizer(context)

    private val _vmState = MutableStateFlow(VmState.CLI_SHELL)
    val vmState: StateFlow<VmState> = _vmState.asStateFlow()

    private val _currentDir = MutableStateFlow("/home/user")
    val currentDir: StateFlow<String> = _currentDir.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory: StateFlow<List<String>> = _commandHistory.asStateFlow()

    private val _isGuiInstalled = MutableStateFlow(prefs.getBoolean("gui_system_comp_installed", false))
    val isGuiInstalled: StateFlow<Boolean> = _isGuiInstalled.asStateFlow()

    // Configurable simulated VM RAM: 256MB, 512MB, 1024MB
    private val _simulatedRamMb = MutableStateFlow(prefs.getInt("simulated_ram_mb", 512))
    val simulatedRamMb: StateFlow<Int> = _simulatedRamMb.asStateFlow()

    // Active GUI Windows
    private val _windows = MutableStateFlow<List<VmWindow>>(emptyList())
    val windows: StateFlow<List<VmWindow>> = _windows.asStateFlow()

    private val _activeWindowId = MutableStateFlow<String?>(null)
    val activeWindowId: StateFlow<String?> = _activeWindowId.asStateFlow()

    // Boot timestamp
    val bootTimeMs = System.currentTimeMillis()

    init {
        // Initial terminal welcome message
        addTerminalLine("NanoVM Micro-Kernel v2.4-lts (aarch64) booting...", LineType.SYSTEM)
        addTerminalLine("Detected Low-RAM Hardware configuration - optimizing buffer caches.", LineType.SYSTEM)
        addTerminalLine("Virtual RAM: ${_simulatedRamMb.value} MB | Swap: 128 MB | Host Heap: ~${memoryOptimizer.getHostMemoryStats().usedMemoryMb} MB", LineType.SYSTEM)
        addTerminalLine("Type 'help' for available commands.", LineType.OUTPUT)
        if (!_isGuiInstalled.value) {
            addTerminalLine("Notice: Desktop GUI subsystem is not installed.", LineType.WARNING)
            addTerminalLine("To install GUI, type: /install --gui_system_comp", LineType.WARNING)
        } else {
            addTerminalLine("Desktop GUI subsystem installed. Type '/run gui_system' to launch.", LineType.SUCCESS)
        }
    }

    fun setSimulatedRam(ramMb: Int) {
        _simulatedRamMb.value = ramMb
        prefs.edit().putInt("simulated_ram_mb", ramMb).apply()
    }

    private fun addTerminalLine(text: String, type: LineType = LineType.OUTPUT) {
        val current = _terminalLines.value.toMutableList()
        current.add(TerminalLine(text = text, type = type))
        // Keep terminal memory bounded to prevent memory leaks on low-end devices
        if (current.size > 250) {
            current.removeAt(0)
        }
        _terminalLines.value = current
    }

    fun executeCommand(rawCommand: String) {
        val cmd = rawCommand.trim()
        if (cmd.isEmpty()) return

        // Record history
        val history = _commandHistory.value.toMutableList()
        if (history.lastOrNull() != cmd) {
            history.add(cmd)
            if (history.size > 50) history.removeAt(0)
            _commandHistory.value = history
        }

        // Show prompt input line
        addTerminalLine("guest@nanovm:${_currentDir.value}$ $cmd", LineType.INPUT)

        // Command dispatcher
        when {
            cmd == "/install --gui_system_comp" -> runGuiInstallation()
            cmd == "/run gui_system" -> launchGuiSystem()
            cmd.startsWith("/install") -> {
                addTerminalLine("Usage: /install --gui_system_comp", LineType.ERROR)
            }
            cmd.startsWith("/run") -> {
                addTerminalLine("Usage: /run gui_system", LineType.ERROR)
            }
            cmd == "help" || cmd == "?" -> showHelp()
            cmd == "neofetch" -> showNeofetch()
            cmd == "free" || cmd.startsWith("free ") -> showFreeMemory()
            cmd == "top" -> showTopProcesses()
            cmd == "memopt" || cmd == "optimize" -> runMemoryOptimization()
            cmd == "clear" -> _terminalLines.value = emptyList()
            cmd == "uname -a" || cmd == "uname" -> {
                addTerminalLine("Linux nanovm 5.15.0-nano-lowram #1 SMP PREEMPT aarch64 GNU/Linux", LineType.OUTPUT)
            }
            cmd == "uptime" -> {
                val uptimeSeconds = (System.currentTimeMillis() - bootTimeMs) / 1000
                val mins = uptimeSeconds / 60
                val secs = uptimeSeconds % 60
                addTerminalLine("up $mins min, $secs sec, load average: 0.12, 0.08, 0.03", LineType.OUTPUT)
            }
            cmd == "whoami" -> addTerminalLine("guest (uid=1000, gid=1000)", LineType.OUTPUT)
            cmd.startsWith("ls") -> runLs(cmd)
            cmd.startsWith("cd") -> runCd(cmd)
            cmd.startsWith("cat ") -> runCat(cmd.removePrefix("cat ").trim())
            cmd.startsWith("touch ") -> runTouch(cmd.removePrefix("touch ").trim())
            cmd.startsWith("rm ") -> runRm(cmd.removePrefix("rm ").trim())
            cmd.startsWith("echo ") -> runEcho(cmd.removePrefix("echo ").trim())
            cmd.startsWith("nano ") -> runNano(cmd.removePrefix("nano ").trim())
            cmd.startsWith("mkdir ") -> runMkdir(cmd.removePrefix("mkdir ").trim())
            cmd == "reboot" -> reboot()
            cmd == "exit" || cmd == "poweroff" -> {
                addTerminalLine("System halting. Session kept in suspended state.", LineType.SYSTEM)
            }
            cmd == "gui" -> {
                if (_isGuiInstalled.value) {
                    addTerminalLine("To open the Light OS GUI, run: /run gui_system", LineType.OUTPUT)
                } else {
                    addTerminalLine("GUI is not installed yet. Please run: /install --gui_system_comp", LineType.WARNING)
                }
            }
            else -> {
                addTerminalLine("nanosh: command not found: '$cmd'. Type 'help' for command list.", LineType.ERROR)
            }
        }
    }

    private fun runGuiInstallation() {
        if (_isGuiInstalled.value) {
            addTerminalLine("Package 'gui_system_comp' is already installed and up to date.", LineType.WARNING)
            addTerminalLine("You can launch the GUI by typing:", LineType.OUTPUT)
            addTerminalLine("  /run gui_system", LineType.SUCCESS)
            return
        }

        _vmState.value = VmState.INSTALLING_GUI
        addTerminalLine("[NanoPkg] Reading package lists... Done", LineType.SYSTEM)
        addTerminalLine("[NanoPkg] Resolving dependencies for: gui_system_comp (Light Desktop Suite)", LineType.SYSTEM)

        scope.launch {
            delay(400)
            addTerminalLine("-> Verifying low-host-RAM profile (<45MB JVM target)... [OK]", LineType.OUTPUT)
            delay(500)
            addTerminalLine("-> Unpacking xnano-compositor (DoubleBuffer window manager)... [OK]", LineType.OUTPUT)
            delay(500)
            addTerminalLine("-> Installing light-desktop apps (FileMgr, Editor, SysMon, Calc)... [OK]", LineType.OUTPUT)
            delay(600)
            addTerminalLine("-> Generating responsive touch-control bindings and assets... [OK]", LineType.OUTPUT)
            delay(400)
            addTerminalLine("Setting up system hooks for /usr/bin/gui_system...", LineType.OUTPUT)

            // Mark as installed
            _isGuiInstalled.value = true
            prefs.edit().putBoolean("gui_system_comp_installed", true).apply()

            // Create executable in /usr/bin/
            fileSystem.createFile(
                "/usr/bin/gui_system",
                """#!/bin/nanosh
# NanoOS GUI Subsystem launcher
exec xnano-compositor --low-ram
"""
            )

            delay(300)
            addTerminalLine("=====================================================", LineType.SUCCESS)
            addTerminalLine("Package 'gui_system_comp' installed successfully!", LineType.SUCCESS)
            addTerminalLine("Now you can open the Light OS GUI with the command:", LineType.SUCCESS)
            addTerminalLine("  /run gui_system", LineType.SUCCESS)
            addTerminalLine("=====================================================", LineType.SUCCESS)

            _vmState.value = VmState.CLI_SHELL
        }
    }

    fun launchGuiSystem() {
        if (!_isGuiInstalled.value) {
            addTerminalLine("[FATAL] GUI subsystem is not installed.", LineType.ERROR)
            addTerminalLine("Please install it first using:", LineType.WARNING)
            addTerminalLine("  /install --gui_system_comp", LineType.WARNING)
            return
        }

        addTerminalLine("Starting X-Nano Compositor & Light OS Window Manager...", LineType.SYSTEM)
        addTerminalLine("Initializing 60FPS double-buffer surface with zero-jank gestures...", LineType.SYSTEM)

        scope.launch {
            delay(350)
            _vmState.value = VmState.GUI_DESKTOP
            // If no windows open, open Welcome / System Monitor and File Manager by default
            if (_windows.value.isEmpty()) {
                openWindow(WindowType.SYSTEM_MONITOR)
                openWindow(WindowType.FILE_EXPLORER)
            }
        }
    }

    fun exitGuiToTerminal() {
        _vmState.value = VmState.CLI_SHELL
        addTerminalLine("Returned to NanoVM console session (tty1).", LineType.SYSTEM)
    }

    private fun showHelp() {
        addTerminalLine("NanoVM Micro-Shell Available Commands:", LineType.SYSTEM)
        addTerminalLine("  /install --gui_system_comp : Install Light OS Graphical Environment", LineType.SUCCESS)
        addTerminalLine("  /run gui_system            : Launch the Light OS GUI desktop", LineType.SUCCESS)
        addTerminalLine("  help, ?                    : Display this command reference", LineType.OUTPUT)
        addTerminalLine("  neofetch                   : Display VM hardware & OS info", LineType.OUTPUT)
        addTerminalLine("  free, free -m              : Show VM memory & real Host RAM footprint", LineType.OUTPUT)
        addTerminalLine("  top                        : Display active system processes", LineType.OUTPUT)
        addTerminalLine("  memopt                     : Run host GC and optimize RAM allocation", LineType.OUTPUT)
        addTerminalLine("  ls [path]                  : List contents of directory", LineType.OUTPUT)
        addTerminalLine("  cd [dir]                   : Change current directory", LineType.OUTPUT)
        addTerminalLine("  cat [file]                 : Print file contents to terminal", LineType.OUTPUT)
        addTerminalLine("  echo [text] > [file]       : Write text into file", LineType.OUTPUT)
        addTerminalLine("  touch [file]               : Create an empty file", LineType.OUTPUT)
        addTerminalLine("  nano [file]                : Open or create file in editor", LineType.OUTPUT)
        addTerminalLine("  rm [file]                  : Remove a file", LineType.OUTPUT)
        addTerminalLine("  mkdir [dir]                : Create a directory", LineType.OUTPUT)
        addTerminalLine("  uname -a                   : Display kernel and architecture", LineType.OUTPUT)
        addTerminalLine("  uptime                     : System running time", LineType.OUTPUT)
        addTerminalLine("  clear                      : Clear terminal screen", LineType.OUTPUT)
        addTerminalLine("  reboot                     : Restart virtual machine", LineType.OUTPUT)
    }

    private fun showNeofetch() {
        val hostStats = memoryOptimizer.getHostMemoryStats()
        val ascii = """
     /\_/\      OS: NanoOS 2.4 LTS (MicroCore)
    ( o.o )     Host: Android Virtual Container
     > ^ <      Kernel: 5.15.0-nano-lowram
    NanoVM      Uptime: ${(System.currentTimeMillis() - bootTimeMs) / 1000} seconds
                Shell: nanosh 1.2
                GUI Suite: ${if (_isGuiInstalled.value) "xnano-desktop (Active)" else "Not Installed"}
                Simulated RAM: 86MB / ${_simulatedRamMb.value}MB
                Host Real JVM RAM: ${hostStats.usedMemoryMb}MB / ${hostStats.totalMemoryMb}MB (Optimized)
                Low-RAM Engine: ${if (hostStats.isLowRamModeActive) "Enabled (High Speed)" else "Standard"}
                Architecture: aarch64 (virtualized)
        """.trimIndent()
        ascii.lines().forEach { addTerminalLine(it, LineType.OUTPUT) }
    }

    private fun showFreeMemory() {
        val hostStats = memoryOptimizer.getHostMemoryStats()
        addTerminalLine("               total        used        free      shared  buff/cache   available", LineType.SYSTEM)
        val vmUsed = (_simulatedRamMb.value * 0.18f).toInt()
        val vmFree = _simulatedRamMb.value - vmUsed - 24
        addTerminalLine("Mem (VM):      ${_simulatedRamMb.value}M         ${vmUsed}M        ${vmFree}M          4M         24M        ${vmFree + 20}M", LineType.OUTPUT)
        addTerminalLine("Swap (VM):      128M          0M        128M", LineType.OUTPUT)
        addTerminalLine("----------------------------------------------------------------", LineType.SYSTEM)
        addTerminalLine("Host Android JVM Memory (Actual Hardware Footprint):", LineType.SYSTEM)
        addTerminalLine("  Host Used:     ${hostStats.usedMemoryMb} MB (Extremely low - safe for 1GB/2GB devices)", LineType.SUCCESS)
        addTerminalLine("  Host Free:     ${hostStats.freeMemoryMb} MB", LineType.OUTPUT)
        addTerminalLine("  Host Max Heap: ${hostStats.maxMemoryMb} MB", LineType.OUTPUT)
    }

    private fun showTopProcesses() {
        val processes = getProcessList()
        addTerminalLine("Tasks: ${processes.size} total, 1 running, ${processes.size - 1} sleeping", LineType.SYSTEM)
        addTerminalLine("%Cpu(s):  2.4 us,  1.1 sy,  0.0 ni, 96.5 id,  0.0 wa", LineType.SYSTEM)
        addTerminalLine("PID   USER      PR  NI  VIRT   RES   %CPU  %MEM   TIME+ COMMAND", LineType.SYSTEM)
        processes.forEach { p ->
            val formatted = String.format(
                "%-5d %-9s 20   0  %4.0fM %4.0fM   %4.1f  %4.1f   0:01 %s",
                p.pid, p.user, p.memoryMb * 2, p.memoryMb, p.cpuPercent, p.memoryMb / 5.12f, p.name
            )
            addTerminalLine(formatted, LineType.OUTPUT)
        }
    }

    fun getProcessList(): List<VmProcess> {
        val list = mutableListOf(
            VmProcess(1, "root", "systemd-nano", 0.1f, 4.2f),
            VmProcess(88, "root", "klogd", 0.0f, 1.8f),
            VmProcess(104, "guest", "nanosh", 0.3f, 3.4f),
            VmProcess(120, "root", "mem-guard", 0.2f, 2.1f)
        )
        if (_isGuiInstalled.value) {
            list.add(VmProcess(210, "guest", "xnano-wm", 1.8f, 14.5f))
            list.add(VmProcess(215, "guest", "desktop-panel", 0.6f, 6.2f))
        }
        _windows.value.filter { !it.isMinimized }.forEachIndexed { idx, win ->
            list.add(VmProcess(300 + idx, "guest", win.type.name.lowercase(), 0.5f, 5.0f))
        }
        return list
    }

    private fun runMemoryOptimization() {
        addTerminalLine("[MemOpt] Triggering low-overhead garbage collector...", LineType.SYSTEM)
        val freed = memoryOptimizer.optimizeMemory()
        val stats = memoryOptimizer.getHostMemoryStats()
        addTerminalLine("Memory trimmed! Freed approx ${freed}MB. Current host JVM usage: ${stats.usedMemoryMb}MB.", LineType.SUCCESS)
        addTerminalLine("Host hardware footprint is kept lean and highly optimized.", LineType.SUCCESS)
    }

    private fun runLs(cmd: String) {
        val target = if (cmd.length > 3) cmd.substring(2).trim() else _currentDir.value
        val norm = fileSystem.normalizePath(target, _currentDir.value)
        val files = fileSystem.listDirectory(norm)
        if (files.isEmpty()) {
            addTerminalLine("(directory is empty)", LineType.OUTPUT)
            return
        }
        val output = files.joinToString("   ") { file ->
            if (file.isDirectory) "${file.name}/" else file.name
        }
        addTerminalLine(output, LineType.OUTPUT)
    }

    private fun runCd(cmd: String) {
        val target = if (cmd.length > 3) cmd.substring(2).trim() else "/home/user"
        val norm = fileSystem.normalizePath(target, _currentDir.value)
        val file = fileSystem.getFile(norm)
        if (file != null && file.isDirectory) {
            _currentDir.value = norm
        } else {
            addTerminalLine("cd: no such file or directory: $target", LineType.ERROR)
        }
    }

    private fun runCat(filename: String) {
        if (filename.isEmpty()) {
            addTerminalLine("cat: missing operand", LineType.ERROR)
            return
        }
        val norm = fileSystem.normalizePath(filename, _currentDir.value)
        val file = fileSystem.getFile(norm)
        if (file == null) {
            addTerminalLine("cat: $filename: No such file or directory", LineType.ERROR)
        } else if (file.isDirectory) {
            addTerminalLine("cat: $filename: Is a directory", LineType.ERROR)
        } else {
            file.content.lines().forEach { addTerminalLine(it, LineType.OUTPUT) }
        }
    }

    private fun runTouch(filename: String) {
        if (filename.isEmpty()) {
            addTerminalLine("touch: missing file operand", LineType.ERROR)
            return
        }
        val norm = fileSystem.normalizePath(filename, _currentDir.value)
        fileSystem.createFile(norm, "")
        addTerminalLine("Created empty file '$filename'", LineType.SUCCESS)
    }

    private fun runRm(filename: String) {
        if (filename.isEmpty()) {
            addTerminalLine("rm: missing operand", LineType.ERROR)
            return
        }
        val norm = fileSystem.normalizePath(filename, _currentDir.value)
        if (fileSystem.deleteFile(norm)) {
            addTerminalLine("Removed '$filename'", LineType.SUCCESS)
        } else {
            addTerminalLine("rm: cannot remove '$filename': No such file or permission denied", LineType.ERROR)
        }
    }

    private fun runEcho(textAndDest: String) {
        if (">" in textAndDest) {
            val parts = textAndDest.split(">", limit = 2)
            val content = parts[0].trim().removeSurrounding("\"").removeSurrounding("'")
            val dest = parts[1].trim()
            val norm = fileSystem.normalizePath(dest, _currentDir.value)
            fileSystem.createFile(norm, content)
            addTerminalLine("Wrote to '$dest'", LineType.SUCCESS)
        } else {
            addTerminalLine(textAndDest.removeSurrounding("\"").removeSurrounding("'"), LineType.OUTPUT)
        }
    }

    private fun runNano(filename: String) {
        if (filename.isEmpty()) {
            addTerminalLine("nano: missing filename", LineType.ERROR)
            return
        }
        val norm = fileSystem.normalizePath(filename, _currentDir.value)
        var file = fileSystem.getFile(norm)
        if (file == null) {
            fileSystem.createFile(norm, "")
            file = fileSystem.getFile(norm)
        }
        if (_isGuiInstalled.value) {
            openWindow(WindowType.TEXT_EDITOR, extraData = norm)
            _vmState.value = VmState.GUI_DESKTOP
        } else {
            addTerminalLine("--- Editing: $norm (CLI Mode) ---", LineType.SYSTEM)
            addTerminalLine(file?.content ?: "", LineType.OUTPUT)
            addTerminalLine("[Notice: Install GUI with '/install --gui_system_comp' for full visual editor]", LineType.WARNING)
        }
    }

    private fun runMkdir(dirname: String) {
        if (dirname.isEmpty()) {
            addTerminalLine("mkdir: missing operand", LineType.ERROR)
            return
        }
        val norm = fileSystem.normalizePath(dirname, _currentDir.value)
        fileSystem.createDirectory(norm)
        addTerminalLine("Created directory '$dirname'", LineType.SUCCESS)
    }

    private fun reboot() {
        _terminalLines.value = emptyList()
        _windows.value = emptyList()
        _activeWindowId.value = null
        _vmState.value = VmState.CLI_SHELL
        addTerminalLine("NanoVM Rebooting...", LineType.SYSTEM)
        addTerminalLine("Restarting micro-kernel and memory buffers...", LineType.SYSTEM)
        memoryOptimizer.optimizeMemory()
        addTerminalLine("System ready. Type 'help' or '/run gui_system'.", LineType.OUTPUT)
    }

    // Window Management for Light OS GUI
    fun openWindow(type: WindowType, extraData: String = "") {
        val existing = _windows.value.find { it.type == type && (extraData.isEmpty() || it.extraData == extraData) }
        if (existing != null) {
            // Restore and bring to front
            bringWindowToFront(existing.id)
            if (existing.isMinimized) {
                toggleMinimizeWindow(existing.id)
            }
            return
        }

        val title = when (type) {
            WindowType.TERMINAL -> "Terminal Shell"
            WindowType.FILE_EXPLORER -> "File Explorer"
            WindowType.TEXT_EDITOR -> "Text Editor"
            WindowType.SYSTEM_MONITOR -> "Task Manager & MemOpt"
            WindowType.CALCULATOR -> "Calculator"
            WindowType.BROWSER -> "Nano Browser"
            WindowType.RETRO_GAME -> "Snake Retro"
            WindowType.SETTINGS -> "VM Settings"
        }

        val windowCount = _windows.value.size
        val cascadeOffset = (windowCount % 5) * 24f

        val newWindow = VmWindow(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            x = 24f + cascadeOffset,
            y = 56f + cascadeOffset,
            width = when (type) {
                WindowType.SYSTEM_MONITOR -> 340f
                WindowType.FILE_EXPLORER -> 330f
                WindowType.TEXT_EDITOR -> 330f
                WindowType.CALCULATOR -> 280f
                WindowType.RETRO_GAME -> 300f
                else -> 320f
            },
            height = when (type) {
                WindowType.CALCULATOR -> 380f
                WindowType.RETRO_GAME -> 390f
                WindowType.SYSTEM_MONITOR -> 420f
                else -> 380f
            },
            zIndex = (_windows.value.maxOfOrNull { it.zIndex } ?: 0) + 1,
            extraData = extraData
        )

        _windows.value = _windows.value + newWindow
        _activeWindowId.value = newWindow.id
    }

    fun closeWindow(id: String) {
        _windows.value = _windows.value.filter { it.id != id }
        if (_activeWindowId.value == id) {
            _activeWindowId.value = _windows.value.maxByOrNull { it.zIndex }?.id
        }
    }

    fun toggleMinimizeWindow(id: String) {
        _windows.value = _windows.value.map { win ->
            if (win.id == id) {
                win.copy(isMinimized = !win.isMinimized)
            } else win
        }
        val target = _windows.value.find { it.id == id }
        if (target != null && !target.isMinimized) {
            bringWindowToFront(id)
        }
    }

    fun toggleMaximizeWindow(id: String) {
        _windows.value = _windows.value.map { win ->
            if (win.id == id) {
                win.copy(isMaximized = !win.isMaximized)
            } else win
        }
        bringWindowToFront(id)
    }

    fun updateWindowPosition(id: String, newX: Float, newY: Float) {
        _windows.value = _windows.value.map { win ->
            if (win.id == id) {
                win.copy(x = newX.coerceAtLeast(0f), y = newY.coerceAtLeast(40f))
            } else win
        }
    }

    fun bringWindowToFront(id: String) {
        val maxZ = (_windows.value.maxOfOrNull { it.zIndex } ?: 0) + 1
        _windows.value = _windows.value.map { win ->
            if (win.id == id) win.copy(zIndex = maxZ, isMinimized = false) else win
        }
        _activeWindowId.value = id
    }
}
