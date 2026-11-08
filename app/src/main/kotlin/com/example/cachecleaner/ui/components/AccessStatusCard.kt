package com.example.cachecleaner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.ui.theme.CacheCleanerTheme

@Composable
fun AccessStatusCard(
    currentMode: AccessMode,
    status: AccessStatus,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    onConfigureClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Mode Name & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (currentMode) {
                            AccessMode.SHIZUKU -> Icons.Default.Security
                            AccessMode.ROOT -> Icons.Default.AdminPanelSettings
                            AccessMode.STANDARD -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Access Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = when (currentMode) {
                            AccessMode.STANDARD -> "Standard"
                            AccessMode.SHIZUKU -> "Shizuku"
                            AccessMode.ROOT -> "Root"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Status title & description based on mode and status
            val (statusText, statusDescription, isReady) = when (currentMode) {
                AccessMode.STANDARD -> Triple(
                    "Active",
                    "Opens system settings to clear cache manually.",
                    true
                )
                AccessMode.SHIZUKU -> when (status) {
                    AccessStatus.AVAILABLE -> Triple(
                        "Connected",
                        "Automated cache deletion enabled via ADB.",
                        true
                    )
                    AccessStatus.PERMISSION_REQUIRED -> Triple(
                        "Permission Required",
                        "Permission required for automated cleaning.",
                        false
                    )
                    AccessStatus.SERVICE_NOT_RUNNING -> Triple(
                        "Service Inactive",
                        "Shizuku service is not running.",
                        false
                    )
                    AccessStatus.NOT_INSTALLED -> Triple(
                        "Not Installed",
                        "Shizuku is not installed.",
                        false
                    )
                    else -> Triple(
                        "Permission Denied",
                        "Permission was denied.",
                        false
                    )
                }
                AccessMode.ROOT -> when (status) {
                    AccessStatus.AVAILABLE -> Triple(
                        "Active",
                        "Automated cache deletion enabled via root.",
                        true
                    )
                    else -> Triple(
                        "Unavailable",
                        "Root access not available.",
                        false
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReady) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Contextual Action Button if user interaction needed
            if (currentMode == AccessMode.SHIZUKU && (status == AccessStatus.PERMISSION_REQUIRED || status == AccessStatus.SERVICE_NOT_RUNNING)) {
                Spacer(modifier = Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.align(Alignment.End),
                    shape = ButtonDefaults.filledTonalShape
                ) {
                    Text(if (status == AccessStatus.PERMISSION_REQUIRED) "Grant Permission" else "Check Status")
                }
            }
        }
    }
}

@Preview(name = "Access Status - Light", showBackground = true)
@Preview(name = "Access Status - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AccessStatusCardPreview() {
    CacheCleanerTheme(dynamicColor = false) {
        AccessStatusCard(
            currentMode = AccessMode.ROOT,
            status = AccessStatus.AVAILABLE,
            onRequestPermission = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
