package com.example.cachecleaner.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.AppTheme
import com.example.cachecleaner.ui.components.AppTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                actions = {
                    IconButton(onClick = { viewModel.checkEnvironmentStatus() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh status")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Appearance Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dynamic Color",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Match system colors and wallpaper",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.dynamicColor,
                                onCheckedChange = { viewModel.setDynamicColor(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }

                    Text(
                        text = "Theme Preference",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeOptionRow(
                        title = "System",
                        selected = uiState.theme == AppTheme.SYSTEM,
                        onClick = { viewModel.setTheme(AppTheme.SYSTEM) }
                    )
                    ThemeOptionRow(
                        title = "Light",
                        selected = uiState.theme == AppTheme.LIGHT,
                        onClick = { viewModel.setTheme(AppTheme.LIGHT) }
                    )
                    ThemeOptionRow(
                        title = "Dark",
                        selected = uiState.theme == AppTheme.DARK,
                        onClick = { viewModel.setTheme(AppTheme.DARK) }
                    )
                }
            }

            // Access Management Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Access Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Cleaning Method",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeOptionRow(
                        title = "Standard",
                        selected = uiState.accessMode == AccessMode.STANDARD,
                        onClick = { viewModel.setAccessMode(AccessMode.STANDARD) }
                    )
                    ThemeOptionRow(
                        title = "Shizuku",
                        selected = uiState.accessMode == AccessMode.SHIZUKU,
                        onClick = { viewModel.setAccessMode(AccessMode.SHIZUKU) }
                    )
                    ThemeOptionRow(
                        title = "Root",
                        selected = uiState.accessMode == AccessMode.ROOT,
                        onClick = { viewModel.setAccessMode(AccessMode.ROOT) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    StatusItemRow(title = "Shizuku", status = uiState.shizukuStatus)

                    if (uiState.shizukuStatus == AccessStatus.PERMISSION_REQUIRED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = { viewModel.requestShizukuPermission() },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    StatusItemRow(title = "Root", status = uiState.rootStatus)
                }
            }

            // General Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Applications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "System apps", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Include preinstalled and system apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.showSystemApps,
                            onCheckedChange = { viewModel.setShowSystemApps(it) }
                        )
                    }
                }
            }

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Inspect and clear application cache safely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StatusItemRow(
    title: String,
    status: AccessStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = when (status) {
                AccessStatus.AVAILABLE -> "Available"
                AccessStatus.PERMISSION_REQUIRED -> "Permission Required"
                AccessStatus.SERVICE_NOT_RUNNING -> "Not Running"
                AccessStatus.NOT_INSTALLED -> "Not Installed"
                AccessStatus.DENIED -> "Denied"
                AccessStatus.UNSUPPORTED -> "Unsupported"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = when (status) {
                AccessStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
                AccessStatus.PERMISSION_REQUIRED -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
