package com.example.cachecleaner.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cachecleaner.core.utils.FileSizeFormatter
import com.example.cachecleaner.data.apps.AppRepository
import com.example.cachecleaner.data.cleaner.CleanerRepository
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AppInfo
import com.example.cachecleaner.domain.model.CleaningStatus
import com.example.cachecleaner.ui.components.AppIcon
import com.example.cachecleaner.ui.components.AppTopBar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppDetailScreen(
    packageName: String,
    appRepository: AppRepository,
    cleanerRepository: CleanerRepository,
    userPreferencesRepository: UserPreferencesRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var appInfo by remember { mutableStateOf<AppInfo?>(null) }
    var accessMode by remember { mutableStateOf(AccessMode.SHIZUKU) }
    var isLoading by remember { mutableStateOf(true) }
    var isCleaning by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(packageName) {
        isLoading = true
        accessMode = userPreferencesRepository.accessModeFlow.first()
        appInfo = appRepository.getAppDetails(packageName)
        isLoading = false
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "App Details",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val app = appInfo
            if (app == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Application information unavailable.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    AppIcon(icon = app.icon, size = 64.dp)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Storage Usage Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            StorageRow(label = "Application Binary", bytes = app.storageInfo?.appBytes)
                            Spacer(modifier = Modifier.height(8.dp))
                            StorageRow(label = "User Private Data", bytes = app.storageInfo?.dataBytes)
                            Spacer(modifier = Modifier.height(8.dp))
                            StorageRow(label = "Cache (Cleanable)", bytes = app.storageInfo?.cacheBytes, isHighlighted = true)

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))

                            StorageRow(
                                label = "Total Space Used",
                                bytes = app.storageInfo?.totalBytes,
                                isTotal = true
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isCleaning = true
                                val engine = cleanerRepository.getEngine(accessMode)
                                val result = cleanerRepository.cleanAndVerify(engine, packageName)

                                // Refresh details
                                appInfo = appRepository.getAppDetails(packageName)
                                isCleaning = false

                                if (result.status == CleaningStatus.SUCCESS) {
                                    val freedStr = FileSizeFormatter.format(result.freedBytes)
                                    snackbarHostState.showSnackbar(
                                        if (accessMode == AccessMode.STANDARD) {
                                            "Settings opened."
                                        } else {
                                            "Cache cleared successfully! Freed $freedStr."
                                        }
                                    )
                                } else {
                                    snackbarHostState.showSnackbar("Failed: ${result.errorMessage ?: "Unknown error"}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isCleaning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (accessMode == AccessMode.STANDARD) Icons.AutoMirrored.Filled.OpenInNew else Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                if (accessMode == AccessMode.STANDARD) "Open System App Settings" else "Clear App Cache"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageRow(
    label: String,
    bytes: Long?,
    isHighlighted: Boolean = false,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal || isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = FileSizeFormatter.format(bytes),
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal || isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
