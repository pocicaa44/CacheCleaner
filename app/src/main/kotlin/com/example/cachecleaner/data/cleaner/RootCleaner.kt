package com.example.cachecleaner.data.cleaner

import com.example.cachecleaner.core.access.RootManager
import com.example.cachecleaner.core.shell.ShellExecutor
import com.example.cachecleaner.core.utils.CacheCleaningScript
import com.example.cachecleaner.core.utils.PackageValidator
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RootCleaner : CacheCleanerEngine {

    override val accessMode: AccessMode = AccessMode.ROOT

    override suspend fun checkAvailability(): AccessStatus {
        return RootManager.checkRootAvailability()
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
                errorMessage = "Root access not available or superuser permission denied"
            )
        }

        try {
            // STRICT SAFETY: Target ONLY cache and code_cache directories.
            // NEVER touch databases, shared_prefs, files, or credential stores!
            val script = CacheCleaningScript.buildCacheCleanScript(packageName)
            val command = arrayOf("su", "-c", script)
            val result = ShellExecutor.execute(command, timeoutMs = 15_000L)

            if (result.isSuccess) {
                val freedBytes = CacheCleaningScript.parseFreedBytes(result.stdout)
                CleaningResult(
                    packageName = packageName,
                    status = CleaningStatus.SUCCESS,
                    freedBytes = freedBytes
                )
            } else {
                CleaningResult(
                    packageName = packageName,
                    status = CleaningStatus.FAILED,
                    errorMessage = result.stderr.ifBlank { "Root execution failed (exit code ${result.exitCode})" }
                )
            }
        } catch (e: Exception) {
            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = e.message ?: "Failed executing root command"
            )
        }
    }

    suspend fun clearBatch(packageNames: List<String>): CleaningResult = withContext(Dispatchers.IO) {
        if (packageNames.isEmpty()) {
            trimAllCaches()
            return@withContext CleaningResult(packageName = "all", status = CleaningStatus.SUCCESS)
        }

        if (checkAvailability() != AccessStatus.AVAILABLE) {
            return@withContext CleaningResult(
                packageName = "batch",
                status = CleaningStatus.PERMISSION_DENIED,
                errorMessage = "Root access not available or superuser permission denied"
            )
        }

        try {
            val script = CacheCleaningScript.buildBatchCacheCleanScript(packageNames)
            val command = arrayOf("su", "-c", script)
            val result = ShellExecutor.execute(command, timeoutMs = 25_000L)

            if (result.isSuccess) {
                val freedBytes = CacheCleaningScript.parseFreedBytes(result.stdout)
                CleaningResult(
                    packageName = "batch",
                    status = CleaningStatus.SUCCESS,
                    freedBytes = freedBytes
                )
            } else {
                CleaningResult(
                    packageName = "batch",
                    status = CleaningStatus.FAILED,
                    errorMessage = result.stderr.ifBlank { "Root batch execution failed (exit code ${result.exitCode})" }
                )
            }
        } catch (e: Exception) {
            CleaningResult(
                packageName = "batch",
                status = CleaningStatus.FAILED,
                errorMessage = e.message ?: "Failed executing root batch command"
            )
        }
    }

    suspend fun trimAllCaches() = withContext(Dispatchers.IO) {
        try {
            ShellExecutor.execute(
                arrayOf("su", "-c", "pm trim-caches 999999999999 2>/dev/null; sync"),
                timeoutMs = 10_000L
            )
        } catch (_: Exception) {
            // Non-fatal global trim
        }
    }
}
