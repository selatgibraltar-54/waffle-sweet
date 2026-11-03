package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.model.VmState
import com.example.ui.desktop.DesktopScreen
import com.example.ui.terminal.TerminalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.vm.VmEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val vmEngine = remember { VmEngine(applicationContext) }
                val vmState by vmEngine.vmState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090D16)
                ) {
                    Crossfade(
                        targetState = vmState,
                        label = "VmStateTransition"
                    ) { state ->
                        when (state) {
                            VmState.GUI_DESKTOP -> {
                                DesktopScreen(
                                    vmEngine = vmEngine,
                                    onReturnToTerminal = {
                                        vmEngine.exitGuiToTerminal()
                                    }
                                )
                            }
                            else -> {
                                TerminalScreen(
                                    vmEngine = vmEngine,
                                    onLaunchGui = {
                                        vmEngine.launchGuiSystem()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
