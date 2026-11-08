package com.example.cachecleaner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cachecleaner.domain.model.AccessMode
import com.example.cachecleaner.domain.model.AppFilter
import com.example.cachecleaner.domain.model.AppTheme
import com.example.cachecleaner.domain.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("app_theme")
        val ACCESS_MODE = stringPreferencesKey("preferred_access_mode")
        val SHOW_SYSTEM_APPS = booleanPreferencesKey("show_system_apps")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val FILTER_OPTION = stringPreferencesKey("filter_option")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.THEME]) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    val accessModeFlow: Flow<AccessMode> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.ACCESS_MODE]) {
            AccessMode.ROOT.name -> AccessMode.ROOT
            AccessMode.STANDARD.name -> AccessMode.STANDARD
            else -> AccessMode.SHIZUKU
        }
    }

    val showSystemAppsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_SYSTEM_APPS] ?: false
    }

    val sortOptionFlow: Flow<SortOption> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.SORT_OPTION]) {
            SortOption.CACHE_ASC.name -> SortOption.CACHE_ASC
            SortOption.NAME_ASC.name -> SortOption.NAME_ASC
            SortOption.NAME_DESC.name -> SortOption.NAME_DESC
            else -> SortOption.CACHE_DESC
        }
    }

    val filterOptionFlow: Flow<AppFilter> = context.dataStore.data.map { preferences ->
        when (preferences[PreferencesKeys.FILTER_OPTION]) {
            AppFilter.USER.name -> AppFilter.USER
            AppFilter.SYSTEM.name -> AppFilter.SYSTEM
            else -> AppFilter.ALL
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun setAccessMode(mode: AccessMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_MODE] = mode.name
        }
    }

    suspend fun setShowSystemApps(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SYSTEM_APPS] = show
        }
    }

    suspend fun setSortOption(sortOption: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_OPTION] = sortOption.name
        }
    }

    suspend fun setFilterOption(filter: AppFilter) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_OPTION] = filter.name
        }
    }
}
