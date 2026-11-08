# Shizuku API Implementation Guide for Cache Cleaner

Dokumen ini merupakan panduan teknis dan referensi arsitektur untuk mengimplementasikan **Shizuku API** pada aplikasi **Cache Cleaner**. Panduan ini berfokus pada pemanfaatan hak akses istimewa (ADB Shell / Root) melalui Shizuku untuk mengeksekusi penghapusan cache aplikasi secara otomatis, aman, dan tanpa tethering ke komputer.

---

## 1. Ikhtisar & Arsitektur Shizuku

### 1.1 Apa itu Shizuku?
Shizuku adalah framework open-source buatan **RikkaApps** yang memungkinkan aplikasi pihak ketiga berinteraksi langsung dengan sistem Android menggunakan hak istimewa tingkat tinggi:
- **ADB Shell (UID 2000)**: Melalui Wireless Debugging / tethered ADB.
- **Root (UID 0)**: Melalui daemon superuser (`su`).

### 1.2 Mengapa Shizuku untuk Cache Cleaner?
1. **Bukan Polling Shell Lambat**: Berbeda dengan perintah shell biasa (`Runtime.getRuntime().exec("su")`) yang lambat dan boros resource, Shizuku menggunakan IPC Binder native Android (`ShizukuBinderWrapper`).
2. **Akses ke System APIs**: Memberikan akses ke API sistem internal (`IPackageManager`) yang dilindungi permission tingkat `signature|privileged` seperti `android.permission.DELETE_CACHE_FILES`.
3. **Tanpa Root untuk Pengguna Biasa**: Pengguna dapat mengaktifkan Shizuku via Wireless Debugging sekali saja (Android 11+) tanpa perlu me-root perangkat.

---

## 2. Dependensi & Konfigurasi Proyek

### 2.1 Konfigurasi Gradle (`gradle/libs.versions.toml`)

Tambahkan versi dan library Shizuku ke catalog:

```toml
[versions]
shizuku = "13.1.5"

[libraries]
shizuku-api = { module = "dev.rikka.shizuku:api", version.ref = "shizuku" }
shizuku-provider = { module = "dev.rikka.shizuku:provider", version.ref = "shizuku" }
```

### 2.2 Konfigurasi Module (`app/build.gradle.kts`)

Tambahkan dependencies dan aktifkan fitur AIDL (jika menggunakan interface AIDL untuk stub sistem):

```kotlin
android {
    ...
    buildFeatures {
        compose = true
        aidl = true // Diperlukan untuk kompilasi stub IPackageManager / IPackageDataObserver
    }
}

dependencies {
    ...
    // Shizuku API & Provider
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
}
```

### 2.3 Konfigurasi Manifest (`app/src/main/AndroidManifest.xml`)

Daftarkan `ShizukuProvider` di dalam tag `<application>`:

```xml
<application ...>

    <!-- Provider untuk komunikasi IPC dengan Shizuku Server -->
    <provider
        android:name="rikka.shizuku.ShizukuProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />

</application>
```

> [!NOTE]
> Nilai `android:permission="android.permission.INTERACT_ACROSS_USERS_FULL"` melindungi provider agar hanya dapat diakses oleh sistem atau Shizuku server, bukan oleh aplikasi biasa lainnya.

---

## 3. Manajemen Siklus Hidup & Permission Shizuku

Sebelum melakukan operasi pembersihan cache, aplikasi wajib memeriksa status ketersediaan server Shizuku dan status izin akses.

### 3.1 Status Ketersediaan & Listener

```kotlin
package com.example.cachecleaner.core.access

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuManager {

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isAvailable.value = false
        _isPermissionGranted.value = false
    }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_SHIZUKU) {
                _isPermissionGranted.value = (grantResult == PackageManager.PERMISSION_GRANTED)
            }
        }

    const val REQUEST_CODE_SHIZUKU = 1001

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        checkStatus()
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }

    fun checkStatus() {
        val pingSuccess = Shizuku.pingBinder()
        _isAvailable.value = pingSuccess

        if (pingSuccess) {
            _isPermissionGranted.value = checkPermission()
        } else {
            _isPermissionGranted.value = false
        }
    }

    fun checkPermission(): Boolean {
        if (!Shizuku.pingBinder()) return false
        return if (Shizuku.isPreV11()) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission() {
        if (!Shizuku.pingBinder()) return
        if (checkPermission()) {
            _isPermissionGranted.value = true
            return
        }
        Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
    }

    /**
     * Memeriksa UID Shizuku aktif:
     * 2000 = ADB Shell
     * 0 = Direct Root
     */
    fun getUid(): Int {
        return if (Shizuku.pingBinder()) Shizuku.getUid() else -1
    }
}
```

---

## 4. Mekanisme Pembersihan Cache Otomatis

Terdapat dua pendekatan utama untuk menghapus cache aplikasi melalui Shizuku:

### Pendekatan 1 (Utama): System Binder Proxy via `deleteApplicationCacheFilesAsUser`

Pendekatan ini menggunakan API resmi sistem Android pada `IPackageManager` yang dibungkus oleh `ShizukuBinderWrapper`. Pemanggilan binder ini memiliki identitas ADB Shell (`com.android.shell`, UID 2000) yang memiliki permission `android.permission.DELETE_CACHE_FILES`.

#### 1. AIDL Stub Definition
Buat file AIDL di `app/src/main/aidl/android/content/pm/IPackageDataObserver.aidl`:

```aidl
package android.content.pm;

oneway interface IPackageDataObserver {
    void onRemoveCompleted(in String packageName, boolean succeeded);
}
```

Buat file AIDL di `app/src/main/aidl/android/content/pm/IPackageManager.aidl`:

```aidl
package android.content.pm;

import android.content.pm.IPackageDataObserver;

interface IPackageManager {
    // Android 11+ (API 30+)
    void deleteApplicationCacheFilesAsUser(
        in String packageName,
        int userId,
        in IPackageDataObserver observer
    );

    // Android 10 kebawah (API 29 kebawah)
    void deleteApplicationCacheFiles(
        in String packageName,
        in IPackageDataObserver observer
    );
}
```

#### 2. Implementasi Kotlin Coroutines Cleaner

```kotlin
package com.example.cachecleaner.data.cleaner

import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageManager
import android.os.Build
import android.os.IBinder
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import kotlin.coroutines.resume

class ShizukuCacheCleaner {

    private fun getPackageManager(): IPackageManager {
        val binder: IBinder = SystemServiceHelper.getSystemService("package")
            ?: throw IllegalStateException("Failed to acquire package service binder")
        return IPackageManager.Stub.asInterface(ShizukuBinderWrapper(binder))
    }

    suspend fun clearCache(
        packageName: String,
        userId: Int = 0,
        timeoutMs: Long = 10_000L
    ): CleaningResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                val observer = object : IPackageDataObserver.Stub() {
                    override fun onRemoveCompleted(pkg: String?, succeeded: Boolean) {
                        if (continuation.isActive) {
                            if (succeeded) {
                                continuation.resume(
                                    CleaningResult(
                                        packageName = packageName,
                                        status = CleaningStatus.SUCCESS
                                    )
                                )
                            } else {
                                continuation.resume(
                                    CleaningResult(
                                        packageName = packageName,
                                        status = CleaningStatus.FAILED,
                                        errorMessage = "System failed to clear cache for $packageName"
                                    )
                                )
                            }
                        }
                    }
                }

                try {
                    val pm = getPackageManager()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pm.deleteApplicationCacheFilesAsUser(packageName, userId, observer)
                    } else {
                        pm.deleteApplicationCacheFiles(packageName, observer)
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(
                            CleaningResult(
                                packageName = packageName,
                                status = CleaningStatus.FAILED,
                                errorMessage = e.message
                            )
                        )
                    }
                }
            }
        }

        result ?: CleaningResult(
            packageName = packageName,
            status = CleaningStatus.FAILED,
            errorMessage = "Cache cleaning operation timed out after ${timeoutMs}ms"
        )
    }
}
```

---

### Pendekatan 2 (Fallback / Root Identity): Pembersihan Direktori Cache via `Shizuku.newProcess`

Jika Shizuku berjalan di bawah **Root (UID 0)** atau API `deleteApplicationCacheFiles` diblokir oleh vendor OEM tertentu:

```kotlin
package com.example.cachecleaner.data.cleaner

import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuShellCacheCleaner {

    private val packageRegex = Regex("^[a-zA-Z0-9_.]+$")

    suspend fun clearCacheViaShell(packageName: String): CleaningResult = withContext(Dispatchers.IO) {
        // 1. Sanitasi input mencegah command injection
        if (!packageRegex.matches(packageName)) {
            return@withContext CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = "Invalid package name format"
            )
        }

        try {
            // HANYA hapus isi direktori cache, JANGAN pernah sentuh databases atau shared_prefs!
            val commands = arrayOf(
                "sh", "-c",
                """
                rm -rf /data/user/0/$packageName/cache/*
                rm -rf /data/user/0/$packageName/code_cache/*
                rm -rf /sdcard/Android/data/$packageName/cache/*
                sync
                """.trimIndent()
            )

            val process = Shizuku.newProcess(commands, null, null)
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                CleaningResult(packageName = packageName, status = CleaningStatus.SUCCESS)
            } else {
                val errorMsg = BufferedReader(InputStreamReader(process.errorStream)).readText()
                CleaningResult(
                    packageName = packageName,
                    status = CleaningStatus.FAILED,
                    errorMessage = errorMsg.ifBlank { "Exit code: $exitCode" }
                )
            }
        } catch (e: Exception) {
            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = e.message
            )
        }
    }
}
```

---

## 5. Prosedur Verifikasi Delta Cache

Sesuai aturan di **[`PRD.md`](file:///home/dwnt/Projects/Android-App/cache_cleaner/PRD.md)**, aplikasi tidak boleh mengklaim cache berkurang hanya karena perintah selesai dijalankan. Aplikasi wajib memverifikasi storage delta:

```
┌──────────────────────────────────────┐
│ 1. Catat Cache Awal                  │
│    StorageStatsManager queryStats    │
│    -> beforeCacheBytes               │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│ 2. Eksekusi Shizuku Cache Cleaner    │
│    deleteApplicationCacheFilesAsUser │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│ 3. Catat Cache Akhir & Delta         │
│    afterCacheBytes                   │
│    freedBytes = before - after       │
└──────────────────────────────────────┘
```

```kotlin
suspend fun cleanAndVerify(
    storageStatsRepo: StorageStatsRepository,
    shizukuCleaner: ShizukuCacheCleaner,
    packageName: String
): CleaningResult {
    val beforeCache = storageStatsRepo.getCacheBytes(packageName) ?: 0L
    val cleanResult = shizukuCleaner.clearCache(packageName)

    if (cleanResult.status == CleaningStatus.SUCCESS) {
        val afterCache = storageStatsRepo.getCacheBytes(packageName) ?: 0L
        val freed = maxOf(0L, beforeCache - afterCache)
        return cleanResult.copy(freedBytes = freed)
    }

    return cleanResult
}
```

---

## 6. Aturan Keamanan Mutlak (Zero Data Loss Policy)

> [!CAUTION]
> **ATURAN WAJIB (STRICT SAFETY POLICY):**
> 1. **DILARANG MENGGUNAKAN `pm clear`**: Perintah `pm clear <package>` akan menghapus SELURUH data pengguna, database Room/SQLite, login token, dan Shared Preferences.
> 2. **HANYA TARGET DIREKTORI CACHE**: Direktori yang diizinkan untuk dibersihkan hanyalah:
>    - `cacheDir` (`/data/user/0/<pkg>/cache/`)
>    - `code_cache` (`/data/user/0/<pkg>/code_cache/`)
>    - `externalCacheDir` (`/sdcard/Android/data/<pkg>/cache/`)
> 3. **VALIDASI REGEX PACKAGE NAME**: Selalu validasi format nama package sebelum mengeksekusi perintah IPC atau shell.
> 4. **TIMEOUT & CANCELLATION**: Semua pemanggilan asynchronous Shizuku wajib dibungkus dengan batas waktu (`withTimeoutOrNull`) agar tidak menyebabkan ANR (Application Not Responding).

---

## 7. Rangkuman Integrasi Arsitektur

File-file yang akan terhubung dalam arsitektur Cache Cleaner:

| Komponen | Lokasi File | Fungsi |
|---|---|---|
| **ShizukuManager** | `core/access/ShizukuManager.kt` | Monitor binder lifecycle, permission checks, status StateFlow |
| **ShizukuCleaner** | `data/cleaner/ShizukuCleaner.kt` | Implementasi `CacheCleanerEngine` menggunakan `ShizukuBinderWrapper` |
| **IPackageManager AIDL** | `src/main/aidl/android/content/pm/` | Stub AIDL untuk pemanggilan `deleteApplicationCacheFilesAsUser` |
| **CleanerRepository** | `data/cleaner/CleanerRepositoryImpl.kt` | Routing otomatis antara Standard, Shizuku, dan Root |
