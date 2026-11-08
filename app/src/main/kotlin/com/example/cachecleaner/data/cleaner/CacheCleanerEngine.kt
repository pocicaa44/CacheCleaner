package com.example.cachecleaner.data.cleaner

import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult

interface CacheCleanerEngine {
    val accessMode: AccessMode

    suspend fun checkAvailability(): AccessStatus

    suspend fun clearCache(packageName: String): CleaningResult

    suspend fun clearCaches(
        packageNames: List<String>,
        onProgress: suspend (current: Int, total: Int, currentPackage: String) -> Unit
    ): List<CleaningResult> {
        val results = mutableListOf<CleaningResult>()
        val total = packageNames.size

        packageNames.forEachIndexed { index, pkg ->
            onProgress(index + 1, total, pkg)
            val result = clearCache(pkg)
            results.add(result)
        }

        return results
    }
}
