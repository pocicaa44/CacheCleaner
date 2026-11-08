package com.example.cachecleaner.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cachecleaner.domain.model.AppFilter
import com.example.cachecleaner.domain.model.SortOption

@Composable
fun FilterSortSection(
    currentFilter: AppFilter,
    onFilterChange: (AppFilter) -> Unit,
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == AppFilter.ALL,
                onClick = { onFilterChange(AppFilter.ALL) },
                label = { Text("All") }
            )
            FilterChip(
                selected = currentFilter == AppFilter.USER,
                onClick = { onFilterChange(AppFilter.USER) },
                label = { Text("User Apps") }
            )
            FilterChip(
                selected = currentFilter == AppFilter.SYSTEM,
                onClick = { onFilterChange(AppFilter.SYSTEM) },
                label = { Text("System Apps") }
            )
        }

        Box {
            IconButton(onClick = { sortMenuExpanded = true }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Options")
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Cache: Largest First") },
                    onClick = {
                        onSortChange(SortOption.CACHE_DESC)
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cache: Smallest First") },
                    onClick = {
                        onSortChange(SortOption.CACHE_ASC)
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Name: A–Z") },
                    onClick = {
                        onSortChange(SortOption.NAME_ASC)
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Name: Z–A") },
                    onClick = {
                        onSortChange(SortOption.NAME_DESC)
                        sortMenuExpanded = false
                    }
                )
            }
        }
    }
}
