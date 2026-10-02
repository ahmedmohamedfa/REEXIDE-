package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant

@Composable
fun CodingToolbar(
    onInsertSymbol: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFormat: () -> Unit,
    onFindToggle: () -> Unit,
    onToggleComment: () -> Unit,
    onInsertSnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    val symbols = listOf(
        "{", "}", "(", ")", "[", "]", ";", ":", "<", ">", "=", "\"", "'",
        "!", "?", "_", "/", "\\", "|", "&", "+", "-", "$", "@", ",", ".", "TAB"
    )

    val snippets = listOf(
        "stless" to "StatelessWidget",
        "stful" to "StatefulWidget",
        "scaffold" to "Scaffold",
        "column" to "Column",
        "row" to "Row",
        "listview" to "ListView.builder",
        "gridview" to "GridView.count",
        "card" to "Card",
        "container" to "Container",
        "textfield" to "TextField",
        "future" to "FutureBuilder",
        "stream" to "StreamBuilder"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState())
            .testTag("coding_toolbar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick Action Buttons
        ToolbarIconButton(
            icon = { Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color(0xFFC9D1D9)) },
            tag = "toolbar_undo",
            onClick = { vibrate(); onUndo() }
        )

        ToolbarIconButton(
            icon = { Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color(0xFFC9D1D9)) },
            tag = "toolbar_redo",
            onClick = { vibrate(); onRedo() }
        )

        ToolbarIconButton(
            icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = "Format Code", tint = Color(0xFF58A6FF)) },
            tag = "toolbar_format",
            onClick = { vibrate(); onFormat() }
        )

        ToolbarIconButton(
            icon = { Icon(Icons.Default.Search, contentDescription = "Find", tint = Color(0xFFC9D1D9)) },
            tag = "toolbar_find",
            onClick = { vibrate(); onFindToggle() }
        )

        ToolbarKeyButton(
            text = "//",
            tag = "toolbar_comment",
            highlight = true,
            onClick = { vibrate(); onToggleComment() }
        )

        // Symbols Bar
        symbols.forEach { sym ->
            ToolbarKeyButton(
                text = sym,
                tag = "key_$sym",
                onClick = {
                    vibrate()
                    if (sym == "TAB") onInsertSymbol("  ") else onInsertSymbol(sym)
                }
            )
        }

        // Snippets
        snippets.forEach { (key, label) ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1F6FEB))
                    .clickable {
                        vibrate()
                        onInsertSnippet(key)
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag("snippet_$key"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: @Composable () -> Unit,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(38.dp)
            .widthIn(min = 38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun ToolbarKeyButton(
    text: String,
    tag: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(38.dp)
            .widthIn(min = 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (highlight) DarkBorder else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (highlight) Color(0xFF58A6FF) else Color(0xFFE6EDF3),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
