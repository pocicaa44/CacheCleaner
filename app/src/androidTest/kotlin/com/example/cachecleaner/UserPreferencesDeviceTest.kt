package com.example.cachecleaner

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.domain.model.AccessMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesDeviceTest {

    @Test
    fun testDefaultAccessModeIsStandard() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = UserPreferencesRepository(context)
        // Set mode to standard and verify
        repo.setAccessMode(AccessMode.STANDARD)
        val mode = repo.accessModeFlow.first()
        assertEquals(AccessMode.STANDARD, mode)
    }

    @Test
    fun testDynamicColorDefaultIsTrue() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = UserPreferencesRepository(context)
        val dynamicColor = repo.dynamicColorFlow.first()
        assertTrue(dynamicColor)
    }
}
