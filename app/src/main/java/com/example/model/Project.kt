package com.example.model

enum class ProjectTemplate(val displayName: String, val description: String, val iconName: String) {
    COUNTER("Flutter Counter App", "Default Flutter starter project with interactive counter", "add_circle"),
    EMPTY("Empty App", "Minimal Flutter application with clean main.dart", "check_box_outline_blank"),
    NAVIGATION("Tabs & Navigation", "Multi-screen application with BottomNavigationBar and routing", "tab"),
    LOCAL_STORAGE("Local Storage App", "Offline data persistence using SharedPreferences & JSON", "storage"),
    HTTP_CLIENT("HTTP & REST API", "Networking, async JSON parsing, and state handling", "cloud_download"),
    STATE_MANAGEMENT("State Management (Provider)", "Structured clean architecture with Provider state flow", "account_tree")
}

data class Project(
    val id: String,
    val name: String,
    val path: String,
    val template: ProjectTemplate = ProjectTemplate.COUNTER,
    val flutterVersion: String = "3.24.3",
    val dartVersion: String = "3.5.3",
    val lastModified: Long = System.currentTimeMillis(),
    val isOfflineReady: Boolean = true,
    val targetSdk: Int = 34,
    val gitBranch: String = "main"
)
