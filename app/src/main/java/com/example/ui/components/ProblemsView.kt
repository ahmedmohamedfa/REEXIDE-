package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.model.Diagnostic
import com.example.model.DiagnosticSeverity
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun ProblemsView(
    diagnostics: List<Diagnostic>,
    onSelectDiagnostic: (Diagnostic) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<DiagnosticSeverity?>(null) }

    val errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
    val warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }
    val hintCount = diagnostics.count { it.severity == DiagnosticSeverity.HINT || it.severity == DiagnosticSeverity.INFO }

    val filtered = if (selectedFilter == null) {
        diagnostics
    } else {
        diagnostics.filter { it.severity == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(8.dp)
            .testTag("problems_view")
    ) {
        // Filter Chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${diagnostics.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkSurfaceVariant,
                    selectedLabelColor = Color.White,
                    containerColor = DarkSurface,
                    labelColor = Color(0xFF8B949E)
                )
            )

            FilterChip(
                selected = selectedFilter == DiagnosticSeverity.ERROR,
                onClick = { selectedFilter = if (selectedFilter == DiagnosticSeverity.ERROR) null else DiagnosticSeverity.ERROR },
                leadingIcon = {
                    Icon(Icons.Default.Error, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
                },
                label = { Text("Errors ($errorCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkSurfaceVariant,
                    selectedLabelColor = AccentRed,
                    containerColor = DarkSurface,
                    labelColor = Color(0xFF8B949E)
                )
            )

            FilterChip(
                selected = selectedFilter == DiagnosticSeverity.WARNING,
                onClick = { selectedFilter = if (selectedFilter == DiagnosticSeverity.WARNING) null else DiagnosticSeverity.WARNING },
                leadingIcon = {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                },
                label = { Text("Warnings ($warningCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkSurfaceVariant,
                    selectedLabelColor = AccentYellow,
                    containerColor = DarkSurface,
                    labelColor = Color(0xFF8B949E)
                )
            )
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "No issues",
                        tint = AccentGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No problems found in workspace",
                        color = Color(0xFFC9D1D9),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Dart Analysis engine is active",
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
            ) {
                items(filtered, key = { it.id }) { diag ->
                    ProblemItemRow(diag = diag, onClick = { onSelectDiagnostic(diag) })
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                }
            }
        }
    }
}

@Composable
private fun ProblemItemRow(
    diag: Diagnostic,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        val (icon, color) = when (diag.severity) {
            DiagnosticSeverity.ERROR -> Icons.Default.Error to AccentRed
            DiagnosticSeverity.WARNING -> Icons.Default.Warning to AccentYellow
            DiagnosticSeverity.INFO -> Icons.Default.Info to CyanPrimary
            DiagnosticSeverity.HINT -> Icons.Default.Lightbulb to Color(0xFFBC8CFF)
        }

        Icon(
            icon,
            contentDescription = diag.severity.name,
            tint = color,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = diag.message,
                color = Color(0xFFE6EDF3),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${diag.fileName}:${diag.line}:${diag.column}",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (diag.code.isNotEmpty()) {
                    Text(
                        text = " • ${diag.code}",
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            if (diag.quickFix != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Quick fix: ${diag.quickFix}",
                    color = AccentGreen,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
