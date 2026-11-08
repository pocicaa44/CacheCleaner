# Cache Cleaner
## Product Requirements Document (PRD) & Technical Specification

**Document Version:** 1.0  
**Date:** November 7, 2026  
**Project Name:** Cache Cleaner  
**Platform:** Android  
**Development Approach:** Native Android (Kotlin + Jetpack Compose)  
**Status:** Canonical Project Specification  

---

# PART I — PRODUCT REQUIREMENTS DOCUMENT

## 1. Product Overview

### 1.1 Description
**Cache Cleaner** is a native Android utility application engineered to inspect, monitor, and clean application cache files across installed Android packages safely and transparently.

The application displays an inventory of installed applications along with detailed storage metrics, including application binary size, user data size, and cache size. Users can select individual applications for cache deletion or execute controlled batch-cleaning operations.

Cache Cleaner supports three distinct operational access modes:
1. **Standard Mode**: Utilizes standard non-root Android platform APIs (Storage Access and System Settings redirection).
2. **Shizuku Mode**: Leverages elevated ADB shell or root privileges via the Shizuku IPC framework without tethering to a computer.
3. **Root Mode**: Executes privileged operations directly via superuser (`su`) access on rooted devices.

### 1.2 Product Vision
Deliver a lightweight, performant, transparent, and completely safe Android cache-cleaning utility free from ads, telemetry, bloatware, or unrelated gimmick features (such as fake RAM boosters or battery coolers).

### 1.3 Target Users
- Standard Android users on non-rooted devices.
- Power users running the Shizuku environment.
- Rooted Android users with direct superuser permissions.
- Developers and users seeking transparent device storage insights.

---

## 2. Product Goals

The core development objectives are:
1. Enumerate and display all identifiable installed applications on the device.
2. Query and surface accurate storage consumption statistics for each application.
3. Provide single-application cache-cleaning execution.
4. Provide batch cache-cleaning execution with progress tracking.
5. Provide automated cache cleaning for non-root devices via Shizuku.
6. Provide direct privileged cleaning on rooted devices.
7. **Absolute Safety Mandate**: Ensure cache-cleaning operations NEVER delete user data, account credentials, or application databases.
8. Maintain minimum APK size and zero persistent background resource overhead.

---

## 3. Product Scope

### 3.1 Core Features Matrix

| Feature ID | Feature Name | Priority | Description |
|---|---|---|---|
| **F-01** | App Scanner | Must Have | Discovers installed applications, package names, labels, and icons. |
| **F-02** | Storage Statistics | Must Have | Retrieves app binary, user data, and cache sizes via `StorageStatsManager`. |
| **F-03** | Individual Cache Cleaning | Must Have | Cleans cache for a single chosen application. |
| **F-04** | Batch Cache Cleaning | Must Have | Queues and cleans cache across multiple selected applications. |
| **F-05** | Standard Access Mode | Must Have | Fallback for devices without elevated privileges. |
| **F-06** | Shizuku Integration | Must Have | Connects to Shizuku service for ADB-level cleaning commands. |
| **F-07** | Root Integration | Must Have | Executes privileged cleaning routines via `su`. |
| **F-08** | Access Status Detection | Must Have | Detects and displays active access capability (Standard, Shizuku, Root). |
| **F-09** | Search and Sort Applications | Should Have | Filters apps by name/package and sorts by cache size or alphabetical order. |
| **F-10** | System App Filter | Should Have | Toggles visibility between user applications and system applications. |
| **F-11** | Cleaning Progress | Should Have | Displays granular real-time progress during batch cleaning. |
| **F-12** | Cleaning Result Summary | Should Have | Reports verified freed storage and per-app operation status. |
| **F-13** | Dynamic Theme Support | Should Have | Seamlessly toggles System Default, Light Mode, and Dark Mode. |

### 3.2 Out of Scope
The following features are strictly prohibited from the project scope:
- RAM cleaners, task killers, or process termination utilities.
- CPU coolers or fake thermal throttling controllers.
- Battery savers or charging speed optimizers.
- Antivirus, anti-malware, or heuristic security scanners.
- Generic residual junk file scanners.
- Duplicate photo/file finders.
- Persistent background polling services or automated scheduled cleaners.
- Cloud synchronization, user authentication, or online accounts.
- Third-party advertisement SDKs or tracking telemetry.

---

## 4. Functional Requirements

### 4.1 Application Scanner
The scanner MUST:
- Retrieve all installed application packages accessible to the application.
- Resolve user-facing application labels via `PackageManager`.
- Retrieve unique application package names (`packageName`).
- Load official application icons asynchronously.
- Differentiate between User applications (`ApplicationInfo.FLAG_SYSTEM == 0`) and System applications.
- Identify unavailable, disabled, or archived packages.
- Support sorting by application label (A–Z, Z–A) and cache size (descending, ascending).
- **Self-Exclusion**: Cache Cleaner MUST automatically exclude its own package from the target cleaning list.

### 4.2 Storage Statistics
The application MUST query storage statistics using `StorageStatsManager`:
- **App Size**: Space consumed by APK files and native libraries.
- **Data Size**: Space consumed by internal/external private user data directories.
- **Cache Size**: Space consumed strictly by application cache directories (`cacheDir`, `externalCacheDir`, `code_cache`).
- **Total Size**: Cumulative storage footprint reported by the operating system.

Formatting guidelines:
- Byte values MUST be formatted using standard binary units: B, KB, MB, GB.
- If storage statistics cannot be retrieved due to missing permissions or OS restrictions, the UI MUST display an explicit `Unavailable` state.
- The UI MUST NEVER display simulated `0 B` values for unqueried applications without clear explanatory subtext.

### 4.3 Individual Cache Cleaning
The individual cleaning flow MUST adhere to the following sequence:
1. User selects an application from the inventory list or details screen.
2. The UI displays the estimated cleanable cache size.
3. User triggers the "Clear Cache" action.
4. The system validates required permissions and executes the operation through the active access provider.
5. The system performs post-operation verification to record storage delta.
6. The UI surfaces the outcome: `Success`, `Failed`, `Unsupported`, `Permission Denied`, or `Skipped`.

### 4.4 Batch Cache Cleaning
The batch cleaning flow MUST adhere to the following sequence:
- Support multi-selection of individual applications and a "Select All" toggle.
- Display a real-time aggregated estimate of total cache to be cleaned.
- Present a confirmation dialog summarizing the action and reassuring data safety before execution.
- Execute cleaning through a strictly serialized queue (one package at a time).
- Surface real-time progress (`Processing X of Y: App Name`).
- Individual failure on one package MUST NOT terminate or disrupt the remaining queue.
- Surface a comprehensive summary upon completion reporting successful, failed, and skipped counts along with verified total bytes freed.

### 4.5 Access Modes Specification

#### Standard Mode
- Requires no root permissions or Shizuku daemon.
- Reads application inventory and storage statistics (subject to `PACKAGE_USAGE_STATS` user grant).
- Provides direct navigation to Android system App Info via `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` for manual cache clearing.
- MUST NOT falsely report or claim ability to clear internal app caches directly.

#### Shizuku Mode
- Validates Shizuku package presence, daemon active status, and IPC binder connectivity.
- Requests runtime Shizuku authorization via Shizuku API.
- Detects whether the Shizuku server runs under ADB shell (UID 2000) or Root (UID 0).
- Verifies package-clearing API compatibility before exposing automated cleaning.
- Provides actionable diagnostic instructions if Shizuku is disconnected or inactive.

#### Root Mode
- Verifies binary existence of `su` in the system environment (`PATH`).
- Requests superuser grant via direct shell IPC.
- Verifies acquired privilege UID (`id -u` == 0).
- Executes verified privileged cache deletion commands.
- MUST target cache directories exclusively; indiscriminate directory deletion is strictly banned.

---

## 5. User Experience & Design Requirements

All user interface implementations MUST conform strictly to **[`DESIGN.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/DESIGN.md)**.

### 5.1 Design Philosophy
- **Minimal & Functional**: Standard Material 3 components; zero visual clutter or gratuitous gradients.
- **Performance First**: Zero GPU-intensive visual effects (no heavy blurs or drop shadows); smooth 60/120 FPS list scrolling.
- **Transparent Communication**: Honest reporting of device capabilities, permission requirements, and verified storage deltas.

### 5.2 Navigation Architecture
The application implements three primary destinations via a bottom `NavigationBar`:
1. **Home**: Overview of total cache, active access engine, and primary scan/clean actions.
2. **Apps**: Complete list of packages with search, filtering, sorting, and multi-selection.
3. **Settings**: Theme selection, access mode management, and application version info.

---

## 6. Non-Functional Requirements

### 6.1 Performance
- List scrolling MUST remain responsive and frame-drop-free via `LazyColumn` with stable keys (`key = { app.packageName }`).
- All storage calculation, package querying, and cleaning operations MUST run strictly off the Main thread on `Dispatchers.IO`.
- `StorageStatsManager` calls SHOULD be executed sequentially or with constrained coroutine parallelism to prevent IO saturation.

### 6.2 Resource Efficiency
- Zero continuous background services or persistent foreground notifications.
- The application MUST NOT require network or internet connectivity.
- Zero analytics, telemetry, or crash-reporting network calls.
- Lightweight memory footprint: Avoid caching raw app icon bitmaps in memory without bounds.

### 6.3 Security & Absolute Data Safety Policy

> [!CAUTION]
> **ABSOLUTE BAN ON DATA DESTRUCTIVE COMMANDS:**
> 1. NEVER execute `pm clear <package>` under any circumstances. `pm clear` wipes all private databases, login tokens, encryption keys, and preferences, causing complete user data loss.
> 2. ONLY target designated cache directories:
>    - `cacheDir` (`/data/user/0/<package>/cache`)
>    - `code_cache` (`/data/user/0/<package>/code_cache`)
>    - `externalCacheDir` (`/sdcard/Android/data/<package>/cache`)
> 3. NEVER delete, modify, or truncate:
>    - `databases/`
>    - `shared_prefs/`
>    - `files/`
>    - Account keystores or credential files.
> 4. All package name inputs passed to shell executors MUST be validated against strict regex (`^[a-zA-Z0-9_.]+$`) to eliminate shell injection vulnerabilities.

### 6.4 Reliability
- Gracefully handle uninstalled or modified packages during active queue processing.
- Recover cleanly from Shizuku binder disconnection or process termination.
- Explicitly model and handle root authorization denials.

---

# PART II — TECHNICAL SPECIFICATION

## 7. Technology Stack

| Layer | Technology | Specification / Library |
|---|---|---|
| **Language** | Kotlin | Version 2.3+ |
| **UI Framework** | Jetpack Compose | Compose BOM (Material 3) |
| **Design System** | Material Design 3 | `androidx.compose.material3` |
| **Architecture** | Clean Architecture + MVVM | Unidirectional Data Flow (UDF) |
| **Concurrency** | Kotlin Coroutines & Flow | `kotlinx-coroutines-core`, `kotlinx-coroutines-android` |
| **State Management** | StateFlow & SharedFlow | `androidx.lifecycle:lifecycle-viewmodel-compose` |
| **Local Persistence**| Jetpack DataStore | Preferences DataStore |
| **Package Discovery**| Android SDK | `android.content.pm.PackageManager` |
| **Storage Metrics** | Android SDK | `android.app.usage.StorageStatsManager` |
| **Privileged IPC** | Shizuku | `dev.rikka.shizuku:api`, `dev.rikka.shizuku:provider` |
| **Root Execution** | Linux Shell IPC | Superuser (`su`) process pipe |
| **Build System** | Gradle Kotlin DSL | Android Gradle Plugin (AGP 9.0+) |

### 7.1 Android Platform Target Configuration
- **Minimum SDK**: API 24 (Android 7.0) — *StorageStatsManager requires API 26+; fallbacks apply on API 24-25*.
- **Target SDK**: API 37 (Android 16 / latest stable).
- **Compile SDK**: API 37.
- **Java Compatibility**: Java 17 toolchain.

---

## 8. Application Architecture

The project enforces Clean Architecture with strict separation into **Presentation**, **Domain**, and **Data** layers.

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Jetpack Compose UI (Screens, Theme, Components)      │
└───────────────────────────┬────────────────────────────┘
                            │ Collects UiState
                            ▼
┌────────────────────────────────────────────────────────┐
│                    ViewModel Layer                     │
│   Exposes StateFlow<UiState>, handles UI events        │
└───────────────────────────┬────────────────────────────┘
                            │ Calls Use Cases
                            ▼
┌────────────────────────────────────────────────────────┐
│                      Domain Layer                      │
│   Use Cases (ScanApps, ClearCache, CheckAccess)        │
│   Domain Models (AppInfo, StorageInfo, CleaningResult) │
└───────────────────────────┬────────────────────────────┘
                            │ Calls Repositories
                            ▼
┌────────────────────────────────────────────────────────┐
│                       Data Layer                       │
│   AppRepository, StorageRepository, CleanerRepository  │
└───────┬───────────────────────┬────────────────────────┘
        │                       │
        ▼                       ▼
┌──────────────┐      ┌─────────────────────────────┐
│ Android SDK  │      │       Access Providers      │
│ PackageManager│     ├──────────────┬──────────────┤
│ StorageStats │      │ StandardMode │  ShizukuMode │ RootMode
└──────────────┘      └──────────────┴──────────────┴──────────
```

### 8.1 Package Structure Conventions
```text
app/src/main/kotlin/com/example/cachecleaner/
├── core/
│   ├── access/          # AccessManager, ShizukuManager, RootManager
│   ├── extensions/      # FileSize, Context, Coroutine helpers
│   └── utils/           # String formatters, Package validator
├── data/
│   ├── apps/            # AppRepository & PackageManager queries
│   ├── cleaner/         # StandardCleaner, ShizukuCleaner, RootCleaner
│   ├── preferences/     # DataStore user settings repository
│   └── storage/         # StorageStatsRepositoryImpl
├── domain/
│   ├── model/           # AppInfo, StorageInfo, CleaningResult, AccessMode
│   ├── repository/      # Interface contracts for repositories
│   └── usecase/         # GetInstalledAppsUseCase, CleanAppCacheUseCase
├── ui/
│   ├── components/      # Reusable Compose widgets (cards, dialogs, lists)
│   ├── main/            # Main screen & navigation hosts
│   ├── theme/           # Color, Theme, Type, Shape
│   └── viewmodel/       # Screen-level ViewModels
└── MainActivity.kt
```

---

## 9. Domain Models & Core Interfaces

### 9.1 Data Models
```kotlin
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val storageInfo: StorageInfo? = null
)

data class StorageInfo(
    val appBytes: Long,
    val dataBytes: Long,
    val cacheBytes: Long
) {
    val totalBytes: Long
        get() = appBytes + dataBytes + cacheBytes
}

data class CleaningResult(
    val packageName: String,
    val status: CleaningStatus,
    val freedBytes: Long = 0L,
    val errorMessage: String? = null
)

enum class CleaningStatus {
    SUCCESS,
    FAILED,
    UNSUPPORTED,
    PERMISSION_DENIED,
    SKIPPED
}

enum class AccessMode {
    STANDARD,
    SHIZUKU,
    ROOT
}

enum class AccessStatus {
    AVAILABLE,
    PERMISSION_REQUIRED,
    SERVICE_NOT_RUNNING,
    UNSUPPORTED,
    DENIED
}
```

### 9.2 Cache Cleaner Engine Contract
```kotlin
interface CacheCleanerEngine {
    val accessMode: AccessMode
    
    suspend fun checkAvailability(): AccessStatus
    
    suspend fun clearCache(packageName: String): CleaningResult
    
    suspend fun clearCaches(
        packageNames: List<String>,
        onProgress: suspend (current: Int, total: Int, currentPackage: String) -> Unit
    ): List<CleaningResult>
}
```

---

## 10. Access Engine Implementation Details

### 10.1 Standard Cleaner Implementation
- Directs user to the system app info page:
  ```kotlin
  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", packageName, null)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  context.startActivity(intent)
  ```
- Evaluates `StorageManager.ACTION_CLEAR_APP_CACHE` on supported Android versions where permitted.

### 10.2 Shizuku Cleaner Implementation
- Binds to the Shizuku IPC service.
- Inspects Shizuku binder token status (`Shizuku.checkSelfPermission()`).
- Calls package manager cache-trimming APIs via `IPackageManager` binder reflection, or executes verified ADB shell cache deletion:
  - Verifies shell capability before reporting `AccessStatus.AVAILABLE`.
  - Disallows `pm clear`.

### 10.3 Root Cleaner Implementation
- Dispatches privileged cache purge commands targeted exclusively to cache directories:
  ```bash
  rm -rf /data/user/0/<validated_package>/cache/*
  rm -rf /data/user/0/<validated_package>/code_cache/*
  rm -rf /sdcard/Android/data/<validated_package>/cache/*
  ```
- Sanitizes `<validated_package>` against strict package-name regex.
- Executes `sync` command to ensure filesystem commit.

### 10.4 Post-Operation Verification
- Before operation: Query `StorageStatsManager.queryStatsForPackage(...)` -> Record `beforeCacheBytes`.
- Execute cleaning command.
- After operation: Query `StorageStatsManager.queryStatsForPackage(...)` -> Record `afterCacheBytes`.
- Calculated freed space: `max(0, beforeCacheBytes - afterCacheBytes)`.
- If cache size decreased: Status is `CleaningStatus.SUCCESS` with recorded `freedBytes`.

---

## 11. Android Permissions & System Integration

### 11.1 AndroidManifest Declarations
```xml
<!-- Required to list non-default installed applications on Android 11+ (API 30+) -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

<!-- Required to query detailed storage statistics via StorageStatsManager -->
<uses-permission
    android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />

<!-- Query external storage metrics if applicable -->
<uses-permission
    android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
    tools:ignore="ScopedStorage" />
```

### 11.2 Permission Handling Guidelines
- `PACKAGE_USAGE_STATS` is a special access permission. The app MUST guide the user to `Settings.ACTION_USAGE_ACCESS_SETTINGS` with clear explanatory rationale.
- If usage stats permission is absent, the app MUST operate gracefully, listing applications while displaying cache sizes as `Unavailable`.

---

# PART III — DEVELOPMENT ROADMAP

## 12. Implementation Phases

```
┌────────────────────────────────────────────────────────┐
│ PHASE 1: Project Foundation & UI Scaffolding          │
│ • Compose M3 theme, navigation, base screens           │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ PHASE 2: Application Discovery & Inventory List        │
│ • PackageManager scanning, icons, search & filtering   │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ PHASE 3: Storage Statistics Engine                     │
│ • StorageStatsManager integration & usage access flow  │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ PHASE 4: Access Engine & Verification Protocol         │
│ • Standard, Shizuku, and Root cleaners + verification  │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ PHASE 5: Batch Execution & Polish                      │
│ • Serialized batch queue, progress dialogs, unit tests │
└────────────────────────────────────────────────────────┘
```

---

## 13. Acceptance Criteria

The implementation is verified and accepted when all criteria are met:
- [ ] Application compiles and executes smoothly on Android 10+ (API 29 to 37).
- [ ] Installed applications are enumerated with official names and icons.
- [ ] Cache sizes are calculated accurately via `StorageStatsManager` when access is granted.
- [ ] Missing permissions produce explicit, informative placeholders (never fake `0 B`).
- [ ] Multi-selection and "Select All" functions operate without recomposition stutter.
- [ ] Shizuku connection lifecycle (connect, bind, disconnect) handles interruptions cleanly.
- [ ] Root access is correctly detected and verified via `su`.
- [ ] Cache cleaning operations target cache directories ONLY; user data and databases remain 100% intact.
- [ ] The command `pm clear` is NEVER executed under any circumstance.
- [ ] Batch cleaning serializes operations, tolerates individual app errors, and summarizes results.
- [ ] Main thread remains responsive (no ANR or UI freeze) during scanning and cleaning.
- [ ] Complete test suite passes via `./gradlew testDebugUnitTest`.

---

# PART IV — DEVELOPMENT RULES

## 14. Coding Guidelines

1. **Idiomatic Kotlin**: Use coroutines, Flows, data classes, sealed interfaces, and standard Kotlin standard library idioms.
2. **Declarative UI**: Jetpack Compose only; never instantiate legacy Android Views or XML layouts for application screens.
3. **DRY & Single Responsibility**: Never accumulate duplicate methods. Consolidate shared logic into core utilities.
4. **No Suppressed Exceptions**: Always model errors explicitly using sealed results or Kotlin `Result<T>`.
5. **No Force Unwraps**: Avoid `!!` operators; leverage Kotlin's safe call (`?.`), `let`, and Elvis (`?:`) operators.

## 15. AI Coding Agent Directives

All AI agents operating on this codebase MUST:
1. Consult **[`AGENTS.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/AGENTS.md)** before planning or modifying code.
2. Verify library APIs using the **Context7 MCP** server (`resolve-library-id` and `query-docs`).
3. Comply strictly with UI/UX rules specified in **[`DESIGN.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/DESIGN.md)**.
4. Validate changes immediately with unit tests or Gradle compilation commands.
5. Strictly adhere to the **Zero Data Loss** policy: never modify or touch user databases, preferences, or credential stores.

---

# 16. Architectural Summary

Cache Cleaner is an honest, lightweight, single-purpose Android tool designed around one foundational promise:

> **"Accurate storage insights and safe cache cleaning without bloated extras, deceptive metrics, or data risk."**
