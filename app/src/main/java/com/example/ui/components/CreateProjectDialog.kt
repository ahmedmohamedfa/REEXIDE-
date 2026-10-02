package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.ProjectTemplate
import com.example.service.LocalizationManager
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, template: ProjectTemplate) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(ProjectTemplate.COUNTER) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validateAndCreate() {
        val trimmed = projectName.trim().lowercase().replace(" ", "_")
        if (trimmed.isEmpty()) {
            errorMessage = LocalizationManager.str("اسم المشروع لا يمكن أن يكون فارغاً", "Project name cannot be empty")
            return
        }
        if (!trimmed.matches(Regex("^[a-z][a-z0-9_]*$"))) {
            errorMessage = LocalizationManager.str("يجب أن يبدأ بحرف إنجليزي صغير ويحتوي أحرف وأرقام و _ فقط", "Must start with a lowercase letter and contain only letters, numbers, and _")
            return
        }
        onCreateProject(trimmed, selectedTemplate)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LocalizationManager.str("إنشاء مشروع Flutter جديد", "Create New Flutter Project"),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = LocalizationManager.str("اسم المشروع (مجلد الحزمة)", "Project Name (package identifier)"),
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        errorMessage = null
                    },
                    placeholder = { Text("my_flutter_app", color = Color(0xFF8B949E)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_name_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFF85149),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = LocalizationManager.str("اختر قالب المشروع (جاهز دون اتصال)", "Select Template (Offline Ready)"),
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(ProjectTemplate.values()) { tmpl ->
                        val isSelected = tmpl == selectedTemplate
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkSurfaceVariant else DarkBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanPrimary else DarkBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTemplate = tmpl }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (tmpl) {
                                ProjectTemplate.COUNTER -> Icons.Default.AddCircle
                                ProjectTemplate.EMPTY -> Icons.Outlined.CheckBoxOutlineBlank
                                ProjectTemplate.NAVIGATION -> Icons.Default.Tab
                                ProjectTemplate.LOCAL_STORAGE -> Icons.Default.Storage
                                ProjectTemplate.HTTP_CLIENT -> Icons.Default.CloudDownload
                                ProjectTemplate.STATE_MANAGEMENT -> Icons.Default.AccountTree
                            }

                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) CyanPrimary else Color(0xFF8B949E),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tmpl.displayName,
                                    color = if (isSelected) Color.White else Color(0xFFC9D1D9),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = tmpl.description,
                                    color = Color(0xFF8B949E),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndCreate() },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_create_project_confirm")
            ) {
                Text(
                    text = LocalizationManager.str("إنشاء المشروع", "Create Project"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = LocalizationManager.str("إلغاء", "Cancel"),
                    color = Color(0xFF8B949E)
                )
            }
        }
    )
}
