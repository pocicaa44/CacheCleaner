package com.example.cachecleaner.core.utils

import java.util.Locale

object FileSizeFormatter {

    fun format(bytes: Long?): String {
        if (bytes == null || bytes < 0) return "Unavailable"
        if (bytes == 0L) return "0 B"

        val kilo = 1024.0
        val mega = kilo * 1024
        val giga = mega * 1024
        val tera = giga * 1024

        val doubleBytes = bytes.toDouble()

        return when {
            doubleBytes >= tera -> String.format(Locale.US, "%.2f TB", doubleBytes / tera)
            doubleBytes >= giga -> String.format(Locale.US, "%.2f GB", doubleBytes / giga)
            doubleBytes >= mega -> String.format(Locale.US, "%.1f MB", doubleBytes / mega)
            doubleBytes >= kilo -> String.format(Locale.US, "%.1f KB", doubleBytes / kilo)
            else -> "$bytes B"
        }
    }
}
