package com.example.cachecleaner.core.access

import com.example.cachecleaner.core.shell.ShellExecutor
import com.example.cachecleaner.domain.model.AccessStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object RootManager {

    private var cachedStatus: AccessStatus? = null
    private var lastCheckTime: Long = 0L
    private const val CACHE_VALIDITY_MS = 5000L

    suspend fun checkRootAvailability(forceRefresh: Boolean = false): AccessStatus = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedStatus != null && (now - lastCheckTime) < CACHE_VALIDITY_MS) {
            return@withContext cachedStatus!!
        }

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
            cachedStatus = AccessStatus.NOT_INSTALLED
            lastCheckTime = now
            return@withContext AccessStatus.NOT_INSTALLED
        }

        // Test su execution
        val result = ShellExecutor.execute(arrayOf("su", "-c", "id -u"), timeoutMs = 3000L)
        val status = if (result.isSuccess && result.stdout.lines().any { it.trim() == "0" }) {
            AccessStatus.AVAILABLE
        } else {
            AccessStatus.PERMISSION_REQUIRED
        }
        cachedStatus = status
        lastCheckTime = now
        status
    }

    private fun checkSuInPath(): Boolean {
        val pathEnv = System.getenv("PATH") ?: return false
        return pathEnv.split(":").any { dir ->
            File(dir, "su").exists()
        }
    }
}
