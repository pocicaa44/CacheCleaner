package com.example.cachecleaner.core.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageValidatorTest {

    @Test
    fun testValidPackageNames() {
        assertTrue(PackageValidator.isValidPackageName("com.example.app"))
        assertTrue(PackageValidator.isValidPackageName("com.google.android.youtube"))
        assertTrue(PackageValidator.isValidPackageName("moe.shizuku.privileged.api"))
        assertTrue(PackageValidator.isValidPackageName("a.b.c_123"))
    }

    @Test
    fun testInvalidPackageNames() {
        assertFalse(PackageValidator.isValidPackageName(null))
        assertFalse(PackageValidator.isValidPackageName(""))
        assertFalse(PackageValidator.isValidPackageName("   "))
        assertFalse(PackageValidator.isValidPackageName("com.example.app; rm -rf /"))
        assertFalse(PackageValidator.isValidPackageName("com.example.app && echo 1"))
        assertFalse(PackageValidator.isValidPackageName("com.example.app|cat"))
        assertFalse(PackageValidator.isValidPackageName("com.example/app"))
        assertFalse(PackageValidator.isValidPackageName("com.example app"))
    }
}
