package com.example.vm

import android.content.Context
import com.example.model.VirtualFile
import org.json.JSONArray
import org.json.JSONObject

class VirtualFileSystem(context: Context) {
    private val prefs = context.getSharedPreferences("nanovm_fs", Context.MODE_PRIVATE)
    private val filesMap = mutableMapOf<String, VirtualFile>()

    init {
        loadFs()
        if (filesMap.isEmpty()) {
            initDefaultFiles()
            saveFs()
        }
    }

    private fun initDefaultFiles() {
        // Directories
        createDirectory("/")
        createDirectory("/bin")
        createDirectory("/etc")
        createDirectory("/home")
        createDirectory("/home/user")
        createDirectory("/usr")
        createDirectory("/usr/bin")
        createDirectory("/var")
        createDirectory("/var/log")
        createDirectory("/tmp")

        // Files
        createFile(
            "/home/user/welcome.txt",
            """=====================================================
Welcome to NanoVM Light OS Environment!
=====================================================
A hyper-optimized, low-overhead virtual environment
running on high efficiency Android runtime.

Quick Commands:
  • /install --gui_system_comp  : Install the Light OS GUI
  • /run gui_system             : Launch the graphical desktop
  • help                        : List all shell commands
  • neofetch                    : Show VM system specifications
  • free -m                     : View host & virtual memory
  • top                         : View running processes
  • memopt                      : Run host memory garbage collection
====================================================="""
        )

        createFile(
            "/home/user/notes.txt",
            """Project Ideas:
1. Write a custom shell script in /home/user/
2. Monitor real host RAM usage (kept under 45 MB!)
3. Run GUI desktop multitasking with draggable windows
4. Test performance in Ultra Low-RAM mode"""
        )

        createFile(
            "/home/user/hello.sh",
            """#!/bin/nanosh
echo "Hello from NanoVM virtualized kernel!"
echo "System ready and responsive."
"""
        )

        createFile(
            "/etc/os-release",
            """NAME="NanoOS Light"
VERSION="2.4 LTS (MicroCore)"
ID=nanoos
PRETTY_NAME="NanoOS Light Micro-Kernel Edition"
ARCHITECTURE="aarch64-virtualized"
KERNEL="Linux 5.15-nano-lowram"
RAM_FOOTPRINT="Optimized (<45MB JVM)"
"""
        )

        createFile(
            "/etc/nanovm.conf",
            """# NanoVM Configuration
gui_enabled=auto
render_buffer=double_buffered
power_profile=low_ram_balanced
max_window_fps=60
host_ram_throttle=aggressive
"""
        )

        createFile(
            "/var/log/syslog",
            """[0.000000] Linux version 5.15.0-nano-lowram (gcc 11.2)
[0.001200] Virtual CPU: 2 Cores allocated, 1200MHz throttle
[0.002400] Memory: 512MB Virtual RAM, 128MB Swap initialized
[0.004100] Host RAM Guard: MemoryOptimizer active (<50MB host target)
[0.008900] Initializing rootfs (virtual in-memory storage)
[0.015000] Virtual File System mounted successfully on /
[0.021000] Starting NanoVM shell daemon (PID 1)
[0.025000] System ready for user input.
"""
        )
    }

    fun listDirectory(dirPath: String): List<VirtualFile> {
        val normalized = normalizePath(dirPath)
        return filesMap.values.filter { file ->
            if (file.path == normalized) return@filter false
            val parent = getParentPath(file.path)
            parent == normalized
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
    }

    fun getFile(path: String): VirtualFile? {
        return filesMap[normalizePath(path)]
    }

    fun createFile(path: String, content: String): Boolean {
        val norm = normalizePath(path)
        val name = norm.substringAfterLast("/")
        val parent = getParentPath(norm)
        if (parent != "/" && !filesMap.containsKey(parent)) {
            createDirectory(parent)
        }
        val file = VirtualFile(
            path = norm,
            name = if (name.isEmpty()) "file" else name,
            isDirectory = false,
            content = content,
            sizeBytes = content.toByteArray().size.toLong(),
            modifiedTime = System.currentTimeMillis()
        )
        filesMap[norm] = file
        saveFs()
        return true
    }

    fun createDirectory(path: String): Boolean {
        val norm = normalizePath(path)
        if (filesMap.containsKey(norm)) return true
        val name = norm.substringAfterLast("/")
        val parent = getParentPath(norm)
        if (norm != "/" && parent != "/" && !filesMap.containsKey(parent)) {
            createDirectory(parent)
        }
        val dir = VirtualFile(
            path = norm,
            name = if (name.isEmpty()) "/" else name,
            isDirectory = true,
            sizeBytes = 4096,
            modifiedTime = System.currentTimeMillis()
        )
        filesMap[norm] = dir
        saveFs()
        return true
    }

    fun deleteFile(path: String): Boolean {
        val norm = normalizePath(path)
        if (norm == "/" || norm == "/home" || norm == "/bin") return false
        val removed = filesMap.remove(norm) != null
        // Also remove any child files if directory
        val toRemove = filesMap.keys.filter { it.startsWith("$norm/") }
        toRemove.forEach { filesMap.remove(it) }
        saveFs()
        return removed
    }

    fun normalizePath(path: String, currentDir: String = "/home/user"): String {
        var clean = path.trim()
        if (clean == "~") return "/home/user"
        if (clean.startsWith("~/")) {
            clean = "/home/user/" + clean.removePrefix("~/")
        }
        if (!clean.startsWith("/")) {
            clean = "$currentDir/$clean"
        }
        val parts = clean.split("/").filter { it.isNotEmpty() && it != "." }
        val resolved = mutableListOf<String>()
        for (part in parts) {
            if (part == "..") {
                if (resolved.isNotEmpty()) resolved.removeAt(resolved.size - 1)
            } else {
                resolved.add(part)
            }
        }
        return "/" + resolved.joinToString("/")
    }

    fun getParentPath(path: String): String {
        val norm = normalizePath(path)
        if (norm == "/") return "/"
        val lastSlash = norm.lastIndexOf('/')
        return if (lastSlash <= 0) "/" else norm.substring(0, lastSlash)
    }

    private fun saveFs() {
        val array = JSONArray()
        filesMap.values.forEach { file ->
            val obj = JSONObject()
            obj.put("path", file.path)
            obj.put("name", file.name)
            obj.put("isDir", file.isDirectory)
            obj.put("content", file.content)
            obj.put("size", file.sizeBytes)
            obj.put("time", file.modifiedTime)
            array.put(obj)
        }
        prefs.edit().putString("fs_data", array.toString()).apply()
    }

    private fun loadFs() {
        val data = prefs.getString("fs_data", null) ?: return
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val path = obj.getString("path")
                filesMap[path] = VirtualFile(
                    path = path,
                    name = obj.getString("name"),
                    isDirectory = obj.getBoolean("isDir"),
                    content = obj.optString("content", ""),
                    sizeBytes = obj.optLong("size", 0),
                    modifiedTime = obj.optLong("time", System.currentTimeMillis())
                )
            }
        } catch (_: Exception) {}
    }

    fun resetToDefaults() {
        filesMap.clear()
        initDefaultFiles()
        saveFs()
    }
}
