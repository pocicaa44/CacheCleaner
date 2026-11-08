package com.example.cachecleaner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cachecleaner.core.access.RootManager
import com.example.cachecleaner.core.access.ShizukuManager
import com.example.cachecleaner.data.preferences.UserPreferencesRepository
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AccessStatus
import com.example.cachecleaner.domain.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val accessMode: AccessMode = AccessMode.STANDARD,
    val showSystemApps: Boolean = false,
    val shizukuStatus: AccessStatus = AccessStatus.SERVICE_NOT_RUNNING,
    val rootStatus: AccessStatus = AccessStatus.NOT_INSTALLED
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        checkEnvironmentStatus()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userPreferencesRepository.themeFlow.collect { theme ->
                _uiState.value = _uiState.value.copy(theme = theme)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.dynamicColorFlow.collect { dynamicColor ->
                _uiState.value = _uiState.value.copy(dynamicColor = dynamicColor)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.accessModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(accessMode = mode)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.showSystemAppsFlow.collect { show ->
                _uiState.value = _uiState.value.copy(showSystemApps = show)
            }
        }
        viewModelScope.launch {
            ShizukuManager.accessStatus.collect { status ->
                _uiState.value = _uiState.value.copy(shizukuStatus = status)
            }
        }
    }

    fun checkEnvironmentStatus() {
        viewModelScope.launch {
            ShizukuManager.updateStatus()
            val root = RootManager.checkRootAvailability()
            _uiState.value = _uiState.value.copy(
                shizukuStatus = ShizukuManager.accessStatus.value,
                rootStatus = root
            )
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(theme)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDynamicColor(enabled)
        }
    }

    fun setAccessMode(mode: AccessMode) {
        viewModelScope.launch {
            userPreferencesRepository.setAccessMode(mode)
        }
    }

    fun setShowSystemApps(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowSystemApps(show)
        }
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userPreferencesRepository) as T
        }
    }
}
