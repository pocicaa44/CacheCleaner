package com.example.cachecleaner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cachecleaner.core.utils.FileSizeFormatter
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.CleaningStatus

@Composable
fun CleanResultDialog(
    results: List<CleaningResult>,
    onDismiss: () -> Unit
) {
    val successCount = results.count { it.status == CleaningStatus.SUCCESS }
    val failedCount = results.count { it.status == CleaningStatus.FAILED || it.status == CleaningStatus.PERMISSION_DENIED }
    val totalFreedBytes = results.sumOf { it.freedBytes }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Cleaning complete")
        },
        text = {
            Column {
                Text(
                    text = "• $successCount apps cleaned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (failedCount > 0) {
                    Text(
                        text = "• $failedCount failed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Space freed: ${FileSizeFormatter.format(totalFreedBytes)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
