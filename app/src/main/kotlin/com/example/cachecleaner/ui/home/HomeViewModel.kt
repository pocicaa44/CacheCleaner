package com.example.cachecleaner.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.data.apps.AppRepository
import com.example.cachecleaner.data.cleaner.CleanerRepository
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.data.storage.StorageStatsRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.CleaningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = true,
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val hasUsageAccess: Boolean = true,
    val cleaningProgress: Pair<Int, Int>? = null,
    val currentCleaningPackage: String? = null,
    val totalCacheBytes: Long = 0L,
    val totalAppsCount: Int = 0,
    val lastScanTime: String = "Never",
    val accessMode: AccessMode = AccessMode.STANDARD,
    val accessStatus: AccessStatus = AccessStatus.AVAILABLE,
    val cleanResults: List<CleaningResult>? = null,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val storageStatsRepository: StorageStatsRepository,
    private val cleanerRepository: CleanerRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.accessModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(accessMode = mode)
                checkAccessStatus(mode)
            }
        }

        viewModelScope.launch {
            ShizukuManager.accessStatus.collect { status ->
                if (_uiState.value.accessMode == AccessMode.SHIZUKU) {
                    _uiState.value = _uiState.value.copy(accessStatus = status)
                }
            }
        }
    }

    fun onAppResumed() {
        viewModelScope.launch {
            ShizukuManager.updateStatus()
            val hasUsage = storageStatsRepository.hasUsageStatsPermission()
            val status = cleanerRepository.checkModeAvailability(_uiState.value.accessMode)

            _uiState.value = _uiState.value.copy(
                hasUsageAccess = hasUsage,
                accessStatus = status
            )

            // If initial cold-start scan was pending permission, complete it now; otherwise no refresh
            if (hasUsage && _uiState.value.totalAppsCount == 0) {
                _uiState.value = _uiState.value.copy(isScanning = true)
                scanAppsInternal()
                _uiState.value = _uiState.value.copy(isScanning = false)
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isScanning = true)
            scanAppsInternal()
            _uiState.value = _uiState.value.copy(isLoading = false, isScanning = false)
        }
    }

    fun scanApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            scanAppsInternal()
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }

    private suspend fun scanAppsInternal() {
        val hasUsage = storageStatsRepository.hasUsageStatsPermission()
        val showSystemApps = userPreferencesRepository.showSystemAppsFlow.first()
        val apps = appRepository.getInstalledApps(includeSystemApps = showSystemApps)

        val totalCache = apps.sumOf { it.storageInfo?.cacheBytes ?: 0L }
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        _uiState.value = _uiState.value.copy(
            hasUsageAccess = hasUsage,
            totalCacheBytes = totalCache,
            totalAppsCount = apps.size,
            lastScanTime = currentTime
        )
    }

    fun checkAccessStatus(mode: AccessMode) {
        viewModelScope.launch {
            val status = cleanerRepository.checkModeAvailability(mode)
            _uiState.value = _uiState.value.copy(accessStatus = status)
        }
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
    }

    fun clearAllCache() {
        viewModelScope.launch {
            val showSystemApps = userPreferencesRepository.showSystemAppsFlow.first()
            val apps = appRepository.getInstalledApps(includeSystemApps = showSystemApps)
            val packagesToClean = apps.filter { (it.storageInfo?.cacheBytes ?: 0L) > 0L }.map { it.packageName }

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

            // Refresh storage numbers
            scanAppsInternal()

            _uiState.value = _uiState.value.copy(
                isCleaning = false,
                cleaningProgress = null,
                currentCleaningPackage = null,
                cleanResults = results
            )
        }
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(cleanResults = null)
    }

    class Factory(
        private val context: Context,
        private val appRepository: AppRepository,
        private val storageStatsRepository: StorageStatsRepository,
        private val cleanerRepository: CleanerRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            ShizukuManager.init(context)
            return HomeViewModel(
                appRepository = appRepository,
                storageStatsRepository = storageStatsRepository,
                cleanerRepository = cleanerRepository,
                userPreferencesRepository = userPreferencesRepository
            ) as T
        }
    }
}
