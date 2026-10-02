package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricAccent
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val glowAlpha = remember { Animatable(0.3f) }
    var terminalOutput by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentStep by remember { mutableStateOf("Initializing REEX IDE Engine...") }
    var progress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        glowAlpha.animateTo(
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(250)
        terminalOutput = listOf("void main() => runApp(const ReexIDE());")
        progress = 0.25f

        delay(350)
        currentStep = "Mounting Local Dart SDK 3.5.3..."
        terminalOutput = terminalOutput + "class _ReexRuntime extends StatefulWidget {"
        progress = 0.45f

        delay(350)
        currentStep = "Checking ABI: arm64-v8a supported [✓]"
        terminalOutput = terminalOutput + "  final String targetAbi = 'arm64-v8a';"
        progress = 0.65f

        delay(350)
        currentStep = "Loading Global Pub Cache (12 packages)..."
        terminalOutput = terminalOutput + "  @override Widget build(BuildContext context) => Scaffold();"
        progress = 0.85f

        delay(350)
        currentStep = "OFFLINE ENVIRONMENT READY [✓]"
        terminalOutput = terminalOutput + "  // IDE Environment Initialized Successfully"
        progress = 1f

        delay(400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Futuristic 'R' Logo with Coding Braces
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale.value)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricAccent, CyanPrimary)
                        )
                    )
                    .border(2.dp, CyanPrimary.copy(alpha = glowAlpha.value), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "{",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "R",
                        color = Color.White,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "REEX IDE",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Offline Flutter & Dart Development Environment",
                fontSize = 13.sp,
                color = CyanPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Code stream console preview during launch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "INITIALIZING CORE",
                            fontSize = 11.sp,
                            color = Color(0xFF8B949E),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentGreen)
                        )
                    }

                    terminalOutput.forEach { line ->
                        Text(
                            text = line,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (line.startsWith("//")) AccentGreen else Color(0xFF79C0FF),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CyanPrimary,
                trackColor = Color(0xFF21262D)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentStep,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF8B949E)
            )
        }
    }
}
