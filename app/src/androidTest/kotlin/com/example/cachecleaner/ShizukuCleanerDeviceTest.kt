package com.example.cachecleaner

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.data.cleaner.ShizukuCleaner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShizukuCleanerDeviceTest {

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ShizukuManager.init(context)
    }

    @Test
    fun testClearCacheDoesNotThrow() = runBlocking {
        val cleaner = ShizukuCleaner()
        val result = cleaner.clearCache("com.thatoneapp.cleaner")
        assertNotNull(result)
        println("ClearCache result: status=${result.status}, error=${result.errorMessage}")
    }
}
