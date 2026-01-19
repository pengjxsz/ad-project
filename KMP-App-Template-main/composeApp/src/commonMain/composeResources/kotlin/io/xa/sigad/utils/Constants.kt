package io.xa.sigad.utils


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import io.xa.sigad.data.BottomNavItem

object Constants {
    val BottomNavItems = listOf(
        // Home screen
        BottomNavItem(
            label = "Home",
            icon = Icons.Filled.Home,
            route = "home"
        ),
        // Search screen
        BottomNavItem(
            label = "Project",
            icon = Icons.Filled.Search,
            route = "project"
        ),
        // Profile screen
        BottomNavItem(
            label = "Net",
            icon = Icons.Filled.Person,
            route = "net"
        )
    )
}