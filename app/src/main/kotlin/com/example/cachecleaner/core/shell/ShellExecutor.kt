package com.example.cachecleaner.core.shell

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.InputStreamReader

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
                val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
                val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

                val exitCode = process.waitFor()
                val stdout = stdoutReader.readText()
                val stderr = stderrReader.readText()

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
