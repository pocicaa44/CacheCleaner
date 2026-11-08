package com.example.cachecleaner.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache
import com.example.cachecleaner.data.storage.StorageStatsRepository
import com.example.cachecleaner.domain.model.AppFilter
import com.example.cachecleaner.domain.model.AppInfo
import com.example.cachecleaner.domain.model.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val storageStatsRepository: StorageStatsRepository
) {

    private val packageManager: PackageManager = context.packageManager
    private val iconCache = LruCache<String, Drawable>(150)
    private val labelCache = LruCache<String, String>(250)

    suspend fun getInstalledApps(
        includeSystemApps: Boolean = false,
        filter: AppFilter = AppFilter.ALL,
        sortOption: SortOption = SortOption.CACHE_DESC,
        searchQuery: String = ""
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val installedApplications = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        val selfPackage = context.packageName
        val hasPermission = storageStatsRepository.hasUsageStatsPermission()
        val queryLower = searchQuery.trim().lowercase()

        // 1. Early filter: exclude self and unwanted system packages before expensive IPC/icon work
        val candidates = installedApplications.filter { appInfo ->
            if (appInfo.packageName == selfPackage) return@filter false
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            when {
                !includeSystemApps && isSystem -> false
                filter == AppFilter.USER -> !isSystem
                filter == AppFilter.SYSTEM -> isSystem
                else -> true
            }
        }

        // 2. Map only eligible packages, utilizing memory cache for icons and labels
        val appList = ArrayList<AppInfo>(candidates.size)
        for (appInfo in candidates) {
            val label = labelCache.get(appInfo.packageName) ?: run {
                try {
                    packageManager.getApplicationLabel(appInfo).toString().also {
                        labelCache.put(appInfo.packageName, it)
                    }
                } catch (e: Exception) {
                    appInfo.packageName
                }
            }

            // Quick check search filter before loading icons and querying storage
            if (queryLower.isNotEmpty()) {
                val matches = label.lowercase().contains(queryLower) || appInfo.packageName.lowercase().contains(queryLower)
                if (!matches) continue
            }

            val icon = iconCache.get(appInfo.packageName) ?: run {
                try {
                    packageManager.getApplicationIcon(appInfo).also {
                        iconCache.put(appInfo.packageName, it)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val storageInfo = storageStatsRepository.getStorageInfoForPackage(
                packageName = appInfo.packageName,
                hasPermission = hasPermission
            )

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            appList.add(
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = label,
                    isSystemApp = isSystem,
                    storageInfo = storageInfo,
                    icon = icon
                )
            )
        }

        // 3. Sort list in-place
        when (sortOption) {
            SortOption.CACHE_DESC -> appList.sortByDescending { it.storageInfo?.cacheBytes ?: -1L }
            SortOption.CACHE_ASC -> appList.sortBy { it.storageInfo?.cacheBytes ?: Long.MAX_VALUE }
            SortOption.NAME_ASC -> appList.sortBy { it.appName.lowercase() }
            SortOption.NAME_DESC -> appList.sortByDescending { it.appName.lowercase() }
        }

        appList
    }

    suspend fun getAppDetails(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val label = labelCache.get(packageName) ?: packageManager.getApplicationLabel(appInfo).toString().also {
                labelCache.put(packageName, it)
            }
            val icon = iconCache.get(packageName) ?: try {
                packageManager.getApplicationIcon(appInfo).also {
                    iconCache.put(packageName, it)
                }
            } catch (e: Exception) {
                null
            }
            val storageInfo = storageStatsRepository.getStorageInfoForPackage(packageName)

            AppInfo(
                packageName = packageName,
                appName = label,
                isSystemApp = isSystem,
                storageInfo = storageInfo,
                icon = icon
            )
        } catch (e: Exception) {
            null
        }
    }
}
