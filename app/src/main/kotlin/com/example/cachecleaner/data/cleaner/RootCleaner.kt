package com.example.cachecleaner.data.cleaner

import com.example.cachecleaner.core.access.RootManager
import com.example.cachecleaner.core.shell.ShellExecutor
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
            // STRICT SAFETY: Delete ONLY cache directories. NEVER touch databases or shared_prefs!
            val command = arrayOf(
                "su", "-c",
                """
                rm -rf /data/user/0/$packageName/cache/*
                rm -rf /data/user/0/$packageName/code_cache/*
                rm -rf /sdcard/Android/data/$packageName/cache/*
                sync
                """.trimIndent()
            )

            val result = ShellExecutor.execute(command, timeoutMs = 8_000L)
            if (result.isSuccess) {
                CleaningResult(packageName = packageName, status = CleaningStatus.SUCCESS)
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
}
