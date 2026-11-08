package com.example.cachecleaner.domain.model

import android.graphics.drawable.Drawable

data class StorageInfo(
    val appBytes: Long = 0L,
    val dataBytes: Long = 0L,
    val cacheBytes: Long = 0L
) {
    val totalBytes: Long
        get() = appBytes + dataBytes + cacheBytes
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val storageInfo: StorageInfo? = null,
    val icon: Drawable? = null
)

data class CleaningResult(
    val packageName: String,
    val status: CleaningStatus,
    val freedBytes: Long = 0L,
    val errorMessage: String? = null
)

data class CacheCleanedEvent(
    val packages: Set<String> = emptySet(),
    val freedBytes: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
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
    NOT_INSTALLED,
    DENIED,
    UNSUPPORTED
}

enum class SortOption {
    SIZE_DESC,
    SIZE_ASC,
    NAME_ASC,
    NAME_DESC
}

enum class AppFilter {
    ALL,
    USER,
    SYSTEM
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}
