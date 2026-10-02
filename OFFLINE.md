# REEX IDE Offline Operation & Cache Architecture

## Offline Principle
REEX IDE is engineered from the ground up to operate completely offline without active internet connectivity. All critical developer workflows—including creating projects from templates, editing Dart code, syntax analysis, diagnostics, package resolution, local APK packaging, and Git version control—function completely on device.

### 1. Global Pub Cache
Rather than downloading packages for every project over the internet, REEX IDE uses a **Shared Global Cache**:
- `provider`: State management
- `shared_preferences`: Key-value storage
- `http`: REST networking
- `flutter_bloc`: BLoC reactive pattern
- `path_provider`: Device filesystem access
- `intl`: Localization and formatting
- `flutter_svg`: Vector graphics rendering
- `sqflite`: Embedded SQLite database
- `dio`: Advanced HTTP networking
- `uuid`: Unique identifier generator
- `get_it`: Dependency injection & service locator
- `cached_network_image`: Image caching engine

### 2. Offline Verification Steps
When running `REEX Doctor`:
1. Architecture verification (`arm64-v8a`).
2. Storage & RAM capacity check.
3. Flutter SDK & Dart SDK offline bundle check.
4. Global Pub Cache readiness verification.
5. Local APK signing keystore check.
