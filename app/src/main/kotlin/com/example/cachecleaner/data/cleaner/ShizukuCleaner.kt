package com.example.cachecleaner.data.cleaner

import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageManager
import android.os.Build
import android.os.IBinder
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.core.utils.PackageValidator
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import kotlin.coroutines.resume

class ShizukuCleaner : CacheCleanerEngine {

    override val accessMode: AccessMode = AccessMode.SHIZUKU

    override suspend fun checkAvailability(): AccessStatus {
        ShizukuManager.updateStatus()
        return ShizukuManager.accessStatus.value
    }

    private fun getPackageManager(): IPackageManager {
        val binder: IBinder = SystemServiceHelper.getSystemService("package")
            ?: throw IllegalStateException("Failed to acquire package service binder from Shizuku")
        return IPackageManager.Stub.asInterface(ShizukuBinderWrapper(binder))
    }

    override suspend fun clearCache(packageName: String): CleaningResult = withContext(Dispatchers.IO) {
        if (!PackageValidator.isValidPackageName(packageName)) {
            return@withContext CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = "Invalid package name format"
            )
        }

        if (checkAvailability() != AccessStatus.AVAILABLE) {
            return@withContext CleaningResult(
                packageName = packageName,
                status = CleaningStatus.PERMISSION_DENIED,
                errorMessage = "Shizuku permission not granted or service unavailable"
            )
        }

        // Primary mechanism: IPackageManager.deleteApplicationCacheFilesAsUser
        clearCacheViaBinder(packageName)
    }

    private suspend fun clearCacheViaBinder(
        packageName: String,
        timeoutMs: Long = 8_000L
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
                        pm.deleteApplicationCacheFilesAsUser(packageName, 0, observer)
                    } else {
                        pm.deleteApplicationCacheFiles(packageName, observer)
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(
                            CleaningResult(
                                packageName = packageName,
                                status = CleaningStatus.FAILED,
                                errorMessage = e.message ?: "Binder transaction exception"
                            )
                        )
                    }
                }
            }
        }

        result ?: CleaningResult(
            packageName = packageName,
            status = CleaningStatus.FAILED,
            errorMessage = "Operation timed out after ${timeoutMs}ms"
        )
    }
}
