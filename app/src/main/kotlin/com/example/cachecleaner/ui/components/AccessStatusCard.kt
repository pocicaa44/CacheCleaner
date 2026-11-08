package com.example.cachecleaner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus

@Composable
fun AccessStatusCard(
    currentMode: AccessMode,
    status: AccessStatus,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (currentMode) {
                            AccessMode.SHIZUKU -> Icons.Default.Security
                            AccessMode.ROOT -> Icons.Default.AdminPanelSettings
                            AccessMode.STANDARD -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Access Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = currentMode.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val (statusText, statusDescription) = when (status) {
                AccessStatus.AVAILABLE -> Pair(
                    "Connected & Ready",
                    "Automated cache clearing enabled."
                )
                AccessStatus.PERMISSION_REQUIRED -> Pair(
                    "Permission Required",
                    "Grant permission to allow automated cache cleaning."
                )
                AccessStatus.SERVICE_NOT_RUNNING -> Pair(
                    "Service Inactive",
                    "Shizuku service is not running on this device."
                )
                AccessStatus.NOT_INSTALLED -> Pair(
                    "Not Installed",
                    "Shizuku manager application is not installed."
                )
                AccessStatus.DENIED -> Pair(
                    "Permission Denied",
                    "Root or elevated privilege request was rejected."
                )
                AccessStatus.UNSUPPORTED -> Pair(
                    "Unsupported",
                    "Automated operation not supported on this platform."
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = when (status) {
                    AccessStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
                    AccessStatus.PERMISSION_REQUIRED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (status == AccessStatus.PERMISSION_REQUIRED || status == AccessStatus.SERVICE_NOT_RUNNING) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(if (status == AccessStatus.PERMISSION_REQUIRED) "Grant Permission" else "Check Service")
                }
            }
        }
    }
}
