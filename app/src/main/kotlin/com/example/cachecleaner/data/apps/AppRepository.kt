package com.example.cachecleaner.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

    suspend fun getInstalledApps(
        includeSystemApps: Boolean = false,
        filter: AppFilter = AppFilter.ALL,
        sortOption: SortOption = SortOption.CACHE_DESC,
        searchQuery: String = ""
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val installedApplications = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList<ApplicationInfo>()
        }

        val selfPackage = context.packageName

        val appList = installedApplications
            .filter { it.packageName != selfPackage }
            .map { appInfo ->
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    appInfo.packageName
                }

                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    null
                }

                val storageInfo = storageStatsRepository.getStorageInfoForPackage(appInfo.packageName)

                AppInfo(
                    packageName = appInfo.packageName,
                    appName = label,
                    isSystemApp = isSystem,
                    storageInfo = storageInfo,
                    icon = icon
                )
            }

        // Apply filters
        val filteredList = appList.filter { app ->
            val matchesSystemFilter = when {
                !includeSystemApps && app.isSystemApp -> false
                filter == AppFilter.USER -> !app.isSystemApp
                filter == AppFilter.SYSTEM -> app.isSystemApp
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            }

            matchesSystemFilter && matchesSearch
        }

        // Apply sorting
        when (sortOption) {
            SortOption.CACHE_DESC -> filteredList.sortedByDescending { it.storageInfo?.cacheBytes ?: -1L }
            SortOption.CACHE_ASC -> filteredList.sortedBy { it.storageInfo?.cacheBytes ?: Long.MAX_VALUE }
            SortOption.NAME_ASC -> filteredList.sortedBy { it.appName.lowercase() }
            SortOption.NAME_DESC -> filteredList.sortedByDescending { it.appName.lowercase() }
        }
    }

    suspend fun getAppDetails(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val label = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
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
