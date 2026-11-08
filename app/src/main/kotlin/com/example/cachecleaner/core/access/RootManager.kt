package com.example.cachecleaner.core.access

import com.example.cachecleaner.core.shell.ShellExecutor
import com.example.cachecleaner.domain.model.AccessStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object RootManager {

    suspend fun checkRootAvailability(): AccessStatus = withContext(Dispatchers.IO) {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )

        val binaryFound = paths.any { File(it).exists() } || checkSuInPath()
        if (!binaryFound) {
            return@withContext AccessStatus.NOT_INSTALLED
        }

        // Test su execution
        val result = ShellExecutor.execute(arrayOf("su", "-c", "id -u"), timeoutMs = 3000L)
        if (result.isSuccess && result.stdout.trim() == "0") {
            AccessStatus.AVAILABLE
        } else {
            AccessStatus.PERMISSION_REQUIRED
        }
    }

    private fun checkSuInPath(): Boolean {
        val pathEnv = System.getenv("PATH") ?: return false
        return pathEnv.split(":").any { dir ->
            File(dir, "su").exists()
        }
    }
}
