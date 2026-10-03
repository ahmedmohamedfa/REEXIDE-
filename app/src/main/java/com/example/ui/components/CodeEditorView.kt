package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Diagnostic
import com.example.model.DiagnosticSeverity
import com.example.service.LocalizationManager
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.SyntaxAnnotation
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType
import com.example.ui.theme.SyntaxWidget

class DartSyntaxHighlighter(private val searchQuery: String = "") : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = buildAnnotatedString {
            val content = text.text
            append(content)

            // Dart, Kotlin, Python, JS Keywords
            val keywordsRegex = Regex("\\b(class|extends|with|implements|import|export|library|part|void|var|final|const|static|late|async|await|yield|return|if|else|switch|case|default|break|continue|for|while|do|in|try|catch|finally|throw|rethrow|new|is|as|this|super|true|false|null|fun|val|package|def|function|let|from)\\b")
            for (match in keywordsRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }

            // Types regex
            val typesRegex = Regex("\\b(int|double|num|String|bool|List|Map|Set|Future|Stream|Widget|BuildContext|State|StatefulWidget|StatelessWidget|Key|Color|EdgeInsets|TextStyle|Duration|ThemeData|ColorScheme|Alignment|FontWeight|FontFamily|DateTime)\\b")
            for (match in typesRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxType, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
            }

            // Complete Flutter Widgets library regex
            val widgetRegex = Regex("\\b(MaterialApp|Scaffold|AppBar|Container|Column|Row|Stack|Wrap|Center|Align|Padding|SizedBox|Text|RichText|Icon|Image|ElevatedButton|TextButton|OutlinedButton|IconButton|FloatingActionButton|ListView|GridView|Card|Divider|Spacer|Drawer|BottomNavigationBar|NavigationBar|NavigationDestination|ListTile|CircleAvatar|Switch|Checkbox|Slider|TextField|TextFormField|DropdownButton|CircularProgressIndicator|LinearProgressIndicator|SingleChildScrollView|Expanded|Flexible|Badge|Hero|Opacity|Transform|AnimatedContainer|CustomScrollView|SliverAppBar|PageView)\\b")
            for (match in widgetRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxWidget, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
            }

            // YAML / JSON keys
            val yamlKeyRegex = Regex("^[\\s]*([a-zA-Z0-9_-]+):", RegexOption.MULTILINE)
            for (match in yamlKeyRegex.findAll(content)) {
                addStyle(SpanStyle(color = Color(0xFFBC8CFF), fontWeight = FontWeight.Bold), match.range.first, match.range.last)
            }

            // XML / HTML tags
            val xmlTagRegex = Regex("</?[a-zA-Z0-9_-]+(\\s|>)")
            for (match in xmlTagRegex.findAll(content)) {
                addStyle(SpanStyle(color = Color(0xFF7EE787), fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
            }

            // Annotations (@override, etc.)
            val annotationRegex = Regex("@[a-zA-Z0-9_]+")
            for (match in annotationRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxAnnotation), match.range.first, match.range.last + 1)
            }

            // Strings regex
            val stringRegex = Regex("('[^']*'|\"[^\"]*\")")
            for (match in stringRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxString), match.range.first, match.range.last + 1)
            }

            // Numbers regex
            val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
            for (match in numberRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxNumber), match.range.first, match.range.last + 1)
            }

            // Single line comments
            val commentRegex = Regex("//.*")
            for (match in commentRegex.findAll(content)) {
                addStyle(SpanStyle(color = SyntaxComment), match.range.first, match.range.last + 1)
            }

            // In-Editor Search Matches Highlighter
            if (searchQuery.isNotBlank() && searchQuery.length >= 2) {
                try {
                    val searchRegex = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE)
                    for (match in searchRegex.findAll(content)) {
                        addStyle(
                            SpanStyle(background = Color(0xFFFFD600), color = Color.Black, fontWeight = FontWeight.Bold),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                } catch (_: Exception) {}
            }
        }

        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}

@Composable
fun CodeEditorView(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    diagnostics: List<Diagnostic>,
    fontSizeSp: Int = 13,
    onFontSizeChange: (Int) -> Unit = {},
    isSearchVisible: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onCloseSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val highlighter = remember(searchQuery) { DartSyntaxHighlighter(searchQuery) }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = textFieldValue.text.lines()
    val lineCount = lines.size.coerceAtLeast(1)

    // Map error and warning line numbers
    val errorLines = remember(diagnostics) {
        diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }.map { it.line }.toSet()
    }
    val warningLines = remember(diagnostics) {
        diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }.map { it.line }.toSet()
    }

    val matchCount = remember(textFieldValue.text, searchQuery) {
        if (searchQuery.length >= 2) {
            try {
                Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).findAll(textFieldValue.text).count()
            } catch (_: Exception) {
                0
            }
        } else 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("code_editor_view")
    ) {
        // Search & Editor Settings Bar
        if (isSearchVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF8B949E))
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(LocalizationManager.str("بحث في الملف...", "Search in file..."), fontSize = 12.sp, color = Color(0xFF8B949E)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF58A6FF),
                        unfocusedBorderColor = DarkBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("editor_search_field")
                )

                if (matchCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$matchCount ${LocalizationManager.str("مطابقة", "matches")}",
                        color = Color(0xFFFFD600),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Font Size Zoom Controls
                IconButton(
                    onClick = { if (fontSizeSp > 10) onFontSizeChange(fontSizeSp - 1) },
                    modifier = Modifier.size(32.dp).testTag("editor_zoom_out")
                ) {
                    Icon(Icons.Default.TextDecrease, contentDescription = "Zoom Out", tint = Color(0xFFC9D1D9), modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "${fontSizeSp}sp",
                    color = Color(0xFF8B949E),
                    fontSize = 10.sp,
                    fontFamily = RobotoMonoFontFamily
                )

                IconButton(
                    onClick = { if (fontSizeSp < 24) onFontSizeChange(fontSizeSp + 1) },
                    modifier = Modifier.size(32.dp).testTag("editor_zoom_in")
                ) {
                    Icon(Icons.Default.TextIncrease, contentDescription = "Zoom In", tint = Color(0xFFC9D1D9), modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onCloseSearch) {
                    Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color(0xFF8B949E))
                }
            }
        }

        // Editor with Line Numbers Gutter
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .background(DarkSurface)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (errorLines.contains(i)) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .width(4.dp)
                                    .height(14.dp)
                                    .background(AccentRed, RoundedCornerShape(2.dp))
                            )
                        } else if (warningLines.contains(i)) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .width(4.dp)
                                    .height(14.dp)
                                    .background(AccentYellow, RoundedCornerShape(2.dp))
                            )
                        }
                        Text(
                            text = "$i",
                            color = when {
                                errorLines.contains(i) -> AccentRed
                                warningLines.contains(i) -> AccentYellow
                                else -> Color(0xFF484F58)
                            },
                            fontFamily = RobotoMonoFontFamily,
                            fontSize = fontSizeSp.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = (fontSizeSp + 7).sp
                        )
                    }
                }
            }

            // Vertical divider line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(DarkBorder)
            )

            // Actual Code Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = onValueChange,
                    visualTransformation = highlighter,
                    textStyle = TextStyle(
                        color = Color(0xFFE6EDF3),
                        fontFamily = RobotoMonoFontFamily,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp + 7).sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF58A6FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_editor_text_input")
                )
            }
        }
    }
}
