package com.uniandes.vinilos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AlbumList : Screen("album_list")
    object AlbumDetail : Screen("album_detail/{albumId}")
    object ArtistList : Screen("artist_list")
    object ArtistDetail : Screen("artist_detail/{artistId}")
    object CollectorList : Screen("collector_list")
    object CollectorDetail : Screen("collector_detail/{collectorId}")
}

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    VINYL("home", "Vinilos", Icons.Filled.Home),
    ALBUMES("album_list", "Álbumes", Icons.Filled.MusicNote),
    ARTISTS("artist_list", "Artistas", Icons.Filled.Person),
    PEOPLE("collector_list", "colecc.", Icons.Filled.AccountCircle)
}
