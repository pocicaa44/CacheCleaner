package com.example.cachecleaner.data.cleaner

import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ShizukuCleanerTest {

    @Test
    fun testAccessModeIsShizuku() {
        val cleaner = ShizukuCleaner()
        assertEquals(AccessMode.SHIZUKU, cleaner.accessMode)
    }

    @Test
    fun testInvalidPackageReturnsFailed() = runTest {
        val cleaner = ShizukuCleaner()
        val result = cleaner.clearCache("invalid package; rm -rf /")
        assertEquals(CleaningStatus.FAILED, result.status)
        assertEquals("Invalid package name format", result.errorMessage)
    }

    @Test
    fun testEmptyPackageReturnsFailed() = runTest {
        val cleaner = ShizukuCleaner()
        val result = cleaner.clearCache("")
        assertEquals(CleaningStatus.FAILED, result.status)
        assertEquals("Invalid package name format", result.errorMessage)
    }
}
