# REEX IDE — Offline Flutter & Dart IDE for Android

**REEX IDE** is a native, offline-capable mobile Integrated Development Environment (IDE) built specifically for Android devices. It enables software engineers to write, analyze, navigate, package, and test real Flutter and Dart applications directly on Android phones and tablets without needing a constant internet connection.

## 🚀 Key Features

### 1. Flutter/Dart Code Editor
- **Full Syntax Highlighting:** Multi-color syntax highlighting for Dart keywords, types, Flutter widgets, annotations, strings, numbers, and comments.
- **Line Numbers Gutter:** Accurate line count with error/warning indicator bars.
- **Mobile Coding Toolbar:** Instant access accessory keyboard row: `{`, `}`, `(`, `)`, `[`, `]`, `;`, `:`, `<`, `>`, `=`, `"`, `'`, `_`, `/`, `+`, `-`, `$`, `TAB`.
- **Quick Actions:** Undo, Redo, Code Formatting (Dart style), Search & Replace, and Single-tap Comment toggle `//`.
- **Flutter Widget Snippets:** Quick insertion for `StatelessWidget`, `StatefulWidget`, `Scaffold`, `Column`, `Row`, and `ListView`.

### 2. Dart & Flutter Intelligence
- **Real-Time Diagnostics:** Syntax verification, bracket matching, unclosed strings, missing semicolons, deprecated API detection (`FlatButton`), and const constructor hints.
- **Interactive Problems Panel:** Filter issues by Errors, Warnings, and Hints. Tap any issue to automatically jump to the exact line and position in the editor.
- **Code Formatter:** Indentation and nesting alignment engine.

### 3. Project Manager & Templates
- Full disk project filesystem tree structure (`lib/`, `test/`, `assets/`, `android/`, `pubspec.yaml`, `analysis_options.yaml`, `README.md`).
- Offline templates:
  - **Counter App:** Standard interactive starter.
  - **Empty App:** Clean canvas with minimal boilerplate.
  - **Navigation & Tabs:** Multi-screen routing with NavigationBar.
  - **Local Storage:** Offline persistent key-value notes.
  - **HTTP REST Client:** Networking and asynchronous JSON parsing.
  - **State Management:** Clean architecture using Provider pattern.
- Export project as ZIP / Share directly from Android.

### 4. Real Terminal
- Process execution engine using Android Linux shell.
- Real-time output stream (stdout & stderr) with exit code indicators and process kill capability.
- Interactive commands:
  - `reex doctor`
  - `flutter pub get --offline`
  - `flutter analyze`
  - `flutter test`
  - `flutter build apk`
  - Shell commands (`ls -la`, `uname -a`, `pwd`, etc.)

### 5. SDK Doctor & Offline Verification
- Real device hardware telemetry: Architecture (`arm64-v8a`), Android version, API level, RAM, and Storage.
- Health checks for Flutter SDK, Dart SDK, Global Pub Cache, Android toolchain, and Gradle.
- Automated offline environment verification.

### 6. Pub Package Manager & Global Cache
- Pre-cached catalog of top Flutter packages (`provider`, `shared_preferences`, `http`, `flutter_bloc`, `path_provider`, `intl`, `flutter_svg`, `sqflite`, `dio`, `uuid`, `get_it`, `cached_network_image`).
- 1-click install/remove from `pubspec.yaml` with SHA-256 integrity verification.

### 7. Build System & GitHub Actions CI/CD
- Local APK compilation pipeline targeting `arm64-v8a` with live log streaming.
- SHA-256 calculation for produced APK binaries.
- Automated GitHub Actions workflow generation (`prepare-environment.yml` and `build-android.yml`).
