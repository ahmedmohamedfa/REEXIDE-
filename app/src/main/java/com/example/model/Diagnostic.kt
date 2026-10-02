package com.example.model

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
    HINT
}

data class Diagnostic(
    val id: String,
    val filePath: String,
    val fileName: String,
    val line: Int,
    val column: Int,
    val message: String,
    val code: String = "",
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
    val quickFix: String? = null
)
