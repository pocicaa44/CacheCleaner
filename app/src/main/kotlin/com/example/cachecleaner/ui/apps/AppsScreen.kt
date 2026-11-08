package com.example.cachecleaner.ui.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cachecleaner.ui.components.AppListItem
import com.example.cachecleaner.ui.components.AppTopBar
import com.example.cachecleaner.ui.components.EmptyStateView
import com.example.cachecleaner.ui.components.FilterSortSection
import com.example.cachecleaner.ui.components.SearchInputBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAppResumed()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Applications",
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshApps(isPullToRefresh = true) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
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

            HorizontalDivider()

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshApps(isPullToRefresh = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.apps.isEmpty()) {
                    EmptyStateView(
                        title = if (uiState.searchQuery.isBlank()) "No apps found" else "No results found",
                        message = if (uiState.searchQuery.isBlank()) {
                            "Scan again to find apps."
                        } else {
                            "No results for '${uiState.searchQuery}'."
                        },
                        actionLabel = if (uiState.searchQuery.isBlank()) "Refresh" else "Clear Search",
                        onActionClick = {
                            if (uiState.searchQuery.isBlank()) viewModel.refreshApps() else viewModel.setSearchQuery("")
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
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
                                onClick = { onAppClick(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}
