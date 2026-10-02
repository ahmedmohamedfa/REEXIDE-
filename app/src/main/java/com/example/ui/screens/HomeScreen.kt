package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Project
import com.example.model.ProjectTemplate
import com.example.runtime.FlutterRuntimeManager
import com.example.service.LocalizationManager
import com.example.service.ProjectManager
import com.example.service.SdkDoctorService
import com.example.ui.components.CreateProjectDialog
import com.example.ui.components.DoctorDialog
import com.example.ui.components.EmulatorView
import kotlinx.coroutines.launch
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    projectManager: ProjectManager,
    doctorService: SdkDoctorService,
    onOpenProject: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf(projectManager.getProjects()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDoctorDialog by remember { mutableStateOf(false) }
    var emulatorProject by remember { mutableStateOf<Project?>(null) }
    val runtime = remember { FlutterRuntimeManager() }

    LaunchedEffect(emulatorProject) {
        emulatorProject?.let { proj ->
            val mainDart = java.io.File(proj.path, "lib/main.dart")
            val code = if (mainDart.exists()) mainDart.readText() else ""
            runtime.start(code)
        }
    }

    fun refreshProjects() {
        projects = projectManager.getProjects()
    }

    BackHandler(enabled = emulatorProject != null || showCreateDialog || showDoctorDialog) {
        if (emulatorProject != null) {
            scope.launch { runtime.stop() }
            emulatorProject = null
        } else if (showCreateDialog) {
            showCreateDialog = false
        } else if (showDoctorDialog) {
            showDoctorDialog = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val isWideScreen = maxWidth >= 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 16.dp)
                .testTag("home_screen")
        ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "REEX IDE",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = LocalizationManager.str("بيئة فلاتر ودارت المتكاملة", "Offline Flutter & Dart Toolchain"),
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .clickable { LocalizationManager.toggleLanguage() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("language_toggle_button")
                ) {
                    Text(
                        text = if (LocalizationManager.currentLanguage == com.example.service.AppLanguage.ARABIC) "English" else "العربية",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showDoctorDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .testTag("home_doctor_button")
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = "REEX Doctor",
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SDK & Toolchain Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocalizationManager.str("حالة بيئة التطوير", "ENVIRONMENT STATUS"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LocalizationManager.str("جاهز بدون إنترنت [✓]", "OFFLINE READY [✓]"),
                            color = AccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusPill(label = "Flutter", value = "3.24.3")
                    StatusPill(label = "Dart", value = "3.5.3")
                    StatusPill(label = "ABI", value = "arm64-v8a")
                    StatusPill(label = "Pub Cache", value = "12 pkgs")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("create_project_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LocalizationManager.str("مشروع جديد", "New Project"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Projects Section
        Text(
            text = "${LocalizationManager.str("المشاريع", "PROJECTS")} (${projects.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B949E),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = LocalizationManager.str("لا توجد مشاريع حتى الآن", "No projects yet"),
                        color = Color(0xFFC9D1D9),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocalizationManager.str("اضغط 'مشروع جديد' لبدء وتجهيز مشروع فلاتر مخصص", "Click 'New Project' to initialize a Flutter workspace"),
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpenProject(project) },
                        onRunEmulator = { emulatorProject = project },
                        onExportZip = {
                            val zip = projectManager.exportProjectZip(project)
                            shareFile(context, zip)
                        },
                        onDelete = {
                            projectManager.deleteProject(project.name)
                            refreshProjects()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

    if (emulatorProject != null) {
        val proj = emulatorProject!!
        val mainDart = java.io.File(proj.path, "lib/main.dart")
        val code = if (mainDart.exists()) mainDart.readText() else ""
        Dialog(
            onDismissRequest = {
                scope.launch { runtime.stop() }
                emulatorProject = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            EmulatorView(
                runtime = runtime,
                editorCode = code,
                onClose = {
                    scope.launch { runtime.stop() }
                    emulatorProject = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreateProject = { name, template ->
                val newProj = projectManager.createProject(name, template)
                showCreateDialog = false
                refreshProjects()
                onOpenProject(newProj)
            }
        )
    }

    if (showDoctorDialog) {
        DoctorDialog(
            doctorService = doctorService,
            onDismiss = { showDoctorDialog = false }
        )
    }
}

@Composable
private fun StatusPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF8B949E), fontSize = 10.sp)
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onRunEmulator: () -> Unit,
    onExportZip: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(project.lastModified))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("project_card_${project.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.template.displayName,
                            color = CyanPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Modified: $dateStr • Flutter ${project.flutterVersion}",
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp
                )
            }

            // Quick Run in Emulator Button
            IconButton(
                onClick = onRunEmulator,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentGreen.copy(alpha = 0.15f))
                    .testTag("run_emulator_btn_${project.name}")
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Run in Emulator",
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color(0xFF8B949E)
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export as ZIP") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onExportZip()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Project", color = Color(0xFFF85149)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF85149)) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun shareFile(context: Context, file: java.io.File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Flutter Project ZIP"))
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Project exported to ${file.absolutePath}")
        }
        context.startActivity(Intent.createChooser(intent, "Project Exported"))
    }
}
