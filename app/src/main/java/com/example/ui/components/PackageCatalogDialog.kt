package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import java.io.File

@Composable
fun PackageCatalogDialog(
    projectDir: File,
    packageService: PackageManagerService,
    onDismiss: () -> Unit,
    onPubspecModified: () -> Unit
) {
    var packages by remember { mutableStateOf(packageService.getCatalog(projectDir)) }

    fun refresh() {
        packages = packageService.getCatalog(projectDir)
        onPubspecModified()
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
                        text = LocalizationManager.str("مستودع حزم Pub بدون إنترنت", "Pub Package Cache"),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LocalizationManager.str("كتالوج الحزم المعتمدة مسبقاً للتجميع الفوري", "Offline Verified Packages Catalog"),
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = LocalizationManager.str(
                        "جميع الحزم التالية مخزنة مسبقاً في الذاكرة لتثبيتها واستخدامها دون الحاجة للإنترنت.",
                        "All packages below are pre-cached in Global Pub Cache for instant offline compilation."
                    ),
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(packages, key = { it.name }) { pkg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .border(1.dp, if (pkg.isInstalledInCurrentProject) CyanPrimary.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pkg.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pkg.version,
                                        color = CyanPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceVariant)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(pkg.category, color = Color(0xFF8B949E), fontSize = 9.sp)
                                    }
                                }

                                Text(
                                    text = pkg.description,
                                    color = Color(0xFFC9D1D9),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Text(
                                    text = "SHA: ${pkg.sha256.take(16)}...",
                                    color = Color(0xFF8B949E),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (pkg.isInstalledInCurrentProject) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AccentGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = LocalizationManager.str("مثبت ✓", "Installed"),
                                            color = AccentGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            packageService.removePackageFromProject(projectDir, pkg.name)
                                            refresh()
                                        },
                                        modifier = Modifier.size(34.dp).testTag("pkg_remove_${pkg.name}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = AccentRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        packageService.addPackageToProject(projectDir, pkg)
                                        refresh()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp).testTag("pkg_install_${pkg.name}")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = LocalizationManager.str("تثبيت", "Add"),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = LocalizationManager.str("إغلاق", "Close"),
                    color = Color(0xFF8B949E)
                )
            }
        }
    )
}
