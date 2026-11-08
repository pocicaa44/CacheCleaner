package com.example.cachecleaner.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cachecleaner.data.apps.AppRepository
import com.example.cachecleaner.data.cleaner.CleanerRepository
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AppFilter
import com.example.cachecleaner.domain.model.AppInfo
import com.example.cachecleaner.domain.model.SortOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AppsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val sortOption: SortOption = SortOption.SIZE_DESC,
    val accessMode: AccessMode = AccessMode.STANDARD
)

class AppsViewModel(
    private val appRepository: AppRepository,
    private val cleanerRepository: CleanerRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        loadPreferencesAndApps()
        observeCleanerEvents()
    }

    private fun observeCleanerEvents() {
        viewModelScope.launch {
            cleanerRepository.cacheCleanedEvent.collect {
                // When cache cleaning succeeds anywhere in the app, refresh list to update sizes
                refreshAppsInternal()
            }
        }
    }

    private fun loadPreferencesAndApps() {
        viewModelScope.launch {
            val filter = userPreferencesRepository.filterOptionFlow.first()
            val sort = userPreferencesRepository.sortOptionFlow.first()
            val mode = userPreferencesRepository.accessModeFlow.first()

            _uiState.value = _uiState.value.copy(
                filter = filter,
                sortOption = sort,
                accessMode = mode
            )

            refreshApps()
        }
    }

    fun refreshApps(isPullToRefresh: Boolean = false) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshAppsInternal(isPullToRefresh)
        }
    }

    private suspend fun refreshAppsInternal(isPullToRefresh: Boolean = false) {
        if (isPullToRefresh) {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
        } else if (_uiState.value.apps.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true)
        } else {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
        }

        val showSystemApps = userPreferencesRepository.showSystemAppsFlow.first()
        val apps = appRepository.getInstalledApps(
            includeSystemApps = showSystemApps,
            filter = _uiState.value.filter,
            sortOption = _uiState.value.sortOption,
            searchQuery = _uiState.value.searchQuery
        )

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            apps = apps
        )
    }

    fun onAppResumed() {
        viewModelScope.launch {
            val mode = userPreferencesRepository.accessModeFlow.first()
            _uiState.value = _uiState.value.copy(accessMode = mode)
            refreshApps()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        refreshApps()
    }

    fun setFilter(filter: AppFilter) {
        viewModelScope.launch {
            userPreferencesRepository.setFilterOption(filter)
            _uiState.value = _uiState.value.copy(filter = filter)
            refreshApps()
        }
    }

    fun setSort(sortOption: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.setSortOption(sortOption)
            _uiState.value = _uiState.value.copy(sortOption = sortOption)
            refreshApps()
        }
    }

    class Factory(
        private val appRepository: AppRepository,
        private val cleanerRepository: CleanerRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppsViewModel(appRepository, cleanerRepository, userPreferencesRepository) as T
        }
    }
}
