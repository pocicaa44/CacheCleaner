package com.example.cachecleaner.data.cleaner

import android.content.pm.IPackageDataObserver
import android.os.Build
import android.os.IBinder
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.core.utils.PackageValidator
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method

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

        clearCacheViaBinder(packageName)
    }

    private fun clearCacheViaBinder(packageName: String): CleaningResult {
        return try {
            val pm = getPackageManager()
            val pmClass = pm.javaClass

            val methodWithUser: Method? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pmClass.methods.firstOrNull {
                    it.name == "deleteApplicationCacheFilesAsUser" && it.parameterTypes.size == 3
                }
            } else null

            if (methodWithUser != null) {
                methodWithUser.isAccessible = true
                try {
                    methodWithUser.invoke(pm, packageName, 0, null)
                } catch (e: Throwable) {
                    val dummyObserver = object : IPackageDataObserver.Stub() {
                        override fun onRemoveCompleted(pkg: String?, succeeded: Boolean) {}
                    }
                    methodWithUser.invoke(pm, packageName, 0, dummyObserver)
                }
            } else {
                val methodFallback = pmClass.methods.firstOrNull {
                    it.name == "deleteApplicationCacheFiles" && it.parameterTypes.size == 2
                } ?: throw NoSuchMethodException("No deleteApplicationCacheFiles method found on IPackageManager")
                methodFallback.isAccessible = true
                try {
                    methodFallback.invoke(pm, packageName, null)
                } catch (e: Throwable) {
                    val dummyObserver = object : IPackageDataObserver.Stub() {
                        override fun onRemoveCompleted(pkg: String?, succeeded: Boolean) {}
                    }
                    methodFallback.invoke(pm, packageName, dummyObserver)
                }
            }

            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.SUCCESS
            )
        } catch (t: Throwable) {
            val rootCause = t.cause ?: t
            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = rootCause.message ?: "Binder error: ${rootCause::class.java.simpleName}"
            )
        }
    }

    /**
     * Request system-wide cache trim via IPackageManager.freeStorageAndNotify.
     * Passing a high ideal storage size triggers PMS to delete LRU cache across all applications.
     */
    fun trimAllCaches() {
        try {
            val pm = getPackageManager()
            val pmClass = pm.javaClass
            val freeStorageMethod = pmClass.methods.firstOrNull {
                it.name == "freeStorageAndNotify" && it.parameterTypes.size == 4
            }
            freeStorageMethod?.isAccessible = true
            freeStorageMethod?.invoke(pm, null, Long.MAX_VALUE, 0, null)
        } catch (e: Throwable) {
            // Ignore non-fatal global trim exception
        }
    }
}
