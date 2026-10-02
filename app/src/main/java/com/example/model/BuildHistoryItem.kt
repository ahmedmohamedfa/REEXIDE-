package com.example.model

enum class BuildStatus {
    QUEUED,
    BUILDING,
    SUCCESS,
    FAILED
}

data class BuildHistoryItem(
    val id: String,
    val projectName: String,
    val buildType: String = "debug", // debug / release
    val architecture: String = "arm64-v8a",
    val status: BuildStatus = BuildStatus.SUCCESS,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 18,
    val apkPath: String = "",
    val apkSizeBytes: Long = 0,
    val sha256: String = "",
    val logOutput: String = ""
)
