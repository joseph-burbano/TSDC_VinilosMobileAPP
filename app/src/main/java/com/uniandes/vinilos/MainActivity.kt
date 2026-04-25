package com.uniandes.vinilos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uniandes.vinilos.ui.albums.AlbumDetailScreen
import com.uniandes.vinilos.ui.albums.AlbumListScreen
import com.uniandes.vinilos.ui.albums.AlbumViewModel
import com.uniandes.vinilos.ui.artists.ArtistListScreen
import com.uniandes.vinilos.ui.collectors.CollectorListScreen
import com.uniandes.vinilos.ui.home.HomeScreen
import com.uniandes.vinilos.ui.navigation.BottomNavItem
import com.uniandes.vinilos.ui.navigation.Screen
import com.uniandes.vinilos.ui.theme.VinilosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VinilosTheme {
                VinilosApp()
            }
        }
    }
}

@Composable
fun VinilosApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = BottomNavItem.entries.map { it.route }
    val showBottomBar = currentRoute in bottomNavRoutes

    val context = LocalContext.current
    val albumViewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.factory(context))

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            modifier = Modifier.semantics {
                                contentDescription = "nav_${item.label.lowercase()}"
                            },
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.AlbumList.route) {
                AlbumListScreen(
                    viewModel = albumViewModel,
                    onAlbumClick = { albumId ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                    }
                )
            }
            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(navArgument("albumId") { type = NavType.IntType })
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getInt("albumId") ?: return@composable
                AlbumDetailScreen(
                    albumId = albumId,
                    viewModel = albumViewModel,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(Screen.ArtistList.route) {
                ArtistListScreen()
            }
            composable(Screen.CollectorList.route) {
                CollectorListScreen()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VinilosAppPreview() {
    VinilosTheme {
        VinilosApp()
    }
}
