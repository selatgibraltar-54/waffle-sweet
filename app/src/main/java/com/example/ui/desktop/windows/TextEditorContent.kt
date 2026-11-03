package com.example.ui.desktop.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vm.VirtualFileSystem

@Composable
fun TextEditorContent(
    filePath: String,
    fileSystem: VirtualFileSystem
) {
    val targetPath = if (filePath.isEmpty()) "/home/user/document.txt" else filePath
    val existingFile = remember(targetPath) { fileSystem.getFile(targetPath) }

    var textContent by remember(targetPath) {
        mutableStateOf(existingFile?.content ?: "")
    }
    var statusMessage by remember { mutableStateOf("Editing: $targetPath") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = targetPath,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    fileSystem.createFile(targetPath, textContent)
                    statusMessage = "Saved successfully at ${System.currentTimeMillis() % 10000}ms"
                },
                modifier = Modifier
                    .height(28.dp)
                    .testTag("save_file_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Save", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Text Area
        OutlinedTextField(
            value = textContent,
            onValueChange = {
                textContent = it
                statusMessage = "Modified (unsaved)"
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("text_editor_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFFE2E8F0),
                unfocusedTextColor = Color(0xFFCBD5E1),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF0A0F1D),
                unfocusedContainerColor = Color(0xFF0A0F1D)
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = statusMessage,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Lines: ${textContent.lines().size} | Chars: ${textContent.length}",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
