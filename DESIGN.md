# DESIGN.md — UI/UX Design Guidelines

**Project:** Cache Cleaner  
**Platform:** Android  
**UI Framework:** Jetpack Compose  
**Design System:** Material Design 3 (M3)  
**Theme:** Light Mode & Dark Mode  
**Design Philosophy:** Minimal, Professional, Simple  
**Version:** 1.0  

---

# 1. Design Philosophy

## 1.1 Overview

Cache Cleaner implements a Material Design 3 (M3) interface built upon a minimalist, highly functional design language.

The interface MUST deliver an intuitive, clean, and professional experience devoid of superfluous decorative components.

Because the application focuses strictly on Android app cache management, the following elements MUST serve as the visual focal points:
1. Storage usage metrics.
2. Device access mode status (Standard, Shizuku, Root).
3. Primary cache-cleaning actions.

## 1.2 Core Design Principles

### Minimal
- Avoid excessive decoration or visual clutter.
- Use standard Material Design 3 components.
- Do NOT use unneeded gradients or multi-color backgrounds.
- Maintain consistent, generous whitespace.

### Professional
- Establish a clear visual hierarchy.
- Enforce consistent typographic scaling.
- Use color strictly for semantic and functional purposes.
- Employ universally recognized system icons.
- Avoid aesthetic patterns typical of generic, ad-heavy "junk cleaner" apps.

### Easy to Use
- Primary functions MUST be immediately discoverable on initial screen load.
- Use explicit, unambiguous labels.
- Maintain shallow navigation (avoid deep page hierarchies).
- Minimize the number of user taps required to clear cache.
- Surface operation states and progress transparently.

### Performance First
- Avoid heavy or nested animations.
- Prohibit expensive blur, glow, or drop-shadow rendering effects.
- Use `LazyColumn` with stable keys for all application lists.
- Load application icons asynchronously directly from `PackageManager`.
- Eliminate visual elements that increase recomposition overhead or cause GPU frame drops.

---

# 2. Design System

## 2.1 Material Design 3

The entire interface MUST strictly comply with Material Design 3 specifications.

The application MUST use:
- Material 3 Color Schemes.
- Material 3 Typography.
- Material 3 Shapes.
- Material 3 Components (`androidx.compose.material3`).
- Material 3 Navigation patterns.
- Material 3 Interaction patterns.

Agents and developers MUST use official components from `androidx.compose.material3`. Do NOT create custom UI components when an official Material 3 component satisfies the requirement.

## 2.2 Theme Support

The application MUST support three theme options:
1. **System Default** (Follows system configuration; default option).
2. **Light Mode**.
3. **Dark Mode**.

Theme toggles MUST apply dynamically across all screens without requiring an application restart.

`MaterialTheme` MUST serve as the single source of truth for all colors, typography styles, and component shapes.

---

# 3. Color System

## 3.1 Color Direction

The application uses a neutral background palette paired with a functional blue accent for primary actions.

Color characteristics:
- Clean and tranquil.
- Professional and non-distracting.
- High contrast compliant with WCAG AA accessibility standards.

Blue is reserved for primary actions, progress indicators, and interactive states. Semantic colors (Success, Warning, Error) MUST ONLY be used when conveying explicit operational status.

## 3.2 Light Color Scheme

| Token | Semantic Role | Hex Value |
|---|---|---|
| `Primary` | Main interactive color & primary buttons | `#2563EB` |
| `On Primary` | Text/icons on primary color | `#FFFFFF` |
| `Primary Container` | Selected states & highlight badges | `#DBEAFE` |
| `On Primary Container`| Content on primary container | `#1E3A8A` |
| `Secondary` | Supporting elements & secondary icons | `#475569` |
| `Background` | Main screen background | `#FFFFFF` |
| `Surface` | Standard cards & container surfaces | `#F8FAFC` |
| `Surface Variant` | Elevated card backgrounds & input fills | `#F1F5F9` |
| `On Surface` | Primary typography & headers | `#0F172A` |
| `On Surface Variant` | Secondary/metadata typography | `#64748B` |
| `Outline` | Borders, dividers, and card outlines | `#CBD5E1` |
| `Error` | Failed operations & destructive actions | `#DC2626` |
| `Success` | Successful cleaning operations | `#16A34A` |
| `Warning` | Items requiring user attention/review | `#D97706` |

## 3.3 Dark Color Scheme

| Token | Semantic Role | Hex Value |
|---|---|---|
| `Primary` | Main interactive color & primary buttons | `#93C5FD` |
| `On Primary` | Text/icons on primary color | `#172554` |
| `Primary Container` | Selected states & highlight badges | `#1E3A5F` |
| `On Primary Container`| Content on primary container | `#DBEAFE` |
| `Secondary` | Supporting elements & secondary icons | `#CBD5E1` |
| `Background` | Main screen background | `#0F172A` |
| `Surface` | Standard cards & container surfaces | `#111827` |
| `Surface Variant` | Elevated card backgrounds & input fills | `#1E293B` |
| `On Surface` | Primary typography & headers | `#F8FAFC` |
| `On Surface Variant` | Secondary/metadata typography | `#94A3B8` |
| `Outline` | Borders, dividers, and card outlines | `#334155` |
| `Error` | Failed operations & destructive actions | `#FCA5A5` |
| `Success` | Successful cleaning operations | `#86EFAC` |
| `Warning` | Items requiring user attention/review | `#FCD34D` |

## 3.4 Color Usage Rules

- **Primary**: Primary action buttons and selected tab indicators.
- **Primary Container**: Active list selections and high-priority status badges.
- **Background**: Top-level Scaffold background.
- **Surface**: Card and container surfaces.
- **On Surface**: Primary text, titles, and high-emphasis information.
- **On Surface Variant**: Package identifiers, subtitles, and secondary metrics.
- **Error**: Explicit operation failures or critical access issues.
- **Success**: Verified completed cache-clearing operations.
- **Warning**: Missing permissions, ungranted scopes, or skipped operations.

> [!WARNING]
> - Never use the `Success` color purely for visual decoration.
> - Never use the `Error` (Red) color for standard "Clear Cache" actions. Cache clearing is a normal maintenance operation, not a catastrophic event. Red MUST be reserved strictly for irreversible destructive actions or critical error states.

## 3.5 Dynamic Color

Dynamic Color (Material You) MAY be enabled on Android 12+ (API 31+).
However, the static application color scheme MUST serve as a consistent, fully tested fallback. Dynamic color MUST NOT compromise text legibility, contrast ratios, or status clarity.

---

# 4. Typography

## 4.1 Typeface

The application MUST use the standard Android system typeface:
- **Roboto** (or standard system font for modern Android releases).
- Do NOT bundle custom external font files.

## 4.2 Typography Scale

The application MUST use the standard Material 3 Typography scale:

| Style | Recommended Application Usage |
|---|---|
| `Display Small` | Primary total cache size readout (e.g., "2.84 GB") |
| `Headline Small` | Top-level page titles |
| `Title Large` | Card and section headers |
| `Title Medium` | Application display names in list rows |
| `Body Large` | Primary descriptive content and dialog bodies |
| `Body Medium` | Secondary descriptions and status subtext |
| `Body Small` | Package identifiers (`com.example.app`) and timestamps |
| `Label Large` | Primary button labels and dialog actions |
| `Label Medium` | Category tags, filter chip labels, and status badges |
| `Label Small` | Low-emphasis metadata and footnote counters |

## 4.3 Typography Rules

- Use `FontWeight.Normal` (400) for descriptive body text.
- Use `FontWeight.Medium` (500) for application names and section headings.
- Use `FontWeight.SemiBold` (600) for storage size readouts and primary action buttons.
- Avoid overuse of bold weights.
- Ensure minimum readable font sizes across all densities.
- Numerical storage values SHOULD use tabular numbers (`FontFeature.tabularFigures()`) when available to prevent horizontal jitter during numerical updates.

---

# 5. Spacing & Layout

## 5.1 Spacing Scale

Layouts MUST strictly adhere to an 8-point / 4-point spacing grid:

| Token | Dimension |
|---|---|
| `XS` | 4 dp |
| `SM` | 8 dp |
| `MD` | 12 dp |
| `LG` | 16 dp |
| `XL` | 20 dp |
| `XXL` | 24 dp |
| `XXXL` | 32 dp |
| `Huge` | 40 dp |

## 5.2 Screen Layout

- **Horizontal content margin**: 16 dp.
- **Vertical section spacing**: 24 dp.
- **Internal card padding**: 16 dp.
- **Inter-component spacing**: 8 dp.
- Standard component heights MUST adhere to Material 3 guidelines (e.g., buttons: 40–48 dp).
- Implement edge-to-edge system bars (`enableEdgeToEdge`) with proper `WindowInsets` padding (status bar & navigation bar).

## 5.3 Responsive & Adaptive Screen Width

The interface is optimized primarily for smartphone form factors:
- On wide-screen devices (foldables, tablets), content width MUST be constrained using a centered max-width container (e.g., 600–720 dp) to preserve readable line lengths and prevent list items from stretching excessively.
- Maintain proportional spacing on large screens rather than blowing up smartphone dimensions.

---

# 6. Shapes & Elevation

## 6.1 Shape Scale

Components MUST use standardized corner radiuses consistent with Material 3:

| Component | Corner Radius | M3 Shape Token |
|---|---|---|
| Small Button | 12 dp | `ShapeDefaults.Small` |
| Standard Card | 16 dp | `ShapeDefaults.Medium` |
| Dialog Container | 28 dp | `ShapeDefaults.ExtraLarge` |
| Input Field (Outlined) | 12 dp | `ShapeDefaults.Small` |
| App Icon Container | 12 dp | `ShapeDefaults.Small` |
| Bottom Sheet | 28 dp (top corners) | `ShapeDefaults.ExtraLarge` |

## 6.2 Elevation & Shadows

Elevation MUST be minimized. Prioritize surface differentiation using:
1. `Surface` vs. `SurfaceVariant` background color contrast.
2. 1 dp `Outline` borders.
3. Material 3 tonal elevation (color overlay calculated via `surfaceColorAtElevation`).

Do NOT use heavy, blurred drop shadows.

---

# 7. Iconography

## 7.1 Icon Style

Use Google Material Symbols / Material Icons exclusively (`androidx.compose.material.icons`).

Icons MUST be simple, consistent, and instantly recognizable:

| Function / Screen | Icon Symbol |
|---|---|
| Home Screen | `Icons.Default.Home` |
| Applications Screen | `Icons.Default.Apps` |
| Settings Screen | `Icons.Default.Settings` |
| Search | `Icons.Default.Search` |
| Scan / Refresh | `Icons.Default.Refresh` |
| Clear Cache | `Icons.Default.DeleteSweep` |
| Select All | `Icons.Default.SelectAll` |
| Storage Overview | `Icons.Default.Storage` |
| Navigation Back | `Icons.AutoMirrored.Filled.ArrowBack` |
| Success Indicator | `Icons.Default.CheckCircle` |
| Error Indicator | `Icons.Default.Error` |
| Shizuku / Security | `Icons.Default.Security` |
| Root / Admin | `Icons.Default.AdminPanelSettings` |

Maintain a single visual weight (e.g., `Outlined` or `Default` filled). Do NOT mix disparate icon styling styles across identical contexts.

App icons displayed in application lists MUST be fetched directly from `PackageManager` using asynchronous, non-blocking coroutines.

---

# 8. Navigation Architecture

## 8.1 Main Navigation

The app uses a standard Material 3 bottom `NavigationBar`.

Three top-level destinations:
1. **Home**: High-level storage overview, quick scan, and bulk clean entry point.
2. **Apps**: Complete list of installed applications with filtering, sorting, and granular multi-selection.
3. **Settings**: Appearance, access mode management (Standard, Shizuku, Root), and app diagnostics.

The `NavigationBar` MUST only be visible on the three primary top-level screens. Detail or modal screens MUST hide the bottom bar and present an `ArrowBack` top bar navigation item.

## 8.2 Navigation Items

| Screen | Icon | Label |
|---|---|---|
| `Home` | `Home` | "Home" |
| `Apps` | `Apps` | "Apps" |
| `Settings` | `Settings` | "Settings" |

Labels MUST always remain visible (`alwaysShowLabel = true`) to maintain clarity and usability.

---

# 9. Screen Design Specifications

## 9.1 Home Screen

### Purpose
Provides an instant, transparent summary of device cache utilization and available access capabilities without requiring deep navigation.

### Visual Hierarchy
1. `TopAppBar`: Displays title "Cache Cleaner" and quick Settings shortcut icon.
2. `StorageSummaryCard`: Prominently features total cache size, application count, and last scan timestamp.
3. `AccessStatusCard`: Displays the active access engine (Standard, Shizuku, or Root) with operational status.
4. `PrimaryActions`: Quick "Scan Apps" and "Clear All Cache" buttons.
5. `RecentCleaningSummary` (Optional): Brief note on last freed space.
6. `NavigationBar`: Bottom navigation tabs.

### Storage Summary Card Specification
```text
┌───────────────────────────────────────────────┐
│ Storage Overview                              │
│                                               │
│ 2.84 GB                                       │
│ Total cache detected                          │
│                                               │
│ 124 applications          Last scan: 10:30 AM │
└───────────────────────────────────────────────┘
```
- The total cache number (e.g., "2.84 GB") MUST be the primary visual anchor using `Display Small`.
- Use a `Card` or `Surface` container with minimal decoration.
- Never show unverified or simulated freed space metrics before cleaning has actually completed.

### Access Status Card Specification
```text
┌───────────────────────────────────────────────┐
│ Access Mode                                   │
│                                               │
│ Shizuku Connected                             │
│ Elevated ADB Shell permissions active         │
└───────────────────────────────────────────────┘
```
Possible states:
- **Standard Mode**: Limited to system app settings redirection.
- **Shizuku Connected**: Automated ADB cache cleaning available.
- **Shizuku Permission Required**: Shizuku service running, authorization pending.
- **Shizuku Not Running**: Shizuku daemon stopped.
- **Root Available**: Superuser access active.
- **Root Permission Denied**: Superuser request rejected.

Labels MUST be self-explanatory and never rely solely on color badges to convey state.

### Primary Actions
- **Scan Apps**: `OutlinedButton` or `FilledTonalButton`. Initiates package and storage stats scanning.
- **Clear All Cache**: `Button` (Filled) using `Primary` color. Disabled if no cleanable cache is present.

### Home Screen Rules
- Do NOT display irrelevant hardware statistics (RAM, CPU load, battery temperature).
- Do NOT display advertisements or affiliate banners.
- Do NOT show active buttons for actions unsupported by the current access mode.

---

## 9.2 Apps Screen

### Purpose
The primary work area for discovering, inspecting, filtering, and executing cache cleaning on specific sets of applications.

### Visual Hierarchy
1. `TopAppBar`: Title "Applications" with search toggle and overflow menu.
2. `SearchBar` / `SearchField`: Instant filtering by application name or package name.
3. `FilterAndSortRow`: Filter chips (All, User, System) and sort dropdown.
4. `SelectionSummaryRow`: Active selection count and selected cache total.
5. `ApplicationList`: Efficient `LazyColumn` of installed apps.
6. `BottomActionBar`: Contextual floating/docked action bar appearing when items are selected.

### Search Field
- Component: `OutlinedTextField` with leading search icon and trailing clear button.
- Placeholder: `"Search applications..."`.
- Filters dynamically across both `appName` and `packageName`.

### Filter & Sort Controls
- **Filters** (`FilterChip`):
  - "All Apps"
  - "User Apps"
  - "System Apps"
- **Sort Options** (Dropdown Menu):
  - Cache Size: Largest First (Default)
  - Cache Size: Smallest First
  - App Name: A–Z
  - App Name: Z–A

### Application List Item Specification
```text
┌────────────────────────────────────────────────────────┐
│ [Icon]  Instagram                              645 MB  │
│         com.instagram.android                     [X]  │
└────────────────────────────────────────────────────────┘
```
- **App Icon**: 40–48 dp rounded container, loaded asynchronously.
- **App Name**: `TitleMedium`, single-line with ellipsis overflow.
- **Package Name**: `BodySmall`, muted `OnSurfaceVariant`.
- **Cache Size**: `TitleMedium` or `BodyLarge`, right-aligned tabular numbers.
- **Checkbox**: Right-aligned Material 3 `Checkbox`.
- Entire item surface MUST be clickable to toggle selection.
- List MUST implement `LazyColumn` with explicit `key = { it.packageName }`.

### Selection Summary & Bottom Action Area
When one or more applications are selected, dock a bottom summary bar above the navigation bar:
```text
┌────────────────────────────────────────────────────────┐
│ 3 apps selected (1.24 GB)       [ Clear Selected (3) ] │
└────────────────────────────────────────────────────────┘
```
- Clear button label MUST reflect selection count: `"Clear Selected (N)"`.

### Empty States
- **No applications detected**:
  - Message: `"No applications found"`.
  - Subtext: `"Tap below to scan your device storage."`.
  - Action: `"Scan Apps"`.
- **Search yields no matches**:
  - Message: `"No matching applications"`.
  - Subtext: `"Check for typos or try a different search term."`.

---

## 9.3 Application Detail Screen

### Purpose
Provides in-depth storage breakdown for an individual application and allows targeted single-app cache clearing.

### Layout Specification
1. Large App Icon (64 dp) centered or top-aligned.
2. Application Name (`HeadlineSmall`) and Package Name (`BodyMedium`).
3. Storage Breakdown Table:
   ```text
   ┌───────────────────────────────────────────────┐
   │ Storage Details                               │
   │                                               │
   │ Application Size                     120 MB   │
   │ User Data                            250 MB   │
   │ Cache                                645 MB   │
   │ ───────────────────────────────────────────── │
   │ Total Storage Usage                  1.01 GB  │
   └───────────────────────────────────────────────┘
   ```
4. Action Button:
   - In **Shizuku / Root Mode**: Filled Button `"Clear Cache"`.
   - In **Standard Mode**: Filled Tonal Button `"Open System App Settings"` with explanatory helper note.

---

## 9.4 Settings Screen

### Purpose
Provides theme selection, access mode configuration, background scan preferences, and version information.

### Structure
- **Appearance**:
  - Theme Selection: System Default, Light, Dark.
- **Access Management**:
  - Active Mode Indicator (Standard, Shizuku, Root).
  - Shizuku Connection Status & Recheck Action.
  - Root Access Request / Verification.
  - Setup Guide link for Shizuku wireless ADB pairing.
- **About**:
  - App Version & Build.
  - Source Repository & License Information.

---

# 10. Dialogs & Bottom Sheets

## 10.1 Clear Cache Confirmation Dialog

A confirmation dialog MUST appear before executing batch operations:

```text
┌─────────────────────────────────────────────────┐
│ Clear selected cache?                           │
│                                                 │
│ You are about to clear cache for 12             │
│ applications (~2.4 GB).                         │
│                                                 │
│ User data, accounts, and saved files will not   │
│ be affected.                                    │
│                                                 │
│                     [ Cancel ]  [ Clear Cache ] │
└─────────────────────────────────────────────────┘
```
- Dismissive button: `"Cancel"` (`TextButton`).
- Confirming button: `"Clear Cache"` (`Button`, Primary color).
- Text MUST explicitly reassure the user that user accounts and database files will not be deleted.

## 10.2 Permission & Setup Dialogs

When privileged features are triggered without active permissions:
- Explain clearly why the elevated privilege is required.
- Provide a direct link to the setup guide or system settings.
- Do NOT block the user from continuing in Standard mode.

## 10.3 Cleaning Result Summary Dialog

Upon batch completion, present a concise verification dialog:
```text
┌─────────────────────────────────────────────────┐
│ Cleaning Completed                              │
│                                                 │
│ • 10 applications cleaned successfully          │
│ • 2 applications failed                         │
│                                                 │
│ Total space freed: 1.82 GB                      │
│                                                 │
│                                        [ Done ] │
└─────────────────────────────────────────────────┘
```

---

# 11. Loading, Progress & Feedback

## 11.1 Scanning State
- Display a smooth `LinearProgressIndicator` or lightweight `CircularProgressIndicator`.
- Display a brief, static message: `"Scanning installed applications..."`.
- Avoid heavy rotating graphics or simulated speedometers.

## 11.2 Cleaning Execution State
- MUST execute in a background coroutine (`Dispatchers.IO`) without blocking the main UI thread.
- Display a linear progress bar reflecting: `"Processing item X of Y (App Name)..."`.
- Prevent accidental navigation or duplicate triggers while an operation is in flight.

## 11.3 Notifications & Feedback
- Successful non-batch operations SHOULD emit a transient `Snackbar`:
  `"Cache cleared successfully."`
- Failures MUST produce actionable, human-readable error descriptions:
  - *"Shizuku service is not responding. Please verify Shizuku is running."*
  - *"Root permission was denied by the superuser manager."*
  - *"Storage permission is required to inspect application sizes."*
- NEVER display raw stack traces, uncaught exceptions, or raw Linux exit codes directly to the user.

---

# 12. Interaction & Motion

## 12.1 Motion Principles
- Motion MUST be functional, purposeful, and brief (150–300 ms).
- Use standard Material 3 motion tokens (`EnterTransition`, `ExitTransition`, `fadeIn`, `fadeOut`).
- Strictly avoid:
  - Heavy Lottie animation JSON files.
  - Parallax scrolling.
  - Particle effects or simulated "flying broom/rocket" graphics.
  - Infinite decorative animations that consume battery cycles.

## 12.2 Accessibility Motion
- System "Reduce Motion" settings MUST be respected by disabling optional transitions.

---

# 13. Accessibility Standards (a11y)

The application MUST comply with Android accessibility guidelines:
- **Touch Target Sizes**: All interactive elements (buttons, checkboxes, chips, list items) MUST have a minimum touch target dimension of **48 × 48 dp**.
- **Content Descriptions**: Every icon-only button and graphical asset MUST have a localized, descriptive `contentDescription` (or `null` if purely decorative).
- **Color Independence**: Visual status MUST NOT rely exclusively on color; pair colored badges with textual labels or distinct icon shapes.
- **Contrast**: All text and critical UI boundaries MUST meet WCAG 2.1 AA minimum contrast standards (4.5:1 for normal text, 3:1 for large text).
- **TalkBack Navigation**: Test focus traversal order to ensure logical, sequential reading.
- **Dynamic Font Scaling**: Layouts MUST handle system font scale up to 200% without text clipping or button overflow.

---

# 14. Responsive Layout Guidelines

- The primary UI target is smartphone screens in portrait orientation.
- Layouts MUST gracefully support:
  - Small phone screens (≥360 dp width).
  - Landscape orientation via scrollable containers (`rememberScrollState()`).
  - Foldables and tablets by capping list widths using `Modifier.widthIn(max = 640.dp)`.
- Never hardcode absolute pixel positions or assume fixed aspect ratios.

---

# 15. UI Component Library Architecture

The UI layer MUST be organized into modular, reusable Compose components:

| Component Composable | Layer / File | Responsibility |
|---|---|---|
| `AppTopBar` | `ui/components/AppTopBar.kt` | Uniform top bar across screens |
| `StorageSummaryCard` | `ui/components/StorageSummaryCard.kt` | Top-level cache visualization card |
| `AccessStatusCard` | `ui/components/AccessStatusCard.kt` | Displays current engine (Standard/Shizuku/Root) |
| `AppListItem` | `ui/components/AppListItem.kt` | App row with icon, title, cache size & checkbox |
| `AppIcon` | `ui/components/AppIcon.kt` | Async app icon loader with fallback placeholder |
| `StorageBreakdownTable` | `ui/components/StorageBreakdownTable.kt` | Detailed breakdown (APK, Data, Cache, Total) |
| `SearchInputBar` | `ui/components/SearchInputBar.kt` | Outlined search input with clear trigger |
| `FilterSortSection` | `ui/components/FilterSortSection.kt` | Filter chips row and sort menu |
| `SelectionBottomBar` | `ui/components/SelectionBottomBar.kt` | Bottom docked bar with selection count & action |
| `EmptyStateView` | `ui/components/EmptyStateView.kt` | Friendly, actionable empty state graphic/text |
| `ConfirmCleanDialog` | `ui/components/ConfirmCleanDialog.kt` | Modal confirmation before batch deletion |
| `CleanResultDialog` | `ui/components/CleanResultDialog.kt` | Summary of completed cleaning actions |

All components MUST consume `MaterialTheme` color and typography tokens directly. Do NOT hardcode colors or dimensions inside individual component implementations.

---

# 16. Jetpack Compose Implementation Guidelines

## 16.1 Theme Package Structure
```text
ui/theme/
├── Color.kt     # Light and Dark color token definitions
├── Theme.kt     # CacheCleanerTheme composable and MaterialTheme wrapper
├── Type.kt      # Typography scale mappings using system Roboto
└── Shape.kt     # ShapeDefinitions matching M3 corner radius specifications
```

## 16.2 State Management Protocol
- All screens MUST use AndroidX `ViewModel` paired with unidirectional data flow (UDF).
- ViewModels expose state via `StateFlow<UiState>`.
- Composables collect state using `collectAsStateWithLifecycle()`.
- Composable functions MUST be stateless where possible; elevate events to ViewModels via lambdas.

## 16.3 Performance Optimization Rules
- All application lists MUST use `LazyColumn` with explicit, stable keys:
  ```kotlin
  LazyColumn {
      items(
          items = appsList,
          key = { app -> app.packageName }
      ) { app ->
          AppListItem(app = app, ...)
      }
  }
  ```
- Use `derivedStateOf` for derived properties (e.g., search filtering, selection count).
- Never trigger disk IO, package queries, or cache scanning directly inside a Composable body.

---

# 17. Design Do's and Don'ts

### DO
- Adhere strictly to Material Design 3 guidelines.
- Maintain consistent whitespace and clean margins.
- Provide honest, verified data metrics to the user.
- Test both Light and Dark themes for visual parity.
- Keep the interface responsive and fast.

### DON'T
- Do NOT use flashy neon gradients or gimmick graphics.
- Do NOT display fake cache counters or simulated acceleration gauges.
- Do NOT place destructive actions behind ambiguous button labels.
- Do NOT bundle custom fonts or heavy asset libraries without explicit necessity.
- Do NOT display dialogs for trivial, non-destructive actions.

---

# 18. Architectural Summary

Cache Cleaner must present itself as a modern, lightweight, respectful Android utility. It embodies the core design philosophy:

> **"Less UI, more clarity."**

The user should be able to check their device cache status, select apps, and safely free space in seconds, with zero confusion and complete transparency.

---

# 19. Reference Documentation

- [Material Design 3 Specifications](https://m3.material.io/)
- [Material 3 for Jetpack Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Android Accessibility Principles](https://developer.android.com/guide/topics/ui/accessibility)
- [Jetpack Compose Performance](https://developer.android.com/develop/ui/compose/performance)
- [Android Adaptive Layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive)
