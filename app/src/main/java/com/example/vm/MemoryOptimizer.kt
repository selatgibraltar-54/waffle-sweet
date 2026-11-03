package com.example.vm

import android.content.Context
import com.example.model.HostMemoryStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MemoryOptimizer(private val context: Context) {

    private val prefs = context.getSharedPreferences("nanovm_prefs", Context.MODE_PRIVATE)

    var isLowRamMode: Boolean
        get() = prefs.getBoolean("low_ram_mode", true)
        set(value) {
            prefs.edit().putBoolean("low_ram_mode", value).apply()
            updateStats()
        }

    private val _hostStats = MutableStateFlow(getHostMemoryStats())
    val hostStats: StateFlow<HostMemoryStats> = _hostStats.asStateFlow()

    fun updateStats() {
        _hostStats.value = getHostMemoryStats()
    }

    fun getHostMemoryStats(): HostMemoryStats {
        val runtime = Runtime.getRuntime()
        val total = runtime.totalMemory()
        val free = runtime.freeMemory()
        val max = runtime.maxMemory()
        val used = total - free

        return HostMemoryStats(
            usedMemoryMb = used / (1024 * 1024),
            freeMemoryMb = free / (1024 * 1024),
            totalMemoryMb = total / (1024 * 1024),
            maxMemoryMb = max / (1024 * 1024),
            isLowRamModeActive = isLowRamMode
        )
    }

    fun optimizeMemory(): Long {
        val beforeUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
        System.gc()
        try {
            Thread.sleep(50)
        } catch (_: Exception) {}
        System.runFinalization()
        updateStats()
        val afterUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
        return (beforeUsed - afterUsed).coerceAtLeast(0)
    }
}
