# Cache Cleaner

[![Release](https://img.shields.io/github/v/release/pocicaa44/CacheCleaner?include_prereleases&color=blue&style=flat-square)](https://github.com/pocicaa44/CacheCleaner/releases/latest)
[![Platform](https://img.shields.io/badge/platform-Android%2010%2B%20(API%2029%2B)-green?style=flat-square)](https://developer.android.com)
[![Size](https://img.shields.io/badge/APK%20Size-~2.8%20MB-orange?style=flat-square)](https://github.com/pocicaa44/CacheCleaner/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-lightgrey?style=flat-square)](LICENSE)

A lightweight, performant, and transparent Android utility built strictly for inspecting device storage and clearing application cache safely. Zero ads, zero background services, zero tracking telemetry, and no bloatware.

---

## Highlights & Features

- **Triple-Tier Access Model**:
  - **Standard Mode**: Zero special permissions needed. Inspects cache sizes and directs you to system app settings for manual clearing.
  - **Shizuku Mode**: Automated 1-click silent cache deletion via wireless ADB (`IPackageManager` IPC) without computer tethering or root.
  - **Root Mode**: Direct privileged shell execution for rooted Android devices.
- **Smart Storage Overview**:
  - Displays verified cleanable cache and total application metrics.
  - Live on-the-spot loading indicator (`Collecting cache...`) while scanning storage.
  - Performance-optimized: calculation triggers only during cold starts, manual scans, or after clean operations.
- **Application Management**:
  - Real-time search across package names and application labels.
  - Quick filter chips: **All**, **User**, and **System** applications.
  - Flexible sorting: by largest/smallest cache size, or alphabetically (A–Z / Z–A).
  - Multi-select batch cleaning with live estimated cache counter.
  - Pull-to-refresh (`PullToRefreshBox`) and an accessible 1-tap refresh button in the top bar.
- **Detailed Storage Breakdown**:
  - Inspect individual application sizes: Application Binary, User Data, Cache, and Total Space Used.
- **Modern Material Design 3**:
  - Full Material 3 Expressive UI.
  - **Dynamic Color (Material You)** matching device wallpaper and system palette.
  - Seamless support for System Default, Light, and Dark themes.
- **Strict Data Safety Guarantee**:
  - Only targets cleanable cache directories (`cacheDir`, `externalCacheDir`, `code_cache`).
  - **NEVER** touches personal databases, accounts, login tokens, or user data files.
- **Ultra-Compact Size**:
  - Fully minified to **~2.8 MB** using R8 shrinking and resource optimization.
  - In-memory multi-tier `LruCache` for instantaneous icon and label loading without main-thread jank.

---

## Operational Access Modes

| Mode | Privileges Required | Automation | Best For |
|---|---|---|---|
| **Standard** | None (Usage Access optional for sizes) | Manual (Opens App Settings) | General users seeking safety and zero setup. |
| **Shizuku** | [Shizuku](https://shizuku.rikka.app) running | Fully Automated (1-Click) | Power users wanting automated batch clearing without rooting. |
| **Root** | Superuser (`su` binary) | Fully Automated (1-Click) | Rooted devices (Magisk, KernelSU, APatch). |

---

## Download & Installation

Download the latest release APK directly from GitHub Releases:

📦 **[Download Cache Cleaner v1.0.0 APK](https://github.com/pocicaa44/CacheCleaner/releases/latest)**

### Prerequisites
- Android 10 (API Level 29) or newer.
- **Usage Access Permission**: Prompted on first launch to allow Android's `StorageStatsManager` to accurately calculate cache sizes.
- *(Optional)* [Shizuku](https://shizuku.rikka.app) for automated non-root cache clearing.

---

## Architecture & Tech Stack

```
app/
├── data/
│   ├── apps/           # PackageManager scanner & LRU icon cache
│   ├── cleaner/        # Triple-tier cleaner engines (Standard, Shizuku, Root)
│   ├── preferences/    # Jetpack DataStore preferences
│   └── storage/        # Android StorageStatsManager repository
├── domain/
│   └── model/          # AppInfo, AccessMode, CleaningResult, AppFilter
├── ui/
│   ├── apps/           # Apps list screen, ViewModel, search & sorting
│   ├── components/     # TopBar, PullToRefresh, StorageSummaryCard, Dialogs
│   ├── detail/         # Individual app storage breakdown screen
│   ├── home/           # Dashboard screen, overview & action buttons
│   ├── settings/       # Appearance, Dynamic Color, Engine preferences
│   └── theme/          # Material 3 ColorScheme & typography
└── core/               # ShizukuManager, ShellExecutor, FileSizeFormatter
```

- **UI Layer**: 100% Jetpack Compose with Material 3 components.
- **Architecture**: Clean Architecture with unidirectional data flow (`StateFlow` + ViewModels).
- **Asynchronous Execution**: Kotlin Coroutines with IO thread dispatching for all disk and IPC operations.
- **IPC & Elevated Privileges**: Official Shizuku API (`dev.rikka.shizuku:api`).
- **Build Engine**: Android Gradle Plugin 9.0+, Kotlin 2.3.20, JVM Toolchain 17.

---

## Building from Source

### Requirements
- JDK 17 or higher
- Android SDK (API 37 compile SDK)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/pocicaa44/CacheCleaner.git
cd CacheCleaner

# Run Unit Tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug

# Assemble Minified Release APK (~2.8 MB)
./gradlew assembleRelease
```
The output APK will be generated at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## Data Safety Policy

> [!IMPORTANT]
> **Zero Risk of Accidental Data Loss:**
> Cache Cleaner is engineered with an absolute safety mandate.
> - **Cleanable Targets**: Only temporary cached files (`/cache`, `/code_cache`, and external cache).
> - **Untouchable Targets**: `/databases`, `/shared_prefs`, `/files`, login sessions, and app accounts are strictly protected and never touched under any circumstance.

---

## License

```
Copyright 2026 pocicaa44

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
