package com.example.cachecleaner.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cachecleaner.ui.apps.AppsScreen
import com.example.cachecleaner.ui.apps.AppsViewModel
import com.example.cachecleaner.ui.home.HomeScreen
import com.example.cachecleaner.ui.home.HomeViewModel
import com.example.cachecleaner.ui.settings.SettingsScreen
import com.example.cachecleaner.ui.settings.SettingsViewModel

enum class MainTab {
    HOME,
    APPS,
    SETTINGS
}

@Composable
fun MainTabScreen(
    homeViewModel: HomeViewModel,
    appsViewModel: AppsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabOrdinal by rememberSaveable { mutableIntStateOf(MainTab.HOME.ordinal) }
    val currentTab = MainTab.entries[selectedTabOrdinal]

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { selectedTabOrdinal = MainTab.HOME.ordinal },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.APPS,
                    onClick = {
                        if (selectedTabOrdinal != MainTab.APPS.ordinal) {
                            selectedTabOrdinal = MainTab.APPS.ordinal
                            appsViewModel.refreshApps()
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps") },
                    label = { Text("Apps") },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { selectedTabOrdinal = MainTab.SETTINGS.ordinal },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    alwaysShowLabel = true
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToApps = {
                        selectedTabOrdinal = MainTab.APPS.ordinal
                        appsViewModel.refreshApps()
                    }
                )
                MainTab.APPS -> AppsScreen(
                    viewModel = appsViewModel,
                    onAppClick = onNavigateToDetail
                )
                MainTab.SETTINGS -> SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
