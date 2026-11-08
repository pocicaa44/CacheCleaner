package com.example.cachecleaner

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppRoute : NavKey

@Serializable
data object MainRoute : AppRoute

@Serializable
data class AppDetailRoute(val packageName: String) : AppRoute
