package com.uniandes.vinilos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.graphics.graphicsLayer
import com.uniandes.vinilos.ui.albums.AlbumDetailScreen
import com.uniandes.vinilos.ui.albums.AlbumListScreen
import com.uniandes.vinilos.ui.albums.AlbumViewModel
import com.uniandes.vinilos.ui.artists.ArtistDetailScreen
import com.uniandes.vinilos.ui.artists.ArtistListScreen
import com.uniandes.vinilos.ui.artists.ArtistViewModel
import com.uniandes.vinilos.ui.collectors.CollectorDetailScreen
import com.uniandes.vinilos.ui.collectors.CollectorListScreen
import com.uniandes.vinilos.ui.collectors.CollectorViewModel
import com.uniandes.vinilos.ui.home.HomeScreen
import com.uniandes.vinilos.ui.navigation.BottomNavItem
import com.uniandes.vinilos.ui.navigation.Screen
import com.uniandes.vinilos.ui.theme.VinilosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    val context = LocalContext.current
    val albumViewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.factory(context))
    val artistViewModel: ArtistViewModel = viewModel(factory = ArtistViewModel.factory(context))
    val collectorViewModel: CollectorViewModel = viewModel(factory = CollectorViewModel.factory(context))

    val isDetailScreen = currentRoute?.startsWith("album_detail") == true ||
            currentRoute?.startsWith("artist_detail") == true ||
            currentRoute?.startsWith("collector_detail") == true

    var isBarVisible by remember { mutableStateOf(true) }

    val systemNavInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val barHeightPx = with(LocalDensity.current) { (80.dp + systemNavInset).toPx() }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(currentRoute) {
        if (!isDetailScreen) {
            isBarVisible = true
        }
    }

    LaunchedEffect(isBarVisible) {
        offsetY.animateTo(
            targetValue = if (isBarVisible) 0f else barHeightPx,
            animationSpec = tween(durationMillis = 250)
        )
    }

    val nestedScrollConnection = remember(isDetailScreen) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isDetailScreen) {
                    if (available.y < -5) isBarVisible = false
                    else if (available.y > 5) isBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        bottomBar = {
            val barHeightDp = with(LocalDensity.current) { offsetY.value.toDp() }
            NavigationBar(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = offsetY.value
                        alpha = if (offsetY.value > barHeightPx * 0.5f) 0f else 1f
                    }
            ) {
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
        },
        modifier = Modifier.nestedScroll(nestedScrollConnection),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = if (offsetY.value > 0f) {
                Modifier.padding(top = innerPadding.calculateTopPadding())
            } else {
                Modifier.padding(innerPadding)
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    albumViewModel = albumViewModel,
                    artistViewModel = artistViewModel,
                    collectorViewModel = collectorViewModel,
                    onAlbumClick = { albumId ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                    },
                    onArtistClick = { artistId ->
                        navController.navigate(Screen.ArtistDetail.createRoute(artistId))
                    },
                    onCollectorClick = { collectorId ->
                        navController.navigate(Screen.CollectorDetail.createRoute(collectorId))
                    }
                )
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
                ArtistListScreen(
                    viewModel = artistViewModel,
                    onArtistClick = { artistId ->
                        navController.navigate(Screen.ArtistDetail.createRoute(artistId))
                    }
                )
            }
            composable(
                route = Screen.ArtistDetail.route,
                arguments = listOf(navArgument("artistId") { type = NavType.IntType })
            ) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getInt("artistId") ?: return@composable
                ArtistDetailScreen(
                    artistId = artistId,
                    viewModel = artistViewModel,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(Screen.CollectorList.route) {
                CollectorListScreen(
                    viewModel = collectorViewModel,
                    onCollectorClick = { collectorId ->
                        navController.navigate(Screen.CollectorDetail.createRoute(collectorId))
                    }
                )
            }
            composable(
                route = Screen.CollectorDetail.route,
                arguments = listOf(navArgument("collectorId") { type = NavType.IntType })
            ) { backStackEntry ->
                val collectorId = backStackEntry.arguments?.getInt("collectorId") ?: return@composable
                CollectorDetailScreen(
                    collectorId = collectorId,
                    viewModel = collectorViewModel,
                    onBack = { navController.navigateUp() }
                )
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
