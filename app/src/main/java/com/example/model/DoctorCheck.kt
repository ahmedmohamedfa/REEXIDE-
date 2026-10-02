package com.example.model

enum class CheckStatus {
    PASSED,
    WARNING,
    FAILED,
    CHECKING
}

data class DoctorCheck(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: CheckStatus,
    val details: String,
    val fixSuggestion: String? = null
)
