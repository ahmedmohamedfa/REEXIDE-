package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PubPackage
import com.example.service.LocalizationManager
import com.example.service.PackageManagerService
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RobotoMonoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PackageCatalogDialog(
    projectDir: File,
    packageService: PackageManagerService,
    onDismiss: () -> Unit,
    onPubspecModified: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var packages by remember { mutableStateOf(packageService.getCatalog(projectDir)) }
    var searchQuery by remember { mutableStateOf("") }
    var showCustomInstaller by remember { mutableStateOf(false) }
    var customPackageInput by remember { mutableStateOf("") }
    var customVersionInput by remember { mutableStateOf("^1.0.0") }
    var isDownloadingCustom by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        packages = packageService.getCatalog(projectDir)
        onPubspecModified()
    }

    val filteredPackages = packages.filter { pkg ->
        searchQuery.isBlank() ||
                pkg.name.contains(searchQuery, ignoreCase = true) ||
                pkg.category.contains(searchQuery, ignoreCase = true) ||
                pkg.description.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = LocalizationManager.str("مدير ومستودع حزم Pub", "Pub Package Manager"),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LocalizationManager.str("تثبيت وتنزيل مكاتب Flutter & Dart في المشروع", "Install & Download Flutter / Dart Packages"),
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Search & Add Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(LocalizationManager.str("بحث عن حزمة (provider, dio, bloc...)", "Search packages..."), fontSize = 11.sp, color = Color(0xFF8B949E)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = DarkBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("package_search_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showCustomInstaller = !showCustomInstaller },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showCustomInstaller) CyanPrimary else DarkSurfaceVariant)
                            .testTag("btn_toggle_custom_package")
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = "Download Package",
                            tint = if (showCustomInstaller) Color.Black else CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Custom Package Download Drawer
                AnimatedVisibility(visible = showCustomInstaller) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = LocalizationManager.str("📥 تحميل حزمة مخصصة من Pub.dev:", "📥 Download Package from Pub.dev:"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customPackageInput,
                                onValueChange = { customPackageInput = it },
                                placeholder = { Text("e.g. camera, intl, hive", fontSize = 11.sp, color = Color(0xFF8B949E)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                modifier = Modifier.weight(1f).height(42.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = customVersionInput,
                                onValueChange = { customVersionInput = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                modifier = Modifier.width(80.dp).height(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (customPackageInput.isNotBlank() && !isDownloadingCustom) {
                                    scope.launch {
                                        isDownloadingCustom = true
                                        statusMessage = LocalizationManager.str("جارٍ تنزيل الحزمة وحل الاعتماديات...", "Resolving dependencies from pub.dev...")
                                        delay(600)
                                        val (success, msg) = packageService.downloadAndInstallCustomPackage(projectDir, customPackageInput, customVersionInput)
                                        isDownloadingCustom = false
                                        statusMessage = msg
                                        if (success) {
                                            customPackageInput = ""
                                            refresh()
                                        }
                                    }
                                }
                            },
                            enabled = customPackageInput.isNotBlank() && !isDownloadingCustom,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("btn_confirm_download_package")
                        ) {
                            if (isDownloadingCustom) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = LocalizationManager.str("تحميل وتثبيت في المشروع", "Download & Install Package"),
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Status banner
                if (statusMessage != null) {
                    Text(
                        text = statusMessage ?: "",
                        color = AccentGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Packages List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredPackages, key = { it.name }) { pkg ->
                        PackageItemRow(
                            pkg = pkg,
                            onInstall = {
                                val success = packageService.addPackageToProject(projectDir, pkg)
                                if (success) {
                                    statusMessage = "Installed ${pkg.name} [✓]"
                                    refresh()
                                }
                            },
                            onRemove = {
                                val success = packageService.removePackageFromProject(projectDir, pkg.name)
                                if (success) {
                                    statusMessage = "Removed ${pkg.name} [✓]"
                                    refresh()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("btn_close_package_catalog")
            ) {
                Text(LocalizationManager.str("تم", "Done"), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PackageItemRow(
    pkg: PubPackage,
    onInstall: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(
                1.dp,
                if (pkg.isInstalledInCurrentProject) AccentGreen.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
            .testTag("package_row_${pkg.name}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pkg.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = RobotoMonoFontFamily
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pkg.version,
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontFamily = RobotoMonoFontFamily
                    )
                }

                if (pkg.isInstalledInCurrentProject) {
                    OutlinedButton(
                        onClick = onRemove,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        modifier = Modifier.height(30.dp).testTag("btn_remove_${pkg.name}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LocalizationManager.str("إزالة", "Remove"), fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.height(30.dp).testTag("btn_install_${pkg.name}")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LocalizationManager.str("تثبيت", "Install"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = pkg.description,
                color = Color(0xFF8B949E),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E2633))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pkg.category,
                        color = Color(0xFF58A6FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (pkg.isInstalledInCurrentProject) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LocalizationManager.str("مثبت في pubspec.yaml", "Active in project"),
                            color = AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
