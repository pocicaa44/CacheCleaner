package com.example.cachecleaner.data.storage

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
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

    private val defaultUuid = StorageManager.UUID_DEFAULT
    private val myUserHandle: UserHandle by lazy { Process.myUserHandle() }

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

    suspend fun getStorageInfoForPackage(
        packageName: String,
        hasPermission: Boolean? = null
    ): StorageInfo? = withContext(Dispatchers.IO) {
        val ssm = storageStatsManager ?: return@withContext null
        val permissionGranted = hasPermission ?: hasUsageStatsPermission()
        if (!permissionGranted) return@withContext null

        try {
            val stats = ssm.queryStatsForPackage(defaultUuid, packageName, myUserHandle)
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
