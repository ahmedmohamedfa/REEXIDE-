package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.runtime.FlutterRuntimeManager
import com.example.runtime.IFlutterRuntime
import com.example.service.AiProjectAgentService
import com.example.service.LocalizationManager
import com.example.ui.components.AiCopilotDialog
import com.example.ui.components.EmulatorView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Diagnostic
import com.example.model.DiagnosticSeverity
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.service.BuildManagerService
import com.example.service.CodeFormatterService
import com.example.service.DartAnalyzerService
import com.example.service.GitService
import com.example.service.PackageManagerService
import com.example.service.ProjectManager
import com.example.service.SdkDoctorService
import com.example.service.TerminalLine
import com.example.service.TerminalService
import com.example.ui.components.BuildDialog
import com.example.ui.components.CodeEditorView
import com.example.ui.components.CodingToolbar
import com.example.ui.components.DoctorDialog
import com.example.ui.components.FileTreeView
import com.example.ui.components.GitDialog
import com.example.ui.components.PackageCatalogDialog
import com.example.ui.components.ProblemsView
import com.example.ui.components.TerminalView
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeScreen(
    project: Project,
    projectManager: ProjectManager,
    analyzerService: DartAnalyzerService,
    formatterService: CodeFormatterService,
    terminalService: TerminalService,
    packageService: PackageManagerService,
    buildService: BuildManagerService,
    doctorService: SdkDoctorService,
    gitService: GitService,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val projectDir = remember { File(project.path) }
    var rootProjectFile by remember { mutableStateOf<ProjectFile?>(null) }

    // Active File State
    val mainDartFile = remember { File(projectDir, "lib/main.dart") }
    var currentFile by remember { mutableStateOf(if (mainDartFile.exists()) mainDartFile else File(projectDir, "pubspec.yaml")) }
    var editorText by remember { mutableStateOf(TextFieldValue(if (currentFile.exists()) currentFile.readText() else "")) }
    var isUnsaved by remember { mutableStateOf(false) }

    // Open file tabs
    var openTabs by remember { mutableStateOf(listOf(currentFile)) }

    // Diagnostics & Problems
    var diagnostics by remember { mutableStateOf<List<Diagnostic>>(emptyList()) }

    // Search in Editor State
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Bottom Panel State
    // 0: None, 1: Problems, 2: Terminal
    var bottomPanelIndex by remember { mutableStateOf(0) }

    // Terminal Output
    var terminalOutput by remember { mutableStateOf<List<TerminalLine>>(listOf(
        TerminalLine("REEX IDE Native Terminal (arm64-v8a)", isCommand = true),
        TerminalLine("Type 'reex doctor' or 'flutter analyze' to check workspace.")
    )) }
    var isTerminalRunning by remember { mutableStateOf(false) }

    // Dialogs
    var showDoctorDialog by remember { mutableStateOf(false) }
    var showBuildDialog by remember { mutableStateOf(false) }
    var showPackagesDialog by remember { mutableStateOf(false) }
    var showGitDialog by remember { mutableStateOf(false) }
    var showPreviewModal by remember { mutableStateOf(false) }
    var showAiCopilotDialog by remember { mutableStateOf(false) }

    val runtime: IFlutterRuntime = remember { FlutterRuntimeManager() }
    val aiService = remember { AiProjectAgentService() }

    LaunchedEffect(showPreviewModal) {
        if (showPreviewModal) {
            runtime.start(editorText.text)
        }
    }

    fun refreshTree() {
        if (projectDir.exists()) {
            rootProjectFile = projectManager.getFileTree(projectDir, projectDir.parent ?: "")
        }
    }

    fun runAnalysis(code: String) {
        if (currentFile.name.endsWith(".dart")) {
            diagnostics = analyzerService.analyze(currentFile.absolutePath, currentFile.name, code)
        } else {
            diagnostics = emptyList()
        }
    }

    LaunchedEffect(project.path) {
        refreshTree()
        if (currentFile.exists()) {
            val text = currentFile.readText()
            editorText = TextFieldValue(text)
            runAnalysis(text)
        }
    }

    BackHandler {
        when {
            showAiCopilotDialog -> showAiCopilotDialog = false
            showPreviewModal -> {
                scope.launch { runtime.stop() }
                showPreviewModal = false
            }
            showBuildDialog -> showBuildDialog = false
            showPackagesDialog -> showPackagesDialog = false
            showGitDialog -> showGitDialog = false
            showDoctorDialog -> showDoctorDialog = false
            drawerState.isOpen -> scope.launch { drawerState.close() }
            bottomPanelIndex != 0 -> bottomPanelIndex = 0
            isSearchVisible -> isSearchVisible = false
            else -> onBackToHome()
        }
    }

    fun openFile(file: File) {
        if (isUnsaved) {
            currentFile.writeText(editorText.text)
        }
        currentFile = file
        if (!openTabs.any { it.absolutePath == file.absolutePath }) {
            openTabs = openTabs + file
        }
        val content = if (file.exists()) file.readText() else ""
        editorText = TextFieldValue(content)
        isUnsaved = false
        runAnalysis(content)
        scope.launch { drawerState.close() }
    }

    fun saveFile() {
        if (currentFile.exists()) {
            currentFile.writeText(editorText.text)
            isUnsaved = false
            Toast.makeText(context, "Saved ${currentFile.name}", Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                modifier = Modifier.width(280.dp)
            ) {
                FileTreeView(
                    rootFile = rootProjectFile,
                    selectedFilePath = currentFile.absolutePath,
                    onFileClick = { projFile ->
                        openFile(File(projFile.path))
                    },
                    onCreateFile = { parentPath ->
                        val newF = File(parentPath, "new_file_${System.currentTimeMillis() % 1000}.dart")
                        newF.writeText("// New Dart File\n")
                        refreshTree()
                        openFile(newF)
                    },
                    onCreateFolder = { parentPath ->
                        val newD = File(parentPath, "folder_${System.currentTimeMillis() % 1000}")
                        newD.mkdirs()
                        refreshTree()
                    },
                    onDeleteFile = { projFile ->
                        File(projFile.path).deleteRecursively()
                        refreshTree()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = project.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isUnsaved) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(CyanPrimary)
                                    )
                                }
                            }
                            Text(
                                text = currentFile.name,
                                fontSize = 11.sp,
                                color = CyanPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Explorer", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showAiCopilotDialog = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanPrimary.copy(alpha = 0.18f))
                                .testTag("action_ai_copilot")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Copilot", tint = CyanPrimary)
                        }
                        IconButton(onClick = { showPreviewModal = true }, modifier = Modifier.testTag("action_run_preview")) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run Preview", tint = AccentGreen)
                        }
                        IconButton(onClick = { saveFile() }, modifier = Modifier.testTag("action_save_file")) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = if (isUnsaved) CyanPrimary else Color(0xFF8B949E))
                        }
                        IconButton(onClick = { showBuildDialog = true }, modifier = Modifier.testTag("action_build_apk")) {
                            Icon(Icons.Default.Build, contentDescription = "Build APK", tint = Color.White)
                        }
                        IconButton(onClick = { showPackagesDialog = true }, modifier = Modifier.testTag("action_packages")) {
                            Icon(Icons.Default.Inventory2, contentDescription = "Packages", tint = Color.White)
                        }
                        IconButton(onClick = { showGitDialog = true }, modifier = Modifier.testTag("action_git")) {
                            Icon(Icons.Default.Commit, contentDescription = "Git", tint = Color.White)
                        }
                        IconButton(onClick = onBackToHome) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit to Projects", tint = Color(0xFF8B949E))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
            ) {
                // Tabs Row for open files
                if (openTabs.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        openTabs.forEach { tabFile ->
                            val isActive = tabFile.absolutePath == currentFile.absolutePath
                            Row(
                                modifier = Modifier
                                    .background(if (isActive) DarkBackground else Color.Transparent)
                                    .clickable { openFile(tabFile) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tabFile.name,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isActive) CyanPrimary else Color(0xFF8B949E),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                if (openTabs.size > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close Tab",
                                        tint = Color(0xFF8B949E),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                val remaining = openTabs.filterNot { it.absolutePath == tabFile.absolutePath }
                                                openTabs = remaining
                                                if (isActive && remaining.isNotEmpty()) {
                                                    openFile(remaining.first())
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                // Center: Code Editor
                Box(modifier = Modifier.weight(1f)) {
                    CodeEditorView(
                        textFieldValue = editorText,
                        onValueChange = { newVal ->
                            editorText = newVal
                            isUnsaved = true
                            runAnalysis(newVal.text)
                        },
                        diagnostics = diagnostics,
                        isSearchVisible = isSearchVisible,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { q -> searchQuery = q },
                        onCloseSearch = { isSearchVisible = false }
                    )
                }

                // Mobile Coding Accessory Toolbar
                CodingToolbar(
                    onInsertSymbol = { sym ->
                        val current = editorText.text
                        val sel = editorText.selection
                        val newText = current.substring(0, sel.start) + sym + current.substring(sel.end)
                        val newPos = sel.start + sym.length
                        editorText = TextFieldValue(newText, TextRange(newPos))
                        isUnsaved = true
                        runAnalysis(newText)
                    },
                    onUndo = {
                        // Basic undo support
                    },
                    onRedo = {
                        // Basic redo support
                    },
                    onFormat = {
                        val formatted = formatterService.format(editorText.text)
                        editorText = TextFieldValue(formatted)
                        isUnsaved = true
                        runAnalysis(formatted)
                        Toast.makeText(context, "Formatted with Dart style", Toast.LENGTH_SHORT).show()
                    },
                    onFindToggle = {
                        isSearchVisible = !isSearchVisible
                    },
                    onToggleComment = {
                        val current = editorText.text
                        val sel = editorText.selection
                        val newText = current.substring(0, sel.start) + "// " + current.substring(sel.end)
                        editorText = TextFieldValue(newText, TextRange(sel.start + 3))
                        isUnsaved = true
                    },
                    onInsertSnippet = { key ->
                        val snippet = when (key) {
                            "stless" -> "\nclass MyWidget extends StatelessWidget {\n  const MyWidget({super.key});\n\n  @override\n  Widget build(BuildContext context) {\n    return Container();\n  }\n}\n"
                            "stful" -> "\nclass MyWidget extends StatefulWidget {\n  const MyWidget({super.key});\n\n  @override\n  State<MyWidget> createState() => _MyWidgetState();\n}\n\nclass _MyWidgetState extends State<MyWidget> {\n  @override\n  Widget build(BuildContext context) {\n    return Container();\n  }\n}\n"
                            "scaffold" -> "Scaffold(\n  appBar: AppBar(title: const Text('Title')),\n  body: const Center(child: Text('Content')),\n)"
                            "column" -> "Column(\n  children: const [\n    Text('Item 1'),\n    Text('Item 2'),\n  ],\n)"
                            "row" -> "Row(\n  children: const [\n    Icon(Icons.star),\n    Text('Star'),\n  ],\n)"
                            "listview" -> "ListView.builder(\n  itemCount: 10,\n  itemBuilder: (context, index) => ListTile(title: Text('Item \$index')),\n)"
                            "gridview" -> "GridView.count(\n  crossAxisCount: 2,\n  children: const [\n    Card(child: Center(child: Text('Grid 1'))),\n    Card(child: Center(child: Text('Grid 2'))),\n  ],\n)"
                            "card" -> "Card(\n  elevation: 4,\n  child: Padding(\n    padding: const EdgeInsets.all(16),\n    child: const Text('Card Content'),\n  ),\n)"
                            "container" -> "Container(\n  padding: const EdgeInsets.all(12),\n  decoration: BoxDecoration(\n    color: Colors.blueGrey,\n    borderRadius: BorderRadius.circular(8),\n  ),\n  child: const Text('Container'),\n)"
                            "textfield" -> "TextField(\n  decoration: const InputDecoration(\n    labelText: 'Label',\n    border: OutlineInputBorder(),\n  ),\n)"
                            "future" -> "FutureBuilder(\n  future: myFuture,\n  builder: (context, snapshot) {\n    if (snapshot.connectionState == ConnectionState.waiting) return const CircularProgressIndicator();\n    return Text('\${snapshot.data}');\n  },\n)"
                            "stream" -> "StreamBuilder(\n  stream: myStream,\n  builder: (context, snapshot) => Text('\${snapshot.data}'),\n)"
                            else -> ""
                        }
                        val current = editorText.text
                        val sel = editorText.selection
                        val newText = current.substring(0, sel.start) + snippet + current.substring(sel.end)
                        editorText = TextFieldValue(newText, TextRange(sel.start + snippet.length))
                        isUnsaved = true
                        runAnalysis(newText)
                    }
                )

                // Bottom Panel Toggle Tabs (Problems, Terminal, Close)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
                    val warnCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

                    Row(
                        modifier = Modifier
                            .clickable { bottomPanelIndex = if (bottomPanelIndex == 1) 0 else 1 }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = if (errorCount > 0) AccentRed else Color(0xFF8B949E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${LocalizationManager.str("المشاكل", "Problems")} ($errorCount E, $warnCount W)",
                            fontSize = 11.sp,
                            fontWeight = if (bottomPanelIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (bottomPanelIndex == 1) CyanPrimary else Color(0xFFC9D1D9)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        modifier = Modifier
                            .clickable { bottomPanelIndex = if (bottomPanelIndex == 2) 0 else 2 }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            tint = if (bottomPanelIndex == 2) CyanPrimary else Color(0xFF8B949E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LocalizationManager.str("الطرفية", "Terminal"),
                            fontSize = 11.sp,
                            fontWeight = if (bottomPanelIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (bottomPanelIndex == 2) CyanPrimary else Color(0xFFC9D1D9)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (bottomPanelIndex != 0) {
                        IconButton(
                            onClick = { bottomPanelIndex = 0 },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Panel", tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Bottom Expandable Drawer Content
                AnimatedVisibility(
                    visible = bottomPanelIndex != 0,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(DarkBackground)
                    ) {
                        when (bottomPanelIndex) {
                            1 -> ProblemsView(
                                diagnostics = diagnostics,
                                onSelectDiagnostic = { diag ->
                                    // Move cursor to that line
                                    val lines = editorText.text.lines()
                                    var offset = 0
                                    for (i in 0 until (diag.line - 1).coerceAtMost(lines.size - 1)) {
                                        offset += lines[i].length + 1
                                    }
                                    offset = (offset + diag.column - 1).coerceIn(0, editorText.text.length)
                                    editorText = editorText.copy(selection = TextRange(offset))
                                }
                            )
                            2 -> TerminalView(
                                outputLines = terminalOutput,
                                isRunning = isTerminalRunning,
                                onSendCommand = { cmd ->
                                    scope.launch {
                                        isTerminalRunning = true
                                        terminalService.executeCommand(cmd, projectDir) { text, isErr ->
                                            terminalOutput = terminalOutput + TerminalLine(text, isError = isErr)
                                        }
                                        isTerminalRunning = false
                                    }
                                },
                                onClear = { terminalOutput = emptyList() },
                                onKill = {
                                    terminalService.killCurrentProcess()
                                    isTerminalRunning = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showDoctorDialog) {
        DoctorDialog(
            doctorService = doctorService,
            onDismiss = { showDoctorDialog = false }
        )
    }

    if (showBuildDialog) {
        BuildDialog(
            projectDir = projectDir,
            buildService = buildService,
            onDismiss = { showBuildDialog = false }
        )
    }

    if (showPackagesDialog) {
        PackageCatalogDialog(
            projectDir = projectDir,
            packageService = packageService,
            onDismiss = { showPackagesDialog = false },
            onPubspecModified = {
                val pubspec = File(projectDir, "pubspec.yaml")
                if (currentFile.name == "pubspec.yaml" && pubspec.exists()) {
                    editorText = TextFieldValue(pubspec.readText())
                }
                refreshTree()
            }
        )
    }

    if (showGitDialog) {
        GitDialog(
            projectDir = projectDir,
            gitService = gitService,
            onDismiss = { showGitDialog = false }
        )
    }

    if (showPreviewModal) {
        Dialog(
            onDismissRequest = {
                scope.launch { runtime.stop() }
                showPreviewModal = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            EmulatorView(
                runtime = runtime,
                editorCode = editorText.text,
                onClose = {
                    scope.launch { runtime.stop() }
                    showPreviewModal = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showAiCopilotDialog) {
        val summary = if (diagnostics.isEmpty()) "No errors detected." else "${diagnostics.size} issues found: " + diagnostics.take(3).joinToString { it.message }
        AiCopilotDialog(
            aiService = aiService,
            project = project,
            currentFile = currentFile,
            currentCode = editorText.text,
            diagnosticsSummary = summary,
            onApplyCode = { newCode ->
                editorText = TextFieldValue(newCode)
                if (currentFile.exists()) {
                    currentFile.writeText(newCode)
                    isUnsaved = false
                    runAnalysis(newCode)
                    scope.launch {
                        runtime.hotReload(newCode)
                    }
                    Toast.makeText(
                        context,
                        LocalizationManager.str("تم تطبيق كود الذكاء الاصطناعي وإعادة التحميل ⚡", "Applied AI code & Hot Reloaded ⚡"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = { showAiCopilotDialog = false }
        )
    }
}
