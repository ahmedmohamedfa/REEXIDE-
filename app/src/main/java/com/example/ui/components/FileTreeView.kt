package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectFile
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun FileTreeView(
    rootFile: ProjectFile?,
    selectedFilePath: String,
    onFileClick: (ProjectFile) -> Unit,
    onCreateFile: (parentDir: String) -> Unit,
    onCreateFolder: (parentDir: String) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }

    fun toggleFolder(path: String) {
        expandedFolders = if (expandedFolders.contains(path)) {
            expandedFolders - path
        } else {
            expandedFolders + path
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(8.dp)
            .testTag("file_tree_view")
    ) {
        // Tree Header with New File / New Folder actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXPLORER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B949E),
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { rootFile?.let { onCreateFile(it.path) } },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New File",
                    tint = Color(0xFFC9D1D9),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { rootFile?.let { onCreateFolder(it.path) } },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Default.CreateNewFolder,
                    contentDescription = "New Folder",
                    tint = Color(0xFFC9D1D9),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkBorder)
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Recursive tree rendering flattened
        if (rootFile != null) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    val isExpanded = expandedFolders.contains(rootFile.path) || expandedFolders.isEmpty()
                    FolderItemRow(
                        file = rootFile,
                        level = 0,
                        isExpanded = isExpanded,
                        onToggle = { toggleFolder(rootFile.path) },
                        onCreateFile = { onCreateFile(rootFile.path) }
                    )
                }

                val flattened = flattenTree(rootFile.children, expandedFolders, 1)
                items(flattened, key = { it.first.path }) { (file, depth) ->
                    if (file.isDirectory) {
                        val isExpanded = expandedFolders.contains(file.path)
                        FolderItemRow(
                            file = file,
                            level = depth,
                            isExpanded = isExpanded,
                            onToggle = { toggleFolder(file.path) },
                            onCreateFile = { onCreateFile(file.path) }
                        )
                    } else {
                        FileItemRow(
                            file = file,
                            level = depth,
                            isSelected = file.path == selectedFilePath,
                            onClick = { onFileClick(file) },
                            onDelete = { onDeleteFile(file) }
                        )
                    }
                }
            }
        }
    }
}

private fun flattenTree(
    files: List<ProjectFile>,
    expandedPaths: Set<String>,
    depth: Int
): List<Pair<ProjectFile, Int>> {
    val result = mutableListOf<Pair<ProjectFile, Int>>()
    for (file in files) {
        result.add(file to depth)
        if (file.isDirectory && (expandedPaths.contains(file.path) || depth == 1 && (file.name == "lib" || file.name == "test"))) {
            result.addAll(flattenTree(file.children, expandedPaths, depth + 1))
        }
    }
    return result
}

@Composable
private fun FolderItemRow(
    file: ProjectFile,
    level: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCreateFile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp, horizontal = (level * 12).dp)
            .testTag("folder_${file.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF8B949E),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFF58A6FF),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = file.name,
            color = Color(0xFFE6EDF3),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FileItemRow(
    file: ProjectFile,
    level: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    val iconColor = when (file.extension.lowercase()) {
        "dart" -> Color(0xFF58A6FF)
        "yaml", "yml" -> Color(0xFFBC8CFF)
        "json" -> Color(0xFF3FB950)
        "xml" -> Color(0xFFFFA657)
        "md" -> Color(0xFF79C0FF)
        else -> Color(0xFF8B949E)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) DarkSurfaceVariant else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .padding(vertical = 4.dp, horizontal = (level * 12 + 16).dp)
            .testTag("file_${file.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (file.isDartFile) Icons.Default.Code else Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = file.name,
            color = if (isSelected) CyanPrimary else Color(0xFFC9D1D9),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )

        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF8B949E),
                    modifier = Modifier.size(14.dp)
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}
