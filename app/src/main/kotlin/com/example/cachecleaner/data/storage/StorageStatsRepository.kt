package com.example.cachecleaner.data.storage

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import android.os.storage.StorageManager
import com.example.cachecleaner.domain.model.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageStatsRepository(private val context: Context) {

    private val storageStatsManager: StorageStatsManager? by lazy {
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
    }

    private val storageManager: StorageManager? by lazy {
        context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
    }

    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun getStorageInfoForPackage(packageName: String): StorageInfo? = withContext(Dispatchers.IO) {
        val ssm = storageStatsManager ?: return@withContext null
        if (!hasUsageStatsPermission()) return@withContext null

        try {
            val uuid = StorageManager.UUID_DEFAULT
            val user = Process.myUserHandle()
            val stats = ssm.queryStatsForPackage(uuid, packageName, user)

            StorageInfo(
                appBytes = stats.appBytes,
                dataBytes = stats.dataBytes,
                cacheBytes = stats.cacheBytes
            )
        } catch (e: Exception) {
            null
        }
    }
}
