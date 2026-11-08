package com.example.cachecleaner.data.cleaner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StandardCleaner(private val context: Context) : CacheCleanerEngine {

    override val accessMode: AccessMode = AccessMode.STANDARD

    override suspend fun checkAvailability(): AccessStatus = AccessStatus.AVAILABLE

    override suspend fun clearCache(packageName: String): CleaningResult = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.SUCCESS,
                errorMessage = "Opened app settings for manual cache clearing"
            )
        } catch (e: Exception) {
            CleaningResult(
                packageName = packageName,
                status = CleaningStatus.FAILED,
                errorMessage = e.message ?: "Failed to open application settings"
            )
        }
    }
}
