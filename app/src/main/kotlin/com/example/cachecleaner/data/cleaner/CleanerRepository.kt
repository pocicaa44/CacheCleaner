package com.example.cachecleaner.data.cleaner

import android.content.Context
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.data.storage.StorageStatsRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CleanerRepository(
    private val context: Context,
    private val storageStatsRepository: StorageStatsRepository
) {

    private val standardCleaner = StandardCleaner(context)
    private val shizukuCleaner = ShizukuCleaner()
    private val rootCleaner = RootCleaner()

    fun getEngine(mode: AccessMode): CacheCleanerEngine {
        return when (mode) {
            AccessMode.SHIZUKU -> shizukuCleaner
            AccessMode.ROOT -> rootCleaner
            AccessMode.STANDARD -> standardCleaner
        }
    }

    suspend fun checkModeAvailability(mode: AccessMode): AccessStatus {
        return getEngine(mode).checkAvailability()
    }

    suspend fun cleanAndVerify(
        engine: CacheCleanerEngine,
        packageName: String
    ): CleaningResult = withContext(Dispatchers.IO) {
        val beforeStats = storageStatsRepository.getStorageInfoForPackage(packageName)
        val beforeCache = beforeStats?.cacheBytes ?: 0L

        val result = engine.clearCache(packageName)

        if (result.status == CleaningStatus.SUCCESS) {
            // Give storage/filesystem a short interval to reflect updated usage
            delay(200)
            val afterStats = storageStatsRepository.getStorageInfoForPackage(packageName)
            val afterCache = afterStats?.cacheBytes ?: 0L
            val freed = maxOf(0L, beforeCache - afterCache)

            result.copy(freedBytes = freed)
        } else {
            result
        }
    }

    suspend fun cleanBatchAndVerify(
        engine: CacheCleanerEngine,
        packageNames: List<String>,
        onProgress: suspend (current: Int, total: Int, currentPackage: String) -> Unit
    ): List<CleaningResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<CleaningResult>()
        val total = packageNames.size

        packageNames.forEachIndexed { index, packageName ->
            onProgress(index + 1, total, packageName)
            val result = cleanAndVerify(engine, packageName)
            results.add(result)
        }

        // If using Shizuku, perform final global cache trim to free system-wide remnants
        if (engine is ShizukuCleaner && ShizukuManager.accessStatus.value == AccessStatus.AVAILABLE) {
            engine.trimAllCaches()
        }

        results
    }
}
