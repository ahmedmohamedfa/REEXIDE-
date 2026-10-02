package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.TerminalLine
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun TerminalView(
    outputLines: List<TerminalLine>,
    onSendCommand: (String) -> Unit,
    onClear: () -> Unit,
    onKill: () -> Unit,
    isRunning: Boolean = false,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(outputLines.size) {
        if (outputLines.isNotEmpty()) {
            listState.animateScrollToItem(outputLines.size - 1)
        }
    }

    val quickCommands = listOf(
        "reex doctor",
        "flutter pub get --offline",
        "flutter analyze",
        "flutter test",
        "flutter build apk",
        "ls -la",
        "uname -a"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(6.dp)
            .testTag("terminal_view")
    ) {
        // Terminal Header Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isRunning) AccentGreen else Color(0xFF8B949E))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "TERMINAL (RUNNING...)" else "TERMINAL (READY)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF8B949E)
                )
            }

            Row {
                if (isRunning) {
                    IconButton(onClick = onKill, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Process", tint = AccentRed, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ClearAll, contentDescription = "Clear Terminal", tint = Color(0xFFC9D1D9), modifier = Modifier.size(18.dp))
                }
            }
        }

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp)
        ) {
            quickCommands.forEach { cmd ->
                AssistChip(
                    onClick = { onSendCommand(cmd) },
                    label = {
                        Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyanPrimary)
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = DarkSurfaceVariant),
                    border = AssistChipDefaults.assistChipBorder(borderColor = DarkBorder, enabled = true),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        // Output Console
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurface)
                .padding(8.dp)
        ) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(outputLines) { line ->
                    Text(
                        text = line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = when {
                            line.isError -> AccentRed
                            line.isCommand -> CyanPrimary
                            line.text.startsWith("[✓]") -> AccentGreen
                            line.text.startsWith("[✗]") -> AccentRed
                            else -> Color(0xFFC9D1D9)
                        },
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Command Prompt Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = AccentGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = {
                    Text("Type command...", color = Color(0xFF8B949E), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (commandInput.isNotBlank()) {
                        onSendCommand(commandInput)
                        commandInput = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("terminal_input_field")
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        onSendCommand(commandInput)
                        commandInput = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Run",
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
