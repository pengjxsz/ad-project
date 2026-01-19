package io.xa.sigad.crop.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Chat : Screen

    @Serializable
    data class Project(val itemId: String) : Screen // Pass item ID for detail page
}