package com.example.cachecleaner.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.cachecleaner.core.utils.FileSizeFormatter
import com.example.cachecleaner.domain.model.AccessMode

@Composable
fun ConfirmCleanDialog(
    selectedCount: Int,
    estimatedCacheBytes: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    accessMode: AccessMode = AccessMode.STANDARD
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Clear cache?")
        },
        text = {
            val formattedSize = FileSizeFormatter.format(estimatedCacheBytes)
            val description = if (accessMode == AccessMode.STANDARD) {
                "Open system settings for $selectedCount apps (~$formattedSize) to clear cache manually.\n\n" +
                "Personal data, accounts, and databases will not be affected."
            } else {
                "Clear cache for $selectedCount apps (~$formattedSize)?\n\n" +
                "Personal data, accounts, and databases will not be affected."
            }
            Text(text = description)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (accessMode == AccessMode.STANDARD) "Open Settings" else "Clear Cache")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
