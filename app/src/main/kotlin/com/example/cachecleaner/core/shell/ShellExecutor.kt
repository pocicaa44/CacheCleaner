package com.example.cachecleaner.core.shell

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val isTimeout: Boolean = false
) {
    val isSuccess: Boolean get() = exitCode == 0 && !isTimeout
}

object ShellExecutor {

    suspend fun execute(
        command: Array<String>,
        timeoutMs: Long = 10_000L
    ): ShellResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs) {
            try {
                val process = ProcessBuilder(*command).start()

                // Read stdout and stderr concurrently to prevent pipe buffer deadlock
                val stdoutDeferred = async(Dispatchers.IO) {
                    process.inputStream.bufferedReader().use { it.readText() }
                }
                val stderrDeferred = async(Dispatchers.IO) {
                    process.errorStream.bufferedReader().use { it.readText() }
                }

                val exitCode = process.waitFor()
                val stdout = stdoutDeferred.await()
                val stderr = stderrDeferred.await()

                ShellResult(exitCode = exitCode, stdout = stdout, stderr = stderr)
            } catch (e: Exception) {
                ShellResult(
                    exitCode = -1,
                    stdout = "",
                    stderr = e.message ?: "Unknown process execution error"
                )
            }
        }

        result ?: ShellResult(
            exitCode = -1,
            stdout = "",
            stderr = "Command execution timed out after ${timeoutMs}ms",
            isTimeout = true
        )
    }
}
