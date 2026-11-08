package com.example.cachecleaner.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cachecleaner.R
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.ui.components.AccessStatusCard
import com.example.cachecleaner.ui.components.AppTopBar
import com.example.cachecleaner.ui.components.CleanResultDialog
import com.example.cachecleaner.ui.components.ConfirmCleanDialog
import com.example.cachecleaner.ui.components.StorageSummaryCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToApps: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAppResumed()
    }

    val isCalculating = uiState.isScanning || uiState.isLoading

    Scaffold(
        topBar = {
            AppTopBar(title = "Cache Cleaner")
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
            // Usage Access Warning Card if permission is missing
            if (!uiState.hasUsageAccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.usage_access_needed_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.usage_access_needed_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.action_grant_usage_access))
                        }
                    }
                }
            }

            // Storage Overview Hero Card with interactive on-the-spot loading
            StorageSummaryCard(
                totalCacheBytes = uiState.totalCacheBytes,
                appCount = uiState.totalAppsCount,
                lastScanTime = uiState.lastScanTime,
                isCalculating = isCalculating,
                onRefresh = { viewModel.scanApps() }
            )

            // Current Access Status Card
            AccessStatusCard(
                currentMode = uiState.accessMode,
                status = uiState.accessStatus,
                onRequestPermission = { viewModel.requestShizukuPermission() },
                onConfigureClick = onNavigateToApps
            )

            // Active Cleaning Progress
            if (uiState.isCleaning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val (current, total) = uiState.cleaningProgress ?: Pair(0, 1)
                        val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

                        Text(
                            text = "Cleaning $current of $total",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        if (!uiState.currentCleaningPackage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.currentCleaningPackage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Interactive Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.scanApps() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isCleaning && !isCalculating,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Scan")
                }

                if (uiState.accessMode == AccessMode.STANDARD) {
                    Button(
                        onClick = onNavigateToApps,
                        modifier = Modifier.weight(1.5f),
                        enabled = !uiState.isCleaning && !isCalculating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Manage Apps")
                    }
                } else {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.weight(1.5f),
                        enabled = !uiState.isCleaning && !isCalculating && uiState.totalCacheBytes > 0L && uiState.accessStatus == AccessStatus.AVAILABLE,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (uiState.isCleaning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cleaning...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Clear All Cache")
                        }
                    }
                }
            }
        }

        if (showConfirmDialog) {
            ConfirmCleanDialog(
                selectedCount = uiState.totalAppsCount,
                estimatedCacheBytes = uiState.totalCacheBytes,
                accessMode = uiState.accessMode,
                onConfirm = {
                    showConfirmDialog = false
                    viewModel.clearAllCache()
                },
                onDismiss = { showConfirmDialog = false }
            )
        }

        uiState.cleanResults?.let { results ->
            CleanResultDialog(
                results = results,
                onDismiss = { viewModel.dismissResultDialog() }
            )
        }
    }
}
