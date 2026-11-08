# AGENTS.md — Autonomous Agent Operating Guidelines & Rules

This document governs the operational rules, architectural constraints, and execution workflow for all AI agents working on the **Cache Cleaner** repository. All instructions are mandatory and strictly enforced.

---

## 1. Skill Analysis & Execution Protocol

> [!IMPORTANT]
> **Mandatory Skill Analysis Before Task Execution:**
> Before formulating a plan or modifying code, the agent **MUST** evaluate the user request against all available skills (both project-level `.agents/skills/` and global/plugin skills).
> When an applicable skill exists, the agent **MUST read its `SKILL.md`** first via `read_file` and adhere to its specialized procedures. Never proceed on assumptions without reading the relevant skill file.

### Four-Phase Execution Workflow

```
┌────────────────────────────────────────────────────────┐
│ 1. SKILL & TASK ANALYSIS                               │
│    Match user intent against the skill registry.       │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ 2. READ SKILL INSTRUCTIONS                             │
│    Inspect SKILL.md and its associated reference docs. │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ 3. CONTEXT7 MCP DOCUMENTATION LOOKUP                   │
│    Fetch the latest Android/Kotlin API references.     │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ 4. EXECUTION, CLEAN CODE & CONTINUOUS TESTING          │
│    Implement with zero duplication, test, and verify.  │
└────────────────────────────────────────────────────────┘
```

### Skill Routing Matrix

| Task Domain | Target Skill | Location | When to Activate |
|---|---|---|---|
| **Android CLI & SDK** | `android-cli` | Global plugin | Managing SDK packages, running apps, emulator interactions, logcat inspection. |
| **Compose UI Validation** | `compose-preview` | Global plugin | Creating or modifying `@Preview` Compose components to render UI snapshots. |
| **Device Hub / AVD** | `android-device-hub` | Global plugin | Controlling or mirroring physical/virtual Android devices via ADB. |
| **API & Library Research** | `context7-mcp` | Global plugin / MCP | Verifying modern AndroidX, Compose, Coroutines, Shizuku, or Room APIs. |
| **AGP 9+ Migration** | `kotlin-tooling-agp9-migration` | [`SKILL.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/.agents/skills/kotlin-tooling-agp9-migration/SKILL.md) | Upgrading Android Gradle Plugin, updating build scripts, or fixing DSL deprecations. |
| **Java to Kotlin** | `kotlin-tooling-java-to-kotlin` | [`SKILL.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/.agents/skills/kotlin-tooling-java-to-kotlin/SKILL.md) | Migrating Java snippets or legacy files to idiomatic Kotlin. |
| **Immutable Collections** | `kotlin-tooling-immutable-collections-0-5-x-migration` | [`SKILL.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/.agents/skills/kotlin-tooling-immutable-collections-0-5-x-migration/SKILL.md) | Working with `kotlinx.collections.immutable` 0.5+ API changes. |
| **Kotlin Toolchain** | `kotlin-tooling-kotlin-toolchain` | [`SKILL.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/.agents/skills/kotlin-tooling-kotlin-toolchain/SKILL.md) | Configuring or troubleshooting JetBrains Kotlin Toolchain projects. |
| **Build Performance** | `kotlin-tooling-native-build-performance` | [`SKILL.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/.agents/skills/kotlin-tooling-native-build-performance/SKILL.md) | Diagnosing compilation bottlenecks and optimizing build speeds. |

---

## 2. Context7 MCP Integration for Modern Documentation

To avoid hallucinated APIs and outdated practices:

1. **Context7 First**: When working with external libraries, AndroidX APIs, Jetpack Compose components, Shizuku APIs, Kotlin Coroutines, or Room persistence, **ALWAYS** query Context7 MCP tools (`resolve-library-id` and `query-docs`).
2. **Two-Step Query Flow**:
   - `resolve-library-id`: Resolve the exact library ID (e.g., `/websites/developer_android_develop_ui_compose`).
   - `query-docs`: Query focused, topic-specific concepts (e.g., `"LazyColumn stickyHeader key optimization"`).
3. **Never Guess Deprecated APIs**: Android platform APIs change frequently. Always confirm target method signatures and lifecycle scopes with Context7.

---

## 3. Clean Code & Architectural Standards

All code written in this repository must maintain high architectural cleanliness:

- **DRY Principle (Don't Repeat Yourself)**:
  - **NEVER accumulate duplicate or overlapping methods**. If a method or helper performs a task similar to existing logic, refactor into a single reusable, well-tested function.
  - Consolidate common data transformations, permission checks, formatting utilities, and IO operations into shared domain/core modules.
- **Single Responsibility Principle (SRP)**:
  - Each class, composable, and function must have one clear responsibility.
  - Keep composable functions modular, stateless where possible, and decoupled from business logic.
- **Layered Clean Architecture**:
  - **Presentation Layer**: Jetpack Compose UI + ViewModels using unidirectional data flow (`StateFlow` / UI State classes).
  - **Domain Layer**: Pure Kotlin use cases encapsulating business logic, completely free from Android framework dependencies where practical.
  - **Data Layer**: Repositories abstracting data sources (Standard Android Storage API, Shizuku Service, Root Shell Provider).
- **Error Handling & Null Safety**:
  - Never suppress exceptions or use empty catch blocks.
  - Model operational failures explicitly with Kotlin `Result<T>` or sealed outcome hierarchies.
  - Avoid force unwraps (`!!`).

---

## 4. Mandatory Testing & Verification Workflow

> [!CAUTION]
> **No Unverified Changes:**
> Every file addition, modification, or refactoring MUST be validated before concluding a task.

1. **Continuous Verification**:
   - When creating or modifying code, run unit tests via Gradle:
     ```bash
     ./gradlew testDebugUnitTest
     ```
   - When updating build configurations, dependencies, or UI code, run compile checks:
     ```bash
     ./gradlew compileDebugKotlin
     ```
2. **Test Coverage**:
   - Write unit tests for all domain use cases, repositories, and utility functions.
   - Test success paths, error states, and boundary conditions (e.g., zero cache found, denied permissions, uninstalled apps).
3. **UI Preview Verification**:
   - For Composable UI changes, maintain and verify `@Preview` functions using the `compose-preview` skill whenever feasible.

---

## 5. UI/UX Design System Guidelines

Refer to **[`DESIGN.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/DESIGN.md)** for all visual, stylistic, and UX specifications:

- **UI Framework**: Modern Jetpack Compose using **Material Design 3 (M3)** components exclusively.
- **Theme Support**: Full, consistent support for both **Light Mode** and **Dark Mode** via MaterialTheme color schemes.
- **Philosophy — *Minimal, Professional, Performance First***:
  - Avoid unnecessary gradients, flashy decorative animations, or heavy drop shadows and blurs.
  - Keep navigation shallow, transparent, and direct (minimal steps required to clean cache).
- **List Performance Optimization**:
  - All application lists MUST use `LazyColumn` with explicit, stable `key = { app.packageName }`.
  - Use `derivedStateOf` and remember memoization to eliminate redundant recompositions.
  - Load app icons asynchronously and efficiently using `PackageManager` or Coil without blocking the main UI thread.

---

## 6. Product Specifications & Data Safety

Refer to **[`PRD.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/PRD.md)** for detailed feature requirements and technical constraints:

- **Triple-Tier Access Model**:
  1. **Standard Mode**: Non-root Android storage APIs (Storage Stats Manager / Storage Access).
  2. **Shizuku Mode**: Wireless ADB / elevated permissions without computer tethering.
  3. **Root Mode**: Direct privileged shell execution for rooted devices.
- **STRICT DATA SAFETY POLICY**:
  - **ONLY** clear cache directories: `cacheDir`, `externalCacheDir`, and `code_cache`.
  - **ABSOLUTE BAN**: **NEVER** delete, truncate, or touch `databases/`, `shared_prefs/`, `files/`, or user credential stores.
  - Accidental data loss constitutes a critical regression.

---

## 7. Agent Completion Checklist

Before reporting task completion to the user, ensure all items are fulfilled:

- [ ] Evaluated and utilized applicable skills from `.agents/skills/` and global plugins.
- [ ] Verified API documentation against Context7 MCP.
- [ ] Adhered to Clean Code principles with zero duplicate/overlapping methods.
- [ ] Executed unit tests or build compilation commands to verify changes.
- [ ] Complied with UI/UX rules in [`DESIGN.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/DESIGN.md).
- [ ] Preserved data safety constraints defined in [`PRD.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/PRD.md).
