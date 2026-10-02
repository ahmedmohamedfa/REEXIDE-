package com.example.service

import com.example.model.Diagnostic
import com.example.model.DiagnosticSeverity
import java.util.Stack
import java.util.UUID

class DartAnalyzerService {

    fun analyze(filePath: String, fileName: String, code: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = code.lines()

        // 1. Check for Bracket & Parentheses Matching
        val bracketStack = Stack<BracketToken>()
        for ((lineIdx, lineText) in lines.withIndex()) {
            var inSingleQuote = false
            var inDoubleQuote = false
            var isComment = false

            for (colIdx in lineText.indices) {
                val c = lineText[colIdx]

                if (colIdx < lineText.length - 1 && lineText[colIdx] == '/' && lineText[colIdx + 1] == '/') {
                    isComment = true
                    break
                }

                if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote
                if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote

                if (!inSingleQuote && !inDoubleQuote && !isComment) {
                    when (c) {
                        '{', '(', '[' -> bracketStack.push(BracketToken(c, lineIdx + 1, colIdx + 1))
                        '}' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().char != '{') {
                                diagnostics.add(
                                    Diagnostic(
                                        id = UUID.randomUUID().toString(),
                                        filePath = filePath,
                                        fileName = fileName,
                                        line = lineIdx + 1,
                                        column = colIdx + 1,
                                        message = "Unexpected closing brace '}'",
                                        code = "unmatched_bracket",
                                        severity = DiagnosticSeverity.ERROR
                                    )
                                )
                            } else {
                                bracketStack.pop()
                            }
                        }
                        ')' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().char != '(') {
                                diagnostics.add(
                                    Diagnostic(
                                        id = UUID.randomUUID().toString(),
                                        filePath = filePath,
                                        fileName = fileName,
                                        line = lineIdx + 1,
                                        column = colIdx + 1,
                                        message = "Unexpected closing parenthesis ')'",
                                        code = "unmatched_parenthesis",
                                        severity = DiagnosticSeverity.ERROR
                                    )
                                )
                            } else {
                                bracketStack.pop()
                            }
                        }
                        ']' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().char != '[') {
                                diagnostics.add(
                                    Diagnostic(
                                        id = UUID.randomUUID().toString(),
                                        filePath = filePath,
                                        fileName = fileName,
                                        line = lineIdx + 1,
                                        column = colIdx + 1,
                                        message = "Unexpected closing bracket ']'",
                                        code = "unmatched_bracket",
                                        severity = DiagnosticSeverity.ERROR
                                    )
                                )
                            } else {
                                bracketStack.pop()
                            }
                        }
                    }
                }
            }
        }

        while (bracketStack.isNotEmpty()) {
            val unclosed = bracketStack.pop()
            diagnostics.add(
                Diagnostic(
                    id = UUID.randomUUID().toString(),
                    filePath = filePath,
                    fileName = fileName,
                    line = unclosed.line,
                    column = unclosed.column,
                    message = "Unclosed '${unclosed.char}'. Missing matching closing bracket.",
                    code = "missing_matching_bracket",
                    severity = DiagnosticSeverity.ERROR,
                    quickFix = "Insert matching closing bracket"
                )
            )
        }

        // 2. Line by Line Dart Syntax Inspections
        var insideClass = false
        var className = ""
        var hasBuildMethod = false

        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = line.trim()

            // Skip comments and empty lines
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                continue
            }

            // Detect class definition
            if (trimmed.startsWith("class ")) {
                insideClass = true
                className = trimmed.substringAfter("class ").substringBefore(" ").substringBefore("{")
                hasBuildMethod = false
            }

            if (insideClass && trimmed.contains("Widget build(BuildContext context)")) {
                hasBuildMethod = true
            }

            // Check missing semicolon on statements
            val statementStarters = listOf("final ", "var ", "int ", "String ", "double ", "bool ", "return ", "throw ", "print(")
            val requiresSemicolon = statementStarters.any { trimmed.startsWith(it) }
            if (requiresSemicolon) {
                if (!trimmed.endsWith(";") && !trimmed.endsWith("{") && !trimmed.endsWith(",") && !trimmed.endsWith("(") && !trimmed.endsWith("+") && !trimmed.endsWith("?")) {
                    // Check if next line continues or is end
                    val isContinued = idx < lines.size - 1 && (lines[idx + 1].trim().startsWith(".") || lines[idx + 1].trim().startsWith(")") || lines[idx + 1].trim().startsWith("?"))
                    if (!isContinued) {
                        diagnostics.add(
                            Diagnostic(
                                id = UUID.randomUUID().toString(),
                                filePath = filePath,
                                fileName = fileName,
                                line = lineNum,
                                column = line.length,
                                message = "Expected ';' after statement.",
                                code = "missing_semicolon",
                                severity = DiagnosticSeverity.ERROR,
                                quickFix = "Add ';'"
                            )
                        )
                    }
                }
            }

            // Warn on print() in production
            if (trimmed.startsWith("print(") || trimmed.contains(" print(")) {
                diagnostics.add(
                    Diagnostic(
                        id = UUID.randomUUID().toString(),
                        filePath = filePath,
                        fileName = fileName,
                        line = lineNum,
                        column = line.indexOf("print") + 1,
                        message = "Avoid 'print' calls in production code. Prefer 'debugPrint' or logger.",
                        code = "avoid_print",
                        severity = DiagnosticSeverity.INFO,
                        quickFix = "Replace with debugPrint"
                    )
                )
            }

            // Warn on deprecated FlatButton or RaisedButton
            if (trimmed.contains("FlatButton") || trimmed.contains("RaisedButton")) {
                diagnostics.add(
                    Diagnostic(
                        id = UUID.randomUUID().toString(),
                        filePath = filePath,
                        fileName = fileName,
                        line = lineNum,
                        column = 1,
                        message = "'FlatButton' and 'RaisedButton' are deprecated in Flutter. Use 'TextButton' or 'ElevatedButton'.",
                        code = "deprecated_member_use",
                        severity = DiagnosticSeverity.WARNING,
                        quickFix = "Replace with ElevatedButton"
                    )
                )
            }

            // Check for const constructor opportunities
            if ((trimmed.startsWith("Text(") || trimmed.startsWith("Icon(") || trimmed.startsWith("SizedBox(")) && !trimmed.startsWith("const ")) {
                if (!trimmed.contains("${'$'}") && !trimmed.contains("widget.") && !trimmed.contains("_")) {
                    diagnostics.add(
                        Diagnostic(
                            id = UUID.randomUUID().toString(),
                            filePath = filePath,
                            fileName = fileName,
                            line = lineNum,
                            column = line.indexOfFirst { !it.isWhitespace() } + 1,
                            message = "Prefer 'const' with constant constructors for improved rendering performance.",
                            code = "prefer_const_constructors",
                            severity = DiagnosticSeverity.HINT,
                            quickFix = "Add const keyword"
                        )
                    )
                }
            }
        }

        return diagnostics.sortedBy { it.line }
    }

    private data class BracketToken(val char: Char, val line: Int, val column: Int)
}
