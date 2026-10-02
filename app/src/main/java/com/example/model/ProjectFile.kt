package com.example.model

data class ProjectFile(
    val name: String,
    val path: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val children: List<ProjectFile> = emptyList(),
    val isExpanded: Boolean = false
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")

    val isDartFile: Boolean
        get() = extension.equals("dart", ignoreCase = true)

    val isYamlFile: Boolean
        get() = extension.equals("yaml", ignoreCase = true) || extension.equals("yml", ignoreCase = true)

    val isJsonFile: Boolean
        get() = extension.equals("json", ignoreCase = true)
}
