package com.example.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Project
import com.example.service.AiProjectAgentService
import com.example.service.ChatMessage
import com.example.service.LocalizationManager
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AiCopilotDialog(
    aiService: AiProjectAgentService,
    project: Project,
    currentFile: File,
    currentCode: String,
    diagnosticsSummary: String,
    onApplyCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val messages by aiService.messages.collectAsState()
    val isLoading by aiService.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var showKeySettings by remember { mutableStateOf(false) }
    var keyInputValue by remember { mutableStateOf(aiService.customApiKey) }
    var isTestingKey by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var keySavedNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("ai_copilot_dialog"),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = LocalizationManager.str("مساعد REEX الذكي", "REEX AI Copilot"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${aiService.getApiKeySourceDescription()} • ${currentFile.name}",
                                    color = if (aiService.hasValidApiKey()) AccentGreen else CyanPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    showKeySettings = !showKeySettings
                                    testResult = null
                                    keySavedNotice = null
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (showKeySettings) CyanPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .testTag("btn_toggle_key_settings")
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Key Settings",
                                    tint = if (aiService.hasValidApiKey()) AccentGreen else CyanPrimary
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8B949E))
                            }
                        }
                    }

                    // API Key Settings Expandable Panel
                    if (showKeySettings) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground)
                                .border(1.dp, CyanPrimary.copy(alpha = 0.3f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LocalizationManager.str("🔑 إعدادات وتفعيل مفتاح Gemini API", "🔑 Gemini API Key Settings"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = aiService.getApiKeySourceDescription(),
                                    color = AccentGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = LocalizationManager.str(
                                    "يمكنك لصق مفتاح Gemini الخاص بك لتوليد الأكواد وإصلاح الأخطاء مباشرة من Google AI، أو تركه فارغاً لاستخدام المحرك الداخلي الذكي.",
                                    "Paste your Gemini API key to enable cloud AI coding, or leave blank to use the built-in smart assistant."
                                ),
                                color = Color(0xFF8B949E),
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = keyInputValue,
                                onValueChange = {
                                    keyInputValue = it
                                    testResult = null
                                    keySavedNotice = null
                                },
                                placeholder = {
                                    Text("AIzaSy...", color = Color(0xFF8B949E), fontSize = 11.sp)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_key_input_field")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        aiService.customApiKey = keyInputValue
                                        keySavedNotice = LocalizationManager.str("تم حفظ المفتاح بنجاح [✓]", "Key saved successfully [✓]")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("btn_save_gemini_key")
                                ) {
                                    Text(
                                        text = LocalizationManager.str("حفظ المفتاح", "Save Key"),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            isTestingKey = true
                                            testResult = null
                                            keySavedNotice = null
                                            testResult = aiService.testApiKeyConnection(keyInputValue)
                                            isTestingKey = false
                                        }
                                    },
                                    enabled = !isTestingKey && keyInputValue.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("btn_test_gemini_key")
                                ) {
                                    if (isTestingKey) {
                                        CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = LocalizationManager.str("اختبار الاتصال", "Test Connection"),
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }

                                if (aiService.customApiKey.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            aiService.customApiKey = ""
                                            keyInputValue = ""
                                            testResult = null
                                            keySavedNotice = LocalizationManager.str("تمت استعادة الوضع الافتراضي", "Reset to default")
                                        },
                                        modifier = Modifier.size(36.dp).testTag("btn_clear_gemini_key")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = AccentRed)
                                    }
                                }
                            }

                            // Notice or Test result display
                            if (keySavedNotice != null) {
                                Text(
                                    text = keySavedNotice ?: "",
                                    color = AccentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            if (testResult != null) {
                                val (success, msg) = testResult!!
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (success) AccentGreen else AccentRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = msg,
                                        color = if (success) AccentGreen else AccentRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Suggested Quick Actions Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val quickPrompts = listOf(
                            LocalizationManager.str("⚡ إصلاح الأخطاء في الكود", "⚡ Fix Code Errors") to "Fix all errors and clean up the active file code.",
                            LocalizationManager.str("🔐 شاشة تسجيل دخول كاملة", "🔐 Full Login Screen") to "Create a complete modern Flutter Login screen with validation.",
                            LocalizationManager.str("📋 قائمة مهام تفاعلية", "📋 Interactive Todo Screen") to "Build an interactive Todo list screen with add and delete capabilities.",
                            LocalizationManager.str("🎨 تفعيل الثيم والمظهر", "🎨 Improve UI Theme") to "Make this app look modern with Material 3 styling, rounded buttons and cyan accents."
                        )

                        quickPrompts.forEach { (label, prompt) ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable(enabled = !isLoading) {
                                        scope.launch {
                                            aiService.sendMessage(prompt, project, currentFile, currentCode, diagnosticsSummary)
                                        }
                                    }
                            ) {
                                Text(
                                    text = label,
                                    color = Color(0xFFC9D1D9),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Chat messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            AiMessageBubble(
                                message = msg,
                                onApply = onApplyCode
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = CyanPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = LocalizationManager.str("الذكاء الاصطناعي يقوم بالتحليل والبرمجة...", "REEX AI is thinking and writing code..."),
                                        color = Color(0xFF8B949E),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Input Field & Send Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = {
                                Text(
                                    text = LocalizationManager.str("اطلب أي شيء: 'أضف شاشة حساب'، 'أصلح الكود'...", "Ask anything: 'Add profile screen', 'Fix errors'..."),
                                    color = Color(0xFF8B949E),
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = DarkBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_prompt_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val trimmed = inputPrompt.trim()
                                if (trimmed.isNotEmpty() && !isLoading) {
                                    inputPrompt = ""
                                    scope.launch {
                                        aiService.sendMessage(trimmed, project, currentFile, currentCode, diagnosticsSummary)
                                    }
                                }
                            },
                            enabled = inputPrompt.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (inputPrompt.isNotBlank() && !isLoading) CyanPrimary else DarkBorder)
                                .testTag("btn_send_ai_prompt")
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (inputPrompt.isNotBlank() && !isLoading) Color.Black else Color(0xFF8B949E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: ChatMessage,
    onApply: (String) -> Unit
) {
    val isUser = message.sender == ChatMessage.Sender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.2f))
                    .border(1.dp, CyanPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUser) Color(0xFF1E3A5F) else DarkBackground)
                .border(1.dp, if (isUser) Color(0xFF388BFD) else DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = Color(0xFFECEFF4),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (message.suggestedCode != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Code Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F141C))
                            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TARGET: ${message.targetFile ?: "lib/main.dart"}",
                                    color = Color(0xFF8B949E),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = { onApply(message.suggestedCode) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("apply_ai_code_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = LocalizationManager.str("تطبيق في المحرر", "Apply to Code"),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = message.suggestedCode.take(240) + if (message.suggestedCode.length > 240) "\n// ... [${message.suggestedCode.lines().size} lines]" else "",
                                color = CyanPrimary,
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
