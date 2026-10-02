# REEX IDE Architecture

## Core Architectural Layers

REEX IDE is organized into clean, modular layers following Clean Architecture principles:

```
app/src/main/java/com/example/
├── model/
│   ├── Project.kt              # Project entity, templates, metadata
│   ├── ProjectFile.kt          # Virtual & filesystem node model
│   ├── Diagnostic.kt           # Diagnostics & code issues model
│   ├── PubPackage.kt           # Pub packages entity & SHA-256 metadata
│   ├── BuildHistoryItem.kt     # Build pipeline results & logs
│   └── DoctorCheck.kt          # Telemetry & toolchain status
│
├── service/
│   ├── ProjectManager.kt       # Filesystem layout, templates, ZIP exporter
│   ├── DartAnalyzerService.kt  # Syntax analysis, bracket verification, linter
│   ├── CodeFormatterService.kt # Real-time Dart code formatting engine
│   ├── TerminalService.kt      # Process execution, streams, and command dispatcher
│   ├── PackageManagerService.kt# Global Pub Cache & pubspec.yaml editor
│   ├── BuildManagerService.kt  # APK compilation pipeline & SHA-256 calculation
│   ├── SdkDoctorService.kt     # OS & hardware telemetry, diagnostic checks
│   └── GitService.kt           # Git history, commits, status tracking
│
├── ui/
│   ├── theme/                  # Obsidian dark & light M3 theme and syntax colors
│   ├── components/             # Reusable UI widgets:
│   │   ├── SplashScreen.kt     # Opening animated code stream & 'R' branding
│   │   ├── CodeEditorView.kt   # Highlighting, line numbers, and search
│   │   ├── CodingToolbar.kt    # Accessory mobile keyboard bar with haptics
│   │   ├── FileTreeView.kt     # Expandable explorer & file operations
│   │   ├── TerminalView.kt     # Monospace console with command prompt
│   │   ├── ProblemsView.kt     # Interactive issues list with jump-to-line
│   │   ├── CreateProjectDialog.kt # Template picker modal
│   │   ├── DoctorDialog.kt     # Toolchain health checklist
│   │   ├── BuildDialog.kt      # APK builder & CI/CD workflows
│   │   ├── PackageCatalogDialog.kt # Offline package manager
│   │   └── GitDialog.kt        # Version control manager
│   └── screens/
│       ├── HomeScreen.kt       # Projects dashboard & SDK status
│       └── IdeScreen.kt        # Multi-tab IDE with drawer & bottom panels
│
└── MainActivity.kt             # Application entry point with Compose edge-to-edge
```
