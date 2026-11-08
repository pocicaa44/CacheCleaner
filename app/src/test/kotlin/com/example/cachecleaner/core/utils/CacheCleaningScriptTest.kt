package com.example.cachecleaner.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CacheCleaningScriptTest {

    @Test
    fun testScriptOnlyTargetsCacheDirectories() {
        val script = CacheCleaningScript.buildCacheCleanScript("com.example.app")

        // Must target cache and code_cache
        assertTrue(script.contains("/data/data/com.example.app/cache"))
        assertTrue(script.contains("/data/data/com.example.app/code_cache"))
        assertTrue(script.contains("/data/user/*/com.example.app/cache"))
        assertTrue(script.contains("/data/user_de/*/com.example.app/cache"))
        assertTrue(script.contains("/data/media/*/Android/data/com.example.app/cache"))

        // Absolute Safety Mandate: Must NEVER contain databases, shared_prefs, files, or sensitive dirs
        assertFalse(script.contains("databases"))
        assertFalse(script.contains("shared_prefs"))
        assertFalse(script.contains("/files"))
        assertFalse(script.contains("keystore"))

        // Must preserve directory container
        assertTrue(script.contains("-mindepth 1 -delete"))
    }

    @Test
    fun testParseFreedBytes() {
        val output = "some random log\nFREED_KB:2048\n"
        val bytes = CacheCleaningScript.parseFreedBytes(output)
        assertEquals(2048L * 1024L, bytes)
    }

    @Test
    fun testParseFreedBytesZeroWhenMissing() {
        val output = "no freed line"
        val bytes = CacheCleaningScript.parseFreedBytes(output)
        assertEquals(0L, bytes)
    }

    @Test
    fun testBatchScriptContainsAllValidPackagesAndSafeDirectoriesOnly() {
        val packages = listOf("com.example.app1", "com.example.app2", "invalid package; rm")
        val script = CacheCleaningScript.buildBatchCacheCleanScript(packages)

        assertTrue(script.contains("com.example.app1"))
        assertTrue(script.contains("com.example.app2"))
        assertFalse(script.contains("invalid package"))

        // Absolute Safety: Must never touch sensitive directories
        assertFalse(script.contains("databases"))
        assertFalse(script.contains("shared_prefs"))
        assertFalse(script.contains("/files"))
        assertFalse(script.contains("keystore"))

        assertTrue(script.contains("pm trim-caches"))
    }
}
