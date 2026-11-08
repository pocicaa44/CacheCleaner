package com.example.cachecleaner

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cachecleaner.data.apps.AppRepository
import com.example.cachecleaner.data.cleaner.CleanerRepository
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.data.storage.StorageStatsRepository
import com.example.cachecleaner.ui.apps.AppsViewModel
import com.example.cachecleaner.ui.detail.AppDetailScreen
import com.example.cachecleaner.ui.home.HomeViewModel
import com.example.cachecleaner.ui.main.MainTabScreen
import com.example.cachecleaner.ui.settings.SettingsViewModel

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(MainRoute)

    val storageStatsRepository = StorageStatsRepository(context)
    val appRepository = AppRepository(context, storageStatsRepository)
    val cleanerRepository = CleanerRepository(context, storageStatsRepository)
    val userPreferencesRepository = UserPreferencesRepository(context)

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            context = context,
            appRepository = appRepository,
            storageStatsRepository = storageStatsRepository,
            cleanerRepository = cleanerRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    )

    val appsViewModel: AppsViewModel = viewModel(
        factory = AppsViewModel.Factory(
            appRepository = appRepository,
            cleanerRepository = cleanerRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            userPreferencesRepository = userPreferencesRepository
        )
    )

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<MainRoute> {
                MainTabScreen(
                    homeViewModel = homeViewModel,
                    appsViewModel = appsViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToDetail = { packageName ->
                        backStack.add(AppDetailRoute(packageName))
                    }
                )
            }
            entry<AppDetailRoute> { route ->
                AppDetailScreen(
                    packageName = route.packageName,
                    appRepository = appRepository,
                    cleanerRepository = cleanerRepository,
                    userPreferencesRepository = userPreferencesRepository,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
