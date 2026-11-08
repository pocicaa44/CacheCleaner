package com.example.cachecleaner.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.cachecleaner.core.utils.FileSizeFormatter

@Composable
fun ConfirmCleanDialog(
    selectedCount: Int,
    estimatedCacheBytes: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Clear selected cache?")
        },
        text = {
            Text(
                text = "You are about to clear cache for $selectedCount application(s) " +
                    "(~${FileSizeFormatter.format(estimatedCacheBytes)}).\n\n" +
                    "Your personal data, accounts, login sessions, and database files will NOT be affected."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Clear Cache")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
