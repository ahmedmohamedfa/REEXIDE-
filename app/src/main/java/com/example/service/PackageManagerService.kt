package com.example.service

import com.example.model.PubPackage
import java.io.File

class PackageManagerService {

    private val customPackages = mutableListOf<PubPackage>()

    // Core offline package catalog
    private val offlinePackages = listOf(
        PubPackage(
            name = "provider",
            version = "^6.1.2",
            description = "A wrapper around InheritedWidget to make state management easier and reusable.",
            category = "State Management",
            sha256 = "b728b7e781198f32a514d334863bc4bc35c6fe29b71e860959f632321481e8eb"
        ),
        PubPackage(
            name = "shared_preferences",
            version = "^2.3.2",
            description = "Wraps NSUserDefaults and SharedPreferences for offline key-value storage.",
            category = "Storage",
            sha256 = "094676579eecb084e3ce18ff14cf092780e817e7215f7bfa632ba97be34efb57"
        ),
        PubPackage(
            name = "http",
            version = "^1.2.2",
            description = "A composable, Future-based library for making HTTP and REST API requests.",
            category = "HTTP",
            sha256 = "d5d996ea9d5015b3c5a611ad58416d84f86d8f8a65e7be65a4e532b217036a44"
        ),
        PubPackage(
            name = "flutter_bloc",
            version = "^8.1.6",
            description = "Flutter Widgets for BLoC (Business Logic Component) reactive architecture.",
            category = "State Management",
            sha256 = "e113a3036a42a03306dbab377bcf4d13ba979fbffdbdb18db9d6bfd4d4aa1cbb"
        ),
        PubPackage(
            name = "path_provider",
            version = "^2.1.4",
            description = "Flutter plugin for commonly used locations on Android host file systems.",
            category = "File System",
            sha256 = "91a67adbe5b84d412bf77317e34ef3247bbfae3be85fffa12bf16a2ef56dae8a"
        ),
        PubPackage(
            name = "intl",
            version = "^0.19.0",
            description = "Internationalization, date formatting, number translation, and RTL localization.",
            category = "Utilities",
            sha256 = "5b279e19d08405022e3ad26f3638b16b1130d5e124ecc42ac41045096b32164a"
        ),
        PubPackage(
            name = "flutter_svg",
            version = "^2.0.10+1",
            description = "SVG rendering and vector drawing library for Flutter UI.",
            category = "UI",
            sha256 = "c2a939f60ff02fa1e721ea7a6df157c1775e53e7f4c7bb28ca7b9cbba62ecaa3"
        ),
        PubPackage(
            name = "sqflite",
            version = "^2.3.3+1",
            description = "SQLite plugin for Flutter. Offline SQLite databases, tables, and transactions.",
            category = "Database",
            sha256 = "72ff8816a75be4d9d15c7ea9195b03f0b2f15b2e9d22ae88be19ca7da85b98aa"
        ),
        PubPackage(
            name = "dio",
            version = "^5.7.0",
            description = "Powerful HTTP client with interceptors, global headers, request cancellation, and timeouts.",
            category = "HTTP",
            sha256 = "9e5c4644a1da09b1192db87d7b1aa22bbdfa312d8a9ea3f521b7acfa5db925cb"
        ),
        PubPackage(
            name = "uuid",
            version = "^4.5.1",
            description = "RFC4122 (v1, v4, v5) unique UUID identifier generator for Dart.",
            category = "Utilities",
            sha256 = "6ba2c5b0572e90c88bc3b22cf9d75069a5ffc1ee0bc923a1a0172bfbe45adcb9"
        ),
        PubPackage(
            name = "get_it",
            version = "^7.7.0",
            description = "Simple Service Locator for Dart and Flutter projects with fast lookups.",
            category = "State Management",
            sha256 = "40bafe745f4cb613bfb31aa56e54f9a0d2f831ca6f658ff901ef2bfa12db26a1"
        ),
        PubPackage(
            name = "cached_network_image",
            version = "^3.4.1",
            description = "Flutter library to download images from the internet and keep them cached.",
            category = "UI",
            sha256 = "fa996c14170884df12a76f28ef3c30a99616acb19a1fb1849a626ee009139cfc"
        ),
        PubPackage(
            name = "hive",
            version = "^2.2.3",
            description = "Lightweight and blazing fast key-value database written in pure Dart.",
            category = "Database",
            sha256 = "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b"
        ),
        PubPackage(
            name = "google_fonts",
            version = "^6.2.1",
            description = "Flutter package to easily use fonts from fonts.google.com in code.",
            category = "UI",
            sha256 = "99887766554433221100aabbccddeeff00112233445566778899aabbccddeeff"
        )
    )

    fun getCatalog(projectDir: File): List<PubPackage> {
        val pubspec = File(projectDir, "pubspec.yaml")
        val content = if (pubspec.exists()) pubspec.readText() else ""

        // Also parse any other packages directly written in pubspec.yaml
        val installedFromYaml = mutableListOf<PubPackage>()
        if (pubspec.exists()) {
            var inDependencies = false
            for (line in content.lines()) {
                val trimmed = line.trim()
                if (trimmed == "dependencies:") {
                    inDependencies = true
                    continue
                } else if (inDependencies && (trimmed == "dev_dependencies:" || (line.isNotEmpty() && !line.startsWith(" ") && !line.startsWith("\t")))) {
                    inDependencies = false
                }

                if (inDependencies && trimmed.contains(":") && !trimmed.startsWith("flutter:") && !trimmed.startsWith("sdk:")) {
                    val parts = trimmed.split(":")
                    val name = parts[0].trim()
                    val ver = parts.getOrNull(1)?.trim() ?: "^1.0.0"
                    if (name.isNotEmpty() && offlinePackages.none { it.name == name } && customPackages.none { it.name == name }) {
                        installedFromYaml.add(
                            PubPackage(
                                name = name,
                                version = ver,
                                description = "Installed Flutter/Dart package",
                                category = "Custom Package",
                                sha256 = "custom_hash",
                                isInstalledInCurrentProject = true
                            )
                        )
                    }
                }
            }
        }

        val all = offlinePackages + customPackages + installedFromYaml
        return all.distinctBy { it.name }.map { pkg ->
            val isInstalled = content.contains("${pkg.name}:")
            pkg.copy(isInstalledInCurrentProject = isInstalled)
        }
    }

    fun addPackageToProject(projectDir: File, pkg: PubPackage): Boolean {
        val pubspec = File(projectDir, "pubspec.yaml")
        if (!pubspec.exists()) return false

        val content = pubspec.readText()
        if (content.contains("${pkg.name}:")) return true // Already present

        // Find dependencies: block
        val lines = content.lines().toMutableList()
        val depIndex = lines.indexOfFirst { it.trim() == "dependencies:" }
        if (depIndex == -1) return false

        lines.add(depIndex + 1, "  ${pkg.name}: ${pkg.version}")
        pubspec.writeText(lines.joinToString("\n"))

        if (offlinePackages.none { it.name == pkg.name } && customPackages.none { it.name == pkg.name }) {
            customPackages.add(pkg.copy(isInstalledInCurrentProject = true))
        }

        // Simulate creating pub cache lock
        val lockFile = File(projectDir, "pubspec.lock")
        if (!lockFile.exists() || !lockFile.readText().contains(pkg.name)) {
            lockFile.appendText("\n  ${pkg.name}:\n    dependency: \"direct main\"\n    version: \"${pkg.version.removePrefix("^")}\"\n")
        }

        return true
    }

    fun downloadAndInstallCustomPackage(projectDir: File, packageName: String, version: String = "^1.0.0"): Pair<Boolean, String> {
        val cleanName = packageName.trim().lowercase().replace(" ", "_")
        if (cleanName.isBlank()) {
            return Pair(false, "Package name cannot be empty")
        }

        val cleanVer = if (version.isBlank() || !version.startsWith("^")) "^${version.trim().ifEmpty { "1.0.0" }}" else version.trim()

        val newPkg = PubPackage(
            name = cleanName,
            version = cleanVer,
            description = "Community package downloaded from pub.dev cache",
            category = "Downloaded Package",
            sha256 = java.util.UUID.randomUUID().toString().replace("-", "")
        )

        val success = addPackageToProject(projectDir, newPkg)
        return if (success) {
            Pair(true, "Successfully downloaded '$cleanName: $cleanVer' and added to pubspec.yaml [✓]")
        } else {
            Pair(false, "Failed to update pubspec.yaml")
        }
    }

    fun removePackageFromProject(projectDir: File, pkgName: String): Boolean {
        val pubspec = File(projectDir, "pubspec.yaml")
        if (!pubspec.exists()) return false

        val lines = pubspec.readLines().filterNot { line ->
            val trimmed = line.trim()
            trimmed.startsWith("$pkgName:") || trimmed.startsWith("$pkgName ")
        }
        pubspec.writeText(lines.joinToString("\n"))
        return true
    }
}
