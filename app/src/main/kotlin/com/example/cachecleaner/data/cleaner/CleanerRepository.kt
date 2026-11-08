package com.example.cachecleaner.data.cleaner

import android.content.Context
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.data.storage.StorageStatsRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CacheCleanedEvent
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class CleanerRepository(
    private val context: Context,
    private val storageStatsRepository: StorageStatsRepository
) {

    private val standardCleaner = StandardCleaner(context)
    private val shizukuCleaner = ShizukuCleaner()
    private val rootCleaner = RootCleaner()

    private val _cacheCleanedEvent = MutableSharedFlow<CacheCleanedEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val cacheCleanedEvent: SharedFlow<CacheCleanedEvent> = _cacheCleanedEvent.asSharedFlow()

    fun notifyCacheCleaned(packages: Set<String> = emptySet(), freedBytes: Long = 0L) {
        _cacheCleanedEvent.tryEmit(
            CacheCleanedEvent(
                packages = packages,
                freedBytes = freedBytes
            )
        )
    }

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
        packageName: String,
        notifyEvent: Boolean = true
    ): CleaningResult = withContext(Dispatchers.IO) {
        val beforeStats = storageStatsRepository.getStorageInfoForPackage(packageName)
        val beforeCache = beforeStats?.cacheBytes ?: 0L

        val result = engine.clearCache(packageName)

        val finalResult = if (result.status == CleaningStatus.SUCCESS) {
            if (engine.accessMode == AccessMode.STANDARD) {
                result
            } else {
                var afterCache = beforeCache
                // Poll briefly up to 3 attempts (150ms, 300ms, 450ms) to allow OS StorageStatsManager to reflect changes
                for (attempt in 1..3) {
                    delay(150L * attempt)
                    val afterStats = storageStatsRepository.getStorageInfoForPackage(packageName)
                    if (afterStats != null) {
                        afterCache = afterStats.cacheBytes
                        if (afterCache < beforeCache) {
                            break
                        }
                    }
                }

                val statFreed = maxOf(0L, beforeCache - afterCache)
                val effectiveFreed = when {
                    statFreed > 0L -> statFreed
                    result.freedBytes > 0L -> result.freedBytes
                    beforeCache > 0L -> beforeCache
                    else -> 0L
                }

                result.copy(freedBytes = effectiveFreed)
            }
        } else {
            result
        }

        if (notifyEvent && finalResult.status == CleaningStatus.SUCCESS) {
            notifyCacheCleaned(
                packages = setOf(packageName),
                freedBytes = finalResult.freedBytes
            )
        }

        finalResult
    }

    suspend fun cleanBatchAndVerify(
        engine: CacheCleanerEngine,
        packageNames: List<String>,
        onProgress: suspend (current: Int, total: Int, currentPackage: String) -> Unit = { _, _, _ -> }
    ): List<CleaningResult> = withContext(Dispatchers.IO) {
        if (packageNames.isEmpty()) return@withContext emptyList()

        val total = packageNames.size
        onProgress(1, total, "Clearing cache...")

        val batchResult = when (engine) {
            is RootCleaner -> engine.clearBatch(packageNames)
            is ShizukuCleaner -> engine.clearBatch(packageNames)
            else -> null
        }

        val results = if (batchResult != null) {
            onProgress(total, total, "Completed")
            packageNames.map { pkg ->
                CleaningResult(
                    packageName = pkg,
                    status = batchResult.status,
                    freedBytes = if (batchResult.freedBytes > 0) batchResult.freedBytes / total else 0L,
                    errorMessage = batchResult.errorMessage
                )
            }
        } else {
            // Standard engine fallback
            packageNames.mapIndexed { index, packageName ->
                onProgress(index + 1, total, packageName)
                cleanAndVerify(engine, packageName, notifyEvent = false)
            }
        }

        // Brief delay for filesystem sync and OS quota updates to settle
        delay(150)

        val successful = results.filter { it.status == CleaningStatus.SUCCESS }
        if (successful.isNotEmpty()) {
            val totalFreed = if (batchResult != null && batchResult.freedBytes > 0) {
                batchResult.freedBytes
            } else {
                results.sumOf { it.freedBytes }
            }
            notifyCacheCleaned(
                packages = successful.map { it.packageName }.toSet(),
                freedBytes = totalFreed
            )
        }

        results
    }
}
