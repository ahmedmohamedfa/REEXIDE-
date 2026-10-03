package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.model.BuildHistoryItem
import com.example.model.BuildStatus
import com.example.service.BuildManagerService
import com.example.service.LocalizationManager
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BuildDialog(
    projectDir: File,
    buildService: BuildManagerService,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var isBuilding by remember { mutableStateOf(false) }
    var buildLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var completedBuild by remember { mutableStateOf<BuildHistoryItem?>(null) }
    var showWorkflowCreatedBanner by remember { mutableStateOf(false) }

    fun shareApk(apkPath: String) {
        try {
            val file = File(apkPath)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share APK"))
            }
        } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = LocalizationManager.str("بناء APK ونظام سير العمل", "Build APK & CI/CD Pipeline"),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LocalizationManager.str("بناء محلي بدون إنترنت أو مسار GitHub Actions", "Local Offline Build or GitHub Workflows"),
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = DarkSurfaceVariant,
                    contentColor = CyanPrimary,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = CyanPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LocalizationManager.str("بناء أوفلاين (APK)", "Offline APK"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LocalizationManager.str("سير عمل GitHub", "GitHub Actions"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    // Local Offline Build
                    if (completedBuild != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .border(1.dp, AccentGreen, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LocalizationManager.str("تم توليد حزمة APK بنجاح (arm64-v8a)", "APK Generated (arm64-v8a)"),
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("SHA-256: ${completedBuild?.sha256}", color = Color(0xFFC9D1D9), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("Path: ${completedBuild?.apkPath}", color = Color(0xFF8B949E), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Build Console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(8.dp)
                    ) {
                        if (buildLogs.isEmpty() && !isBuilding) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = LocalizationManager.str(
                                        "جاهز لتجميع وبناء APK محلياً بدون إنترنت.\nالمعمارية المستهدفة: arm64-v8a",
                                        "Ready to package APK offline using bundled toolchain.\nTarget ABI: arm64-v8a"
                                    ),
                                    color = Color(0xFF8B949E),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(buildLogs) { log ->
                                    Text(
                                        text = log,
                                        color = if (log.contains("SUCCESS") || log.contains("generated")) AccentGreen else Color(0xFFC9D1D9),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // GitHub Actions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = LocalizationManager.str("مسارات عمل GitHub Actions CI/CD", "GitHub Actions CI/CD Workflows"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LocalizationManager.str(
                                "توليد ملفات سير العمل .github/workflows تلقائياً لبناء التطبيق وتشغيل الاختبارات وتوليد حزم Release على مستودع GitHub.",
                                "Automatically generates .github/workflows for building APKs, running tests, and publishing releases on GitHub."
                            ),
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (showWorkflowCreatedBanner) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentGreen.copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = LocalizationManager.str(
                                        "تم إنشاء .github/workflows/build-android.yml بنجاح [✓]",
                                        "Created .github/workflows/build-android.yml [✓]"
                                    ),
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                buildService.generateGitHubActionsWorkflows(projectDir)
                                showWorkflowCreatedBanner = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_generate_github_actions")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LocalizationManager.str("توليد مسارات سير العمل للمشروع", "Generate CI/CD Workflows"),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTabIndex == 0) {
                if (completedBuild != null) {
                    Button(
                        onClick = { shareApk(completedBuild!!.apkPath) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_build_share_apk")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LocalizationManager.str("مشاركة APK", "Share APK"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                isBuilding = true
                                buildLogs = emptyList()
                                val item = buildService.performLocalBuild(
                                    projectDir = projectDir,
                                    onLog = { line: String -> buildLogs = buildLogs + line }
                                )
                                isBuilding = false
                                if (item.status == BuildStatus.SUCCESS) {
                                    completedBuild = item
                                }
                            }
                        },
                        enabled = !isBuilding,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_build_apk_confirm")
                    ) {
                        if (isBuilding) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isBuilding) LocalizationManager.str("جاري البناء...", "Building...") else LocalizationManager.str("بدء بناء APK", "Build APK"),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = LocalizationManager.str("إغلاق", "Close"),
                    color = Color(0xFF8B949E)
                )
            }
        }
    )
}
