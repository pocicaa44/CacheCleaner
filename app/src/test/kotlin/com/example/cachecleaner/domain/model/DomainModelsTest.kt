package com.example.cachecleaner.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelsTest {

    @Test
    fun testStorageInfoTotalCalculation() {
        val storage = StorageInfo(
            appBytes = 1000L,
            dataBytes = 2000L,
            cacheBytes = 3000L
        )

        assertEquals(6000L, storage.totalBytes)
        assertEquals(3000L, storage.cacheBytes)
    }

    @Test
    fun testCleaningResultModel() {
        val successResult = CleaningResult(
            packageName = "com.example.app",
            status = CleaningStatus.SUCCESS,
            freedBytes = 1024L
        )

        assertEquals("com.example.app", successResult.packageName)
        assertEquals(CleaningStatus.SUCCESS, successResult.status)
        assertEquals(1024L, successResult.freedBytes)
    }

    @Test
    fun testAppInfoModel() {
        val app = AppInfo(
            packageName = "com.example.test",
            appName = "Test App",
            isSystemApp = false,
            storageInfo = StorageInfo(500L, 500L, 500L)
        )

        assertEquals("com.example.test", app.packageName)
        assertFalse(app.isSystemApp)
        assertEquals(1500L, app.storageInfo?.totalBytes)
    }
}
