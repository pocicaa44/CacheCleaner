package com.example.cachecleaner.data.cleaner

import android.content.pm.IPackageDataObserver
import android.os.Build
import android.os.IBinder
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.core.utils.CacheCleaningScript
import com.example.cachecleaner.core.utils.PackageValidator
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method
import kotlin.coroutines.resume

class ShizukuCleaner : CacheCleanerEngine {

    override val accessMode: AccessMode = AccessMode.SHIZUKU

    override suspend fun checkAvailability(): AccessStatus {
        ShizukuManager.updateStatus()
        return ShizukuManager.accessStatus.value
    }

    private fun getPackageManager(): Any {
        val binder: IBinder = SystemServiceHelper.getSystemService("package")
            ?: throw IllegalStateException("Failed to acquire package service binder from Shizuku")
        val stubClass = Class.forName("android.content.pm.IPackageManager\$Stub")
        val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
        return asInterfaceMethod.invoke(null, ShizukuBinderWrapper(binder))
            ?: throw IllegalStateException("IPackageManager.Stub.asInterface returned null")
    }

    private fun newShizukuProcess(command: Array<String>): Process {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(null, command, null, null) as Process
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

        // Approach 1: Primary System Binder Proxy with asynchronous callback observation
        val binderResult = clearCacheViaBinder(packageName)
        if (binderResult.status == CleaningStatus.SUCCESS) {
            return@withContext binderResult
        }

        // Approach 2: Fallback via Shizuku shell process if Binder API is blocked by OEM or permissions
        clearCacheViaShizukuShell(packageName)
    }

    private suspend fun clearCacheViaBinder(packageName: String): CleaningResult = withTimeoutOrNull(10_000L) {
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
                val pmClass = pm.javaClass

                val methodWithUser: Method? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    pmClass.methods.firstOrNull {
                        it.name == "deleteApplicationCacheFilesAsUser" && it.parameterTypes.size == 3
                    }
                } else null

                if (methodWithUser != null) {
                    methodWithUser.isAccessible = true
                    methodWithUser.invoke(pm, packageName, 0, observer)
                } else {
                    val methodFallback = pmClass.methods.firstOrNull {
                        it.name == "deleteApplicationCacheFiles" && it.parameterTypes.size == 2
                    } ?: throw NoSuchMethodException("No deleteApplicationCacheFiles method found on IPackageManager")
                    methodFallback.isAccessible = true
                    methodFallback.invoke(pm, packageName, observer)
                }
            } catch (t: Throwable) {
                if (continuation.isActive) {
                    val rootCause = t.cause ?: t
                    continuation.resume(
                        CleaningResult(
                            packageName = packageName,
                            status = CleaningStatus.FAILED,
                            errorMessage = rootCause.message ?: "Binder error: ${rootCause::class.java.simpleName}"
                        )
                    )
                }
            }
        }
    } ?: CleaningResult(
        packageName = packageName,
        status = CleaningStatus.FAILED,
        errorMessage = "Binder cache clearing timed out after 10000ms"
    )

    private suspend fun clearCacheViaShizukuShell(packageName: String): CleaningResult = withContext(Dispatchers.IO) {
        try {
            val script = CacheCleaningScript.buildCacheCleanScript(packageName)
            val process = newShizukuProcess(arrayOf("sh", "-c", script))
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                val freedBytes = CacheCleaningScript.parseFreedBytes(stdout)
                CleaningResult(
                    packageName = packageName,
                    status = CleaningStatus.SUCCESS,
                    freedBytes = freedBytes
                )
            } else {
                CleaningResult(
                    packageName = packageName,
                    status = CleaningStatus.FAILED,
                    errorMessage = stderr.ifBlank { "Shizuku shell failed with exit code $exitCode" }
                )
            }
        } catch (e: Exception) {
            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = e.message ?: "Failed executing Shizuku process"
            )
        }
    }

    suspend fun clearBatch(packageNames: List<String>): CleaningResult = withContext(Dispatchers.IO) {
        if (checkAvailability() != AccessStatus.AVAILABLE) {
            return@withContext CleaningResult(
                packageName = "batch",
                status = CleaningStatus.PERMISSION_DENIED,
                errorMessage = "Shizuku permission not granted or service unavailable"
            )
        }

        // 1. System-wide cache trim via PMS
        trimAllCaches()

        // 2. Direct directory wipe for targeted packages via Shizuku shell
        var freedBytes = 0L
        if (packageNames.isNotEmpty()) {
            try {
                val script = CacheCleaningScript.buildBatchCacheCleanScript(packageNames)
                val process = newShizukuProcess(arrayOf("sh", "-c", script))
                val stdout = process.inputStream.bufferedReader().use { it.readText() }
                process.waitFor()
                freedBytes = CacheCleaningScript.parseFreedBytes(stdout)
            } catch (_: Exception) {}
        }

        CleaningResult(
            packageName = "batch",
            status = CleaningStatus.SUCCESS,
            freedBytes = freedBytes
        )
    }

    /**
     * Request system-wide cache trim via IPackageManager.freeStorageAndNotify or pm trim-caches.
     */
    fun trimAllCaches() {
        try {
            val pm = getPackageManager()
            val pmClass = pm.javaClass
            val freeStorageMethod = pmClass.methods.firstOrNull {
                it.name == "freeStorageAndNotify" && it.parameterTypes.size == 4
            }
            if (freeStorageMethod != null) {
                freeStorageMethod.isAccessible = true
                val observer = object : IPackageDataObserver.Stub() {
                    override fun onRemoveCompleted(pkg: String?, succeeded: Boolean) {}
                }
                freeStorageMethod.invoke(pm, null, Long.MAX_VALUE, 0, observer)
                return
            }
        } catch (_: Throwable) {}

        // Fallback via shell pm trim-caches
        try {
            val process = newShizukuProcess(
                arrayOf("sh", "-c", "pm trim-caches 999999999999 2>/dev/null; sync")
            )
            process.waitFor()
        } catch (_: Throwable) {}
    }
}
