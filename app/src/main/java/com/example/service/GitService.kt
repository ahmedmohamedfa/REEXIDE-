package com.example.service

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class GitCommit(
    val hash: String,
    val author: String,
    val date: String,
    val message: String
)

class GitService {

    fun getCommits(projectDir: File): List<GitCommit> {
        val gitDir = File(projectDir, ".git")
        val commitLogFile = File(gitDir, "commits.log")
        if (!commitLogFile.exists()) {
            return listOf(
                GitCommit(
                    hash = "a1b2c3d",
                    author = "REEX Developer <dev@reex.local>",
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(projectDir.lastModified())),
                    message = "Initial Flutter project commit (REEX IDE)"
                )
            )
        }

        return commitLogFile.readLines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 4) {
                GitCommit(parts[0], parts[1], parts[2], parts[3])
            } else null
        }.reversed()
    }

    fun commit(projectDir: File, message: String): GitCommit {
        val gitDir = File(projectDir, ".git").apply { mkdirs() }
        val commitLogFile = File(gitDir, "commits.log")

        val hash = UUID.randomUUID().toString().substring(0, 7)
        val author = "REEX Developer <dev@reex.local>"
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val entry = "$hash|$author|$date|$message\n"
        commitLogFile.appendText(entry)

        return GitCommit(hash, author, date, message)
    }

    fun getStatus(projectDir: File): List<String> {
        val modified = mutableListOf<String>()
        val mainDart = File(projectDir, "lib/main.dart")
        if (mainDart.exists()) {
            modified.add("modified:   lib/main.dart")
        }
        val pubspec = File(projectDir, "pubspec.yaml")
        if (pubspec.exists()) {
            modified.add("modified:   pubspec.yaml")
        }
        return modified
    }
}
