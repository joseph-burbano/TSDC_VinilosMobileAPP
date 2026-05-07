package com.uniandes.vinilos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.ui.albums.AlbumViewModel
import com.uniandes.vinilos.ui.albums.AlbumsUiState
import com.uniandes.vinilos.ui.artists.ArtistViewModel
import com.uniandes.vinilos.ui.collectors.CollectorViewModel
import com.uniandes.vinilos.ui.theme.VinilosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import com.uniandes.vinilos.ui.components.VinilosTopBar
import com.uniandes.vinilos.model.UserRole

@Composable
fun HomeScreen(
    albumViewModel: AlbumViewModel = viewModel(
        factory = AlbumViewModel.factory(LocalContext.current)
    ),
    artistViewModel: ArtistViewModel = viewModel(
        factory = ArtistViewModel.factory(LocalContext.current)
    ),
    collectorViewModel: CollectorViewModel = viewModel(
        factory = CollectorViewModel.factory(LocalContext.current)
    ),
    onMenuClick: () -> Unit = {},
    userRole: UserRole? = null, 
    onAlbumClick: (Int) -> Unit = {},
    onArtistClick: (Int) -> Unit = {},
    onCollectorClick: (Int) -> Unit = {}
) {
    // collectAsStateWithLifecycle pausa la recolección al pasar a STOPPED.
    val uiState by albumViewModel.uiState.collectAsStateWithLifecycle()
    val performers by artistViewModel.performers.collectAsStateWithLifecycle()
    val collectors by collectorViewModel.collectors.collectAsStateWithLifecycle()

    // remember/derivedStateOf evita recalcular las listas (takeLast, flatMap, distinctBy)
    // en cada recomposition: solo cuando cambia el upstream observado.
    val albums by remember(uiState) {
        derivedStateOf { (uiState as? AlbumsUiState.Success)?.albums.orEmpty() }
    }
    val lastAlbums by remember(albums) {
        derivedStateOf { albums.takeLast(2).reversed() }
    }
    val consultedArtists by remember(performers) {
        derivedStateOf { performers.takeLast(2) }
    }
    val recommendedArtists by remember(collectors) {
        derivedStateOf {
            collectors.flatMap { it.favoritePerformers }
                .distinctBy { it.id }
                .take(2)
        }
    }
    val featuredCollectors by remember(collectors) {
        derivedStateOf { collectors.take(2) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {  // sin verticalScroll

            VinilosTopBar(
                userRole = userRole,
                onMenuClick = onMenuClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // ── Header ──────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(
                        start = 24.dp, end = 24.dp,
                        top = 24.dp, bottom = 16.dp
                    )
                ) {
                    Text(
                        text = "RESUMEN",
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Vinilos",
                        fontSize = 48.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    )
                }

                // ── Últimos álbumes ───────────────────────────────────────
                SectionHeader(
                    label = "ÚLTIMOS ÁLBUMES",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (lastAlbums.isEmpty()) {
                    LoadingRow()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .testTag("home_albums_row"),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        lastAlbums.forEach { album ->
                            AlbumCard(album = album, onClick = { onAlbumClick(album.id) })
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Artistas consultados ──────────────────────────────────
                SectionHeader(
                    label = "ARTISTAS CONSULTADOS",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (consultedArtists.isEmpty()) {
                    LoadingRow()
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.testTag("home_consulted_artists_row")
                    ) {
                        items(consultedArtists, key = { it.id }) { artist ->
                            ArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Artistas recomendados ─────────────────────────────────
                SectionHeader(
                    label = "ARTISTAS RECOMENDADOS",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (recommendedArtists.isEmpty()) {
                    LoadingRow()
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.testTag("home_recommended_artists_row")
                    ) {
                        items(recommendedArtists, key = { it.id }) { artist ->
                            ArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Coleccionistas ────────────────────────────────────────
                SectionHeader(
                    label = "COLECCIONISTAS",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (collectors.isEmpty()) {
                    LoadingRow()
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.testTag("home_collectors_row")
                    ) {
                        // key = { it.id } permite a Compose reusar composables y preservar
                        // su estado interno cuando la lista cambia, en vez de recrearlos.
                        items(featuredCollectors, key = { it.id }) { collector ->
                            CollectorCard(
                                collector = collector,
                                onClick = { onCollectorClick(collector.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = modifier
    )
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AlbumCard(album: Album, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_album_${album.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            var imageError by remember { mutableStateOf(false) }
            if (album.cover.isBlank() || imageError) {
                Icon(
                    imageVector = Icons.Filled.Album,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                // ImageRequest con size + crossfade le indica a Coil que decodifique
                // el bitmap al tamaño máximo en el que se va a mostrar (≈ 200dp x ancho).
                // Sin esto, Coil decodifica la imagen al tamaño nativo (a veces 2-4 MB
                // por bitmap) — un costo de memoria innecesario que escala mal con
                // listas de imágenes.
                AsyncImage(
                    model = remember(album.cover) {
                        ImageRequest.Builder(context)
                            .data(album.cover)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .build()
                    },
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    onError = { imageError = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.releaseDate.take(4),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = album.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
        )
        Text(
            text = album.performers.firstOrNull()?.name ?: "—",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArtistCard(artist: Performer, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
            .testTag("home_artist_${artist.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            var imageError by remember { mutableStateOf(false) }
            if (artist.image.isBlank() || imageError) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                // ImageRequest con scale=FILL + crossfade. El composable es un círculo
                // de 80dp; Coil decodificará el bitmap a ese tamaño en lugar del
                // original del servidor.
                AsyncImage(
                    model = remember(artist.image) {
                        ImageRequest.Builder(context)
                            .data(artist.image)
                            .crossfade(true)
                            .scale(Scale.FILL)
                            .build()
                    },
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    onError = { imageError = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = artist.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun CollectorCard(collector: Collector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .testTag("home_collector_${collector.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = collector.name.first().uppercase(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = collector.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        collector.favoritePerformers.firstOrNull()?.let { performer ->
            Text(
                text = "Fan de: ${performer.name}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    VinilosTheme {
        HomeScreen()
    }
}
