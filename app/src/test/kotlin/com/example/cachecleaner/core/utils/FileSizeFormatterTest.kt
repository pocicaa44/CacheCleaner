package com.example.cachecleaner.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FileSizeFormatterTest {

    @Test
    fun testZeroBytes() {
        assertEquals("0 B", FileSizeFormatter.format(0L))
    }

    @Test
    fun testBytes() {
        assertEquals("512 B", FileSizeFormatter.format(512L))
    }

    @Test
    fun testKilobytes() {
        assertEquals("1.0 KB", FileSizeFormatter.format(1024L))
        assertEquals("1.5 KB", FileSizeFormatter.format(1536L))
    }

    @Test
    fun testMegabytes() {
        assertEquals("1.0 MB", FileSizeFormatter.format(1024L * 1024L))
        assertEquals("645.0 MB", FileSizeFormatter.format(645L * 1024L * 1024L))
    }

    @Test
    fun testGigabytes() {
        assertEquals("1.00 GB", FileSizeFormatter.format(1024L * 1024L * 1024L))
        assertEquals("2.84 GB", FileSizeFormatter.format((2.84 * 1024.0 * 1024.0 * 1024.0).toLong()))
    }

    @Test
    fun testNullOrNegative() {
        assertEquals("Unavailable", FileSizeFormatter.format(null))
        assertEquals("Unavailable", FileSizeFormatter.format(-1L))
    }
}
