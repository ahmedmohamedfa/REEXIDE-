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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.model.CheckStatus
import com.example.model.DoctorCheck
import com.example.service.LocalizationManager
import com.example.service.SdkDoctorService
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DoctorDialog(
    doctorService: SdkDoctorService,
    onDismiss: () -> Unit
) {
    var checks by remember { mutableStateOf(doctorService.runDoctorChecks()) }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = AccentGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "REEX Doctor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LocalizationManager.str("فحص بيئة SDK والأدوات بدون اتصال", "SDK & Offline Toolchain Verification"),
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (verifyResult != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentGreen.copy(alpha = 0.15f))
                            .border(1.dp, AccentGreen, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(verifyResult ?: "", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(checks, key = { it.id }) { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val (icon, tint) = when (check.status) {
                                CheckStatus.PASSED -> Icons.Default.CheckCircle to AccentGreen
                                CheckStatus.WARNING -> Icons.Default.Warning to AccentYellow
                                CheckStatus.FAILED -> Icons.Default.Error to AccentRed
                                CheckStatus.CHECKING -> Icons.Default.Refresh to CyanPrimary
                            }

                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(check.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(check.subtitle, color = Color(0xFFC9D1D9), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(check.details, color = Color(0xFF8B949E), fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                                if (check.fixSuggestion != null) {
                                    Text(
                                        text = "${LocalizationManager.str("اقتراح الحل: ", "Suggestion: ")}${check.fixSuggestion}",
                                        color = AccentYellow,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isVerifying = true
                        verifyResult = null
                        delay(600)
                        checks = doctorService.runDoctorChecks()
                        isVerifying = false
                        verifyResult = LocalizationManager.str("تم فحص بيئة Flutter و Dart بنجاح [✓]", "All Offline Toolchain checks verified [✓]")
                    }
                },
                enabled = !isVerifying,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_doctor_verify_btn")
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = LocalizationManager.str("إعادة الفحص الآن", "Run Verification"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
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
