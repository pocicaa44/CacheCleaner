package com.example.cachecleaner.core.utils

object PackageValidator {

    private val packageRegex = Regex("^[a-zA-Z0-9_.]+$")

    fun isValidPackageName(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        if (packageName.length > 255) return false
        return packageRegex.matches(packageName)
    }
}
