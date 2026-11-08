package com.example.cachecleaner.ui.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cachecleaner.ui.components.AppListItem
import com.example.cachecleaner.ui.components.AppTopBar
import com.example.cachecleaner.ui.components.CleanResultDialog
import com.example.cachecleaner.ui.components.ConfirmCleanDialog
import com.example.cachecleaner.ui.components.EmptyStateView
import com.example.cachecleaner.ui.components.FilterSortSection
import com.example.cachecleaner.ui.components.SearchInputBar
import com.example.cachecleaner.ui.components.SelectionBottomBar

@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val allSelected = uiState.apps.isNotEmpty() && uiState.selectedPackages.size == uiState.apps.size

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Applications",
                actions = {
                    if (uiState.apps.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (allSelected) viewModel.clearSelection() else viewModel.selectAll()
                            }
                        ) {
                            Icon(
                                imageVector = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = if (allSelected) "Deselect All" else "Select All"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.selectedPackages.isNotEmpty() && !uiState.isCleaning) {
                SelectionBottomBar(
                    selectedCount = uiState.selectedPackages.size,
                    selectedCacheBytes = uiState.selectedCacheBytes,
                    onClearClick = { showConfirmDialog = true }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchInputBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )

            FilterSortSection(
                currentFilter = uiState.filter,
                onFilterChange = { viewModel.setFilter(it) },
                currentSort = uiState.sortOption,
                onSortChange = { viewModel.setSort(it) }
            )

            if (uiState.isCleaning) {
                val (current, total) = uiState.cleaningProgress ?: Pair(0, 1)
                val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Cleaning... ($current of $total)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                    if (!uiState.currentCleaningPackage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.currentCleaningPackage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.apps.isEmpty()) {
                EmptyStateView(
                    title = if (uiState.searchQuery.isBlank()) "No Applications Found" else "No Matching Applications",
                    message = if (uiState.searchQuery.isBlank()) {
                        "Try scanning applications again."
                    } else {
                        "No applications match '${uiState.searchQuery}'. Try another search query."
                    },
                    actionLabel = if (uiState.searchQuery.isBlank()) "Refresh" else "Clear Search",
                    onActionClick = {
                        if (uiState.searchQuery.isBlank()) viewModel.refreshApps() else viewModel.setSearchQuery("")
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.apps,
                        key = { app -> app.packageName }
                    ) { app ->
                        AppListItem(
                            app = app,
                            isSelected = app.packageName in uiState.selectedPackages,
                            onToggleSelect = { viewModel.toggleSelect(app.packageName) },
                            onClick = { onAppClick(app.packageName) }
                        )
                    }
                }
            }
        }

        if (showConfirmDialog) {
            ConfirmCleanDialog(
                selectedCount = uiState.selectedPackages.size,
                estimatedCacheBytes = uiState.selectedCacheBytes,
                onConfirm = {
                    showConfirmDialog = false
                    viewModel.cleanSelected()
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
