package com.example.cachecleaner.ui.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cachecleaner.data.apps.AppRepository
import com.example.cachecleaner.data.cleaner.CleanerRepository
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AppFilter
import com.example.cachecleaner.domain.model.AppInfo
import com.example.cachecleaner.domain.model.CleaningResult
import com.example.cachecleaner.domain.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AppsUiState(
    val isLoading: Boolean = true,
    val isCleaning: Boolean = false,
    val cleaningProgress: Pair<Int, Int>? = null,
    val currentCleaningPackage: String? = null,
    val apps: List<AppInfo> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val sortOption: SortOption = SortOption.CACHE_DESC,
    val accessMode: AccessMode = AccessMode.SHIZUKU,
    val cleanResults: List<CleaningResult>? = null,
    val errorMessage: String? = null
) {
    val selectedApps: List<AppInfo>
        get() = apps.filter { it.packageName in selectedPackages }

    val selectedCacheBytes: Long
        get() = selectedApps.sumOf { it.storageInfo?.cacheBytes ?: 0L }
}

class AppsViewModel(
    private val appRepository: AppRepository,
    private val cleanerRepository: CleanerRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        loadPreferencesAndApps()
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

    fun refreshApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val showSystemApps = userPreferencesRepository.showSystemAppsFlow.first()
            val apps = appRepository.getInstalledApps(
                includeSystemApps = showSystemApps,
                filter = _uiState.value.filter,
                sortOption = _uiState.value.sortOption,
                searchQuery = _uiState.value.searchQuery
            )

            // Remove uninstalled apps from selection
            val currentSelected = _uiState.value.selectedPackages.intersect(apps.map { it.packageName }.toSet())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                apps = apps,
                selectedPackages = currentSelected
            )
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

    fun toggleSelect(packageName: String) {
        val current = _uiState.value.selectedPackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _uiState.value = _uiState.value.copy(selectedPackages = current)
    }

    fun selectAll() {
        val allPackages = _uiState.value.apps.map { it.packageName }.toSet()
        _uiState.value = _uiState.value.copy(selectedPackages = allPackages)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedPackages = emptySet())
    }

    fun cleanSelected() {
        viewModelScope.launch {
            val packagesToClean = _uiState.value.selectedPackages.toList()
            if (packagesToClean.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(
                isCleaning = true,
                cleaningProgress = Pair(0, packagesToClean.size)
            )

            val engine = cleanerRepository.getEngine(_uiState.value.accessMode)
            val results = cleanerRepository.cleanBatchAndVerify(
                engine = engine,
                packageNames = packagesToClean,
                onProgress = { current, total, pkg ->
                    _uiState.value = _uiState.value.copy(
                        cleaningProgress = Pair(current, total),
                        currentCleaningPackage = pkg
                    )
                }
            )

            refreshApps()

            _uiState.value = _uiState.value.copy(
                isCleaning = false,
                cleaningProgress = null,
                currentCleaningPackage = null,
                selectedPackages = emptySet(),
                cleanResults = results
            )
        }
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(cleanResults = null)
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
