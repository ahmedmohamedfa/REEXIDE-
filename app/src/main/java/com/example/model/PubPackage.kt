package com.example.model

data class PubPackage(
    val name: String,
    val version: String,
    val description: String,
    val category: String,
    val isOfflineCached: Boolean = true,
    val sha256: String,
    val dependencies: List<String> = emptyList(),
    val isInstalledInCurrentProject: Boolean = false
)
