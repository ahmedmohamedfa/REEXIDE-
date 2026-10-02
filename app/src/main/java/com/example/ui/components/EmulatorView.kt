package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runtime.IFlutterRuntime
import com.example.runtime.ParsedWidget
import com.example.runtime.RuntimeState
import com.example.runtime.RuntimeStatus
import com.example.service.LocalizationManager
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeviceModel(val displayName: String, val widthDp: Int, val heightDp: Int) {
    PIXEL_8("Pixel 8 (Compact)", 320, 560),
    GALAXY_S24("Galaxy S24 (Standard)", 350, 600),
    TABLET_10("Tablet 10\" (Expanded)", 480, 620)
}

@Composable
fun EmulatorView(
    runtime: IFlutterRuntime,
    editorCode: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by runtime.state.collectAsState()
    val scope = rememberCoroutineScope()

    var isFullscreen by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf(DeviceModel.PIXEL_8) }
    var showDeviceMenu by remember { mutableStateOf(false) }
    var showInspector by remember { mutableStateOf(false) }
    var activeNoteInput by remember { mutableStateOf("") }
    var selectedTabItem by remember { mutableStateOf(0) }

    val reloadFlashAlpha = remember { Animatable(0f) }

    LaunchedEffect(state.isHotReloading) {
        if (state.isHotReloading) {
            reloadFlashAlpha.snapTo(0.6f)
            reloadFlashAlpha.animateTo(0f, animationSpec = tween(350))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("emulator_view")
    ) {
        if (isFullscreen) {
            // FULLSCREEN BORDERLESS MODE (يملا الشاشة بالكامل)
            Box(modifier = Modifier.fillMaxSize()) {
                RenderRuntimeContent(
                    state = state,
                    runtime = runtime,
                    editorCode = editorCode,
                    activeNoteInput = activeNoteInput,
                    onNoteChange = { activeNoteInput = it },
                    selectedTab = selectedTabItem,
                    onTabSelect = { selectedTabItem = it }
                )

                // Translucent Floating Top Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { runtime.hotReload(editorCode) } },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Hot Reload", tint = Color(0xFF58A6FF), modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { scope.launch { runtime.hotRestart() } },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Hot Restart", tint = AccentGreen, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = { isFullscreen = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFF85149), modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            // FRAMED PHONE BEZEL MODE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Top Master Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.status == RuntimeStatus.RUNNING) AccentGreen else AccentRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.status == RuntimeStatus.RUNNING) {
                                LocalizationManager.str("المحاكي (نشط)", "EMULATOR (ONLINE)")
                            } else {
                                "EMULATOR (${state.status.name})"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                        if (state.reloadDurationMs > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡${state.reloadDurationMs}ms",
                                color = Color(0xFF58A6FF),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Fullscreen Toggle
                        IconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.size(34.dp).testTag("emulator_fullscreen_btn")
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        // Hot Reload Button
                        IconButton(
                            onClick = { scope.launch { runtime.hotReload(editorCode) } },
                            enabled = state.status == RuntimeStatus.RUNNING,
                            modifier = Modifier.size(34.dp).testTag("emulator_hot_reload")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = "Hot Reload", tint = Color(0xFF58A6FF), modifier = Modifier.size(20.dp))
                        }

                        // Hot Restart Button
                        IconButton(
                            onClick = { scope.launch { runtime.hotRestart() } },
                            enabled = state.status == RuntimeStatus.RUNNING,
                            modifier = Modifier.size(34.dp).testTag("emulator_hot_restart")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Hot Restart", tint = AccentGreen, modifier = Modifier.size(20.dp))
                        }

                        // Orientation Toggle
                        IconButton(
                            onClick = { isLandscape = !isLandscape },
                            modifier = Modifier.size(34.dp).testTag("emulator_rotate")
                        ) {
                            Icon(
                                if (isLandscape) Icons.Default.CropPortrait else Icons.Default.CropLandscape,
                                contentDescription = "Rotate",
                                tint = Color(0xFFC9D1D9),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Device Model Selector
                        Box {
                            IconButton(
                                onClick = { showDeviceMenu = true },
                                modifier = Modifier.size(34.dp).testTag("emulator_device_selector")
                            ) {
                                Icon(Icons.Default.Devices, contentDescription = "Device Model", tint = Color(0xFFC9D1D9), modifier = Modifier.size(20.dp))
                            }
                            DropdownMenu(
                                expanded = showDeviceMenu,
                                onDismissRequest = { showDeviceMenu = false }
                            ) {
                                DeviceModel.values().forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.displayName) },
                                        leadingIcon = {
                                            if (d == selectedDevice) Icon(Icons.Default.Check, contentDescription = null, tint = CyanPrimary)
                                        },
                                        onClick = {
                                            selectedDevice = d
                                            showDeviceMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Widget Tree Inspector Toggle
                        IconButton(
                            onClick = { showInspector = !showInspector },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = "Widget Inspector",
                                tint = if (showInspector) CyanPrimary else Color(0xFF8B949E),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close Emulator
                        IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close Emulator", tint = Color(0xFF8B949E), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Physical Device Frame Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val phoneWidth = if (isLandscape) selectedDevice.heightDp.dp else selectedDevice.widthDp.dp
                    val phoneHeight = if (isLandscape) selectedDevice.widthDp.dp else selectedDevice.heightDp.dp

                    Box(
                        modifier = Modifier
                            .width(phoneWidth)
                            .height(phoneHeight)
                            .shadow(20.dp, RoundedCornerShape(30.dp))
                            .border(4.dp, Color(0xFF30363D), RoundedCornerShape(30.dp))
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF0F141C))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Android Status Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .background(Color(0xFF161B22))
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                Text(
                                    text = currentTime,
                                    color = Color(0xFFC9D1D9),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                // Camera Punch Hole
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                        .border(1.dp, Color(0xFF30363D), CircleShape)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = Color(0xFFC9D1D9), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFFC9D1D9), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.BatteryFull, contentDescription = null, tint = Color(0xFFC9D1D9), modifier = Modifier.size(14.dp))
                                }
                            }

                            // Virtual Screen Area
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E2E))
                            ) {
                                RenderRuntimeContent(
                                    state = state,
                                    runtime = runtime,
                                    editorCode = editorCode,
                                    activeNoteInput = activeNoteInput,
                                    onNoteChange = { activeNoteInput = it },
                                    selectedTab = selectedTabItem,
                                    onTabSelect = { selectedTabItem = it }
                                )

                                if (reloadFlashAlpha.value > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(CyanPrimary.copy(alpha = reloadFlashAlpha.value))
                                    )
                                }

                                // FPS Telemetry Chip
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "60.0 FPS • 44.8 MB",
                                        color = AccentGreen,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Android Gesture Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(Color(0xFF161B22)),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF8B949E))
                                )
                            }
                        }
                    }
                }

                // Inspector Drawer
                AnimatedVisibility(visible = showInspector) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("FLUTTER WIDGET INSPECTOR", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                IconButton(onClick = { showInspector = false }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF8B949E), modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• MaterialApp [theme: dark, useMaterial3: true]\n  • Scaffold\n    • AppBar [title: '${state.appTitle}']\n    • Center\n      • Column\n        • Text (\$_counter: ${state.stateVariables["_counter"] ?: 0})\n    • FloatingActionButton [tooltip: 'Increment']",
                                color = Color(0xFFC9D1D9),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderRuntimeContent(
    state: RuntimeState,
    runtime: IFlutterRuntime,
    editorCode: String,
    activeNoteInput: String,
    onNoteChange: (String) -> Unit,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    when (state.status) {
        RuntimeStatus.STOPPED -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = LocalizationManager.str("المحاكي متوقف", "Simulator Stopped"),
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { scope.launch { runtime.start(editorCode) } },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LocalizationManager.str("تشغيل تطبيق فلاتر", "Run Flutter App"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        RuntimeStatus.COMPILING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CyanPrimary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LocalizationManager.str("جارٍ معالجة عناصر فلاتر...", "Compiling Flutter widgets..."),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        RuntimeStatus.ERROR -> {
            FlutterErrorScreen(
                error = state.errorMessage ?: "Unknown Flutter Runtime Exception",
                stackTrace = state.errorStackTrace ?: ""
            )
        }
        RuntimeStatus.RUNNING, RuntimeStatus.HOT_RELOADING -> {
            val root = state.rootWidget
            if (root is ParsedWidget.Scaffold) {
                RenderComprehensiveScaffold(
                    scaffold = root,
                    runtimeState = state,
                    onTriggerAction = { runtime.triggerAction(it) },
                    noteInput = activeNoteInput,
                    onNoteInputChange = onNoteChange,
                    selectedTab = selectedTab,
                    onTabSelect = onTabSelect
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Rendering Flutter Widgets...", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RenderComprehensiveScaffold(
    scaffold: ParsedWidget.Scaffold,
    runtimeState: RuntimeState,
    onTriggerAction: (String) -> Unit,
    noteInput: String,
    onNoteInputChange: (String) -> Unit,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // AppBar
            if (scaffold.appBar != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF2E3440))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (scaffold.drawer != null) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = scaffold.appBar.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val body = scaffold.body
                if (body != null) {
                    RenderAnyWidget(
                        widget = body,
                        runtimeState = runtimeState,
                        onTriggerAction = onTriggerAction,
                        noteInput = noteInput,
                        onNoteInputChange = onNoteInputChange
                    )
                }
            }

            // BottomNavigationBar
            if (scaffold.bottomNavigationBar != null) {
                NavigationBar(
                    containerColor = Color(0xFF2E3440),
                    modifier = Modifier.height(56.dp)
                ) {
                    scaffold.bottomNavigationBar.items.forEachIndexed { idx, item ->
                        val isSelected = idx == selectedTab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onTabSelect(idx) },
                            icon = {
                                val icon = when (item.iconName) {
                                    "home" -> Icons.Default.Home
                                    "code" -> Icons.Default.Dashboard
                                    "settings" -> Icons.Default.Settings
                                    else -> Icons.Default.Star
                                }
                                Icon(icon, contentDescription = item.label, tint = if (isSelected) CyanPrimary else Color(0xFF8B949E))
                            },
                            label = {
                                Text(item.label, fontSize = 10.sp, color = if (isSelected) CyanPrimary else Color(0xFF8B949E))
                            }
                        )
                    }
                }
            }
        }

        // FloatingActionButton
        if (scaffold.floatingActionButton != null) {
            FloatingActionButton(
                onClick = { onTriggerAction(scaffold.floatingActionButton.action) },
                containerColor = Color(0xFF58A6FF),
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("emulator_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = scaffold.floatingActionButton.tooltip)
            }
        }
    }
}

@Composable
private fun RenderAnyWidget(
    widget: ParsedWidget,
    runtimeState: RuntimeState,
    onTriggerAction: (String) -> Unit,
    noteInput: String,
    onNoteInputChange: (String) -> Unit
) {
    when (widget) {
        is ParsedWidget.Center -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                RenderAnyWidget(widget.child, runtimeState, onTriggerAction, noteInput, onNoteInputChange)
            }
        }
        is ParsedWidget.Column -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                widget.children.forEach { child ->
                    RenderAnyWidget(child, runtimeState, onTriggerAction, noteInput, onNoteInputChange)
                }
            }
        }
        is ParsedWidget.Row -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                widget.children.forEach { child ->
                    RenderAnyWidget(child, runtimeState, onTriggerAction, noteInput, onNoteInputChange)
                }
            }
        }
        is ParsedWidget.Text -> {
            var text = widget.content
            runtimeState.stateVariables.forEach { (k, v) ->
                text = text.replace("$$k", v.toString())
                text = text.replace("\${$k}", v.toString())
            }

            Text(
                text = text,
                color = if (widget.colorHex != null) Color(android.graphics.Color.parseColor(widget.colorHex)) else Color.White,
                fontSize = widget.fontSize.sp,
                fontWeight = if (widget.isBold) FontWeight.Bold else FontWeight.Normal
            )
        }
        is ParsedWidget.ElevatedButton -> {
            Button(
                onClick = { onTriggerAction(widget.action) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58A6FF)),
                modifier = Modifier.testTag("emulator_btn_${widget.label.lowercase()}")
            ) {
                if (widget.iconName == "add") {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (widget.iconName == "refresh") {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (widget.iconName == "save") {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(widget.label, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        is ParsedWidget.OutlinedButton -> {
            OutlinedButton(
                onClick = { onTriggerAction(widget.action) },
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyanPrimary))
            ) {
                Text(widget.label, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        is ParsedWidget.TextButton -> {
            TextButton(onClick = { onTriggerAction(widget.action) }) {
                Text(widget.label, color = CyanPrimary, fontSize = 13.sp)
            }
        }
        is ParsedWidget.TextField -> {
            OutlinedTextField(
                value = noteInput,
                onValueChange = onNoteInputChange,
                placeholder = { Text(widget.placeholder, color = Color(0xFF8B949E), fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = DarkBorder
                ),
                modifier = Modifier.fillMaxWidth().testTag("emulator_textfield")
            )
        }
        is ParsedWidget.Card -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2E3440),
                modifier = Modifier.fillMaxWidth()
            ) {
                RenderAnyWidget(widget.child, runtimeState, onTriggerAction, noteInput, onNoteInputChange)
            }
        }
        is ParsedWidget.Padding -> {
            Box(modifier = Modifier.padding(widget.all.dp)) {
                RenderAnyWidget(widget.child, runtimeState, onTriggerAction, noteInput, onNoteInputChange)
            }
        }
        is ParsedWidget.SizedBox -> {
            Spacer(modifier = Modifier.width(widget.width.dp).height(widget.height.dp))
        }
        is ParsedWidget.Icon -> {
            val icon = when (widget.name) {
                "star" -> Icons.Default.Star
                "dashboard" -> Icons.Default.Dashboard
                "check_circle" -> Icons.Default.CheckCircle
                "schedule" -> Icons.Default.Schedule
                else -> Icons.Default.Star
            }
            Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(widget.size.dp))
        }
        is ParsedWidget.ListTile -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { widget.action?.let { onTriggerAction(it) } }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (widget.leadingIcon != null) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(widget.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (widget.subtitle != null) {
                        Text(widget.subtitle, color = Color(0xFF8B949E), fontSize = 11.sp)
                    }
                }
                if (widget.trailingIcon != null) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                }
            }
        }
        is ParsedWidget.ListView -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (widget.children.isNotEmpty()) {
                    items(widget.children) { child ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E3440),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            RenderAnyWidget(child, runtimeState, onTriggerAction, noteInput, onNoteInputChange)
                        }
                    }
                } else {
                    items(widget.itemLabels) { label ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E3440),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(text = label, color = Color(0xFFECEFF4), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
        else -> {
            Text("Widget: ${widget::class.simpleName}", color = Color(0xFF8B949E))
        }
    }
}

@Composable
private fun FlutterErrorScreen(error: String, stackTrace: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB00020))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFFFEB3B))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "══╡ EXCEPTION CAUGHT BY FLUTTER ╞══",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = error,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stackTrace,
            color = Color(0xFFFFCDD2),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 14.sp
        )
    }
}
