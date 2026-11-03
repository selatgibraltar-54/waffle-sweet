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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorContent() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var shouldClearOnNextDigit by remember { mutableStateOf(false) }

    fun onDigit(d: String) {
        if (display == "0" || shouldClearOnNextDigit) {
            display = d
            shouldClearOnNextDigit = false
        } else {
            if (display.length < 10) display += d
        }
    }

    fun onOp(op: String) {
        val current = display.toDoubleOrNull() ?: 0.0
        if (operand1 == null) {
            operand1 = current
        } else if (pendingOp != null) {
            val res = when (pendingOp) {
                "+" -> operand1!! + current
                "-" -> operand1!! - current
                "×" -> operand1!! * current
                "÷" -> if (current != 0.0) operand1!! / current else 0.0
                else -> current
            }
            operand1 = res
            display = if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
        }
        pendingOp = op
        shouldClearOnNextDigit = true
    }

    fun onEquals() {
        val current = display.toDoubleOrNull() ?: 0.0
        if (operand1 != null && pendingOp != null) {
            val res = when (pendingOp) {
                "+" -> operand1!! + current
                "-" -> operand1!! - current
                "×" -> operand1!! * current
                "÷" -> if (current != 0.0) operand1!! / current else 0.0
                else -> current
            }
            display = if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
            operand1 = null
            pendingOp = null
            shouldClearOnNextDigit = true
        }
    }

    fun onClear() {
        display = "0"
        operand1 = null
        pendingOp = null
        shouldClearOnNextDigit = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = display,
                color = Color(0xFF00E5FF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val buttons = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { btn ->
                    val isOp = btn in listOf("÷", "×", "-", "+", "=")
                    val isSpecial = btn in listOf("C", "±", "%")
                    val weight = if (btn == "0") 2f else 1f

                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    btn == "=" -> Color(0xFF00E5FF)
                                    isOp -> Color(0xFF334155)
                                    isSpecial -> Color(0xFF475569)
                                    else -> Color(0xFF1E293B)
                                }
                            )
                            .clickable {
                                when (btn) {
                                    "C" -> onClear()
                                    "±" -> display = if (display.startsWith("-")) display.removePrefix("-") else "-$display"
                                    "%" -> display = ((display.toDoubleOrNull() ?: 0.0) / 100.0).toString()
                                    "÷", "×", "-", "+" -> onOp(btn)
                                    "=" -> onEquals()
                                    "." -> if (!display.contains(".")) display += "."
                                    else -> onDigit(btn)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = btn,
                            color = if (btn == "=") Color(0xFF0A0F1D) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
