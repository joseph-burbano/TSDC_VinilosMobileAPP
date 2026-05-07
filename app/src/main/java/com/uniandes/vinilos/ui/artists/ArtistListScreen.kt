package com.uniandes.vinilos.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.ui.theme.VinilosTheme
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.components.VinilosTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistListScreen(
    onArtistClick: (Int) -> Unit = {},
    onMenuClick: () -> Unit = {},
    userRole: UserRole? = null,
    viewModel: ArtistViewModel = viewModel(
        factory = ArtistViewModel.factory(LocalContext.current)
    )
) {
    // collectAsStateWithLifecycle libera la suscripción al pasar a STOPPED, ahorrando
    // batería y evitando reposiciones innecesarias del ViewModel a la UI cuando la
    // pantalla no está visible.
    val visiblePerformers by viewModel.visiblePerformers.collectAsStateWithLifecycle()
    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val allPerformers by viewModel.performers.collectAsStateWithLifecycle()
    
    // remember evita re-filtrar en cada recomposition: solo cuando cambia el query
    // o la lista upstream.
    val displayPerformers = remember(visiblePerformers, allPerformers, searchQuery) {
        if (searchQuery.isBlank()) {
            visiblePerformers
        } else {
            allPerformers.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredCount = remember(searchQuery, allPerformers, displayPerformers) {
        if (searchQuery.isBlank()) allPerformers.size else displayPerformers.size
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background   
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            VinilosTopBar(
                userRole = userRole,
                onMenuClick = onMenuClick
            )
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().testTag("loading_indicator"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().testTag("error_message"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().testTag("artist_list"),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = "ARCHIVO DE",
                                    fontSize = 12.sp,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "Artistas",
                                        fontSize = 48.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 56.sp
                                    )
                                    Text(
                                        text = "$filteredCount encontrados",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }
                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar artistas...") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .testTag("artist_search"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // key estable: reduce trabajo de Compose al reordenar o filtrar.
                        itemsIndexed(
                            items = displayPerformers,
                            key = { _, performer -> performer.id }
                        ) { _, performer ->
                            PerformerGridItem(
                                performer = performer,
                                onClick = { onArtistClick(performer.id) }
                            )
                        }

                        if (hasMore && searchQuery.isBlank()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                OutlinedButton(
                                    onClick = { viewModel.loadMore() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .testTag("load_more_button"),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "CARGAR MÁS",
                                        letterSpacing = 2.sp,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformerGridItem(performer: Performer, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("artist_item_${performer.id}")
    ) {
        // scale = FILL le indica a Coil que decodifique al tamaño que ocupa el composable
        // (no al tamaño nativo de la imagen del servidor): bitmap mucho más liviano,
        // listas de muchos artistas no inflan el heap.
        AsyncImage(
            model = remember(performer.image) {
                ImageRequest.Builder(context)
                    .data(performer.image)
                    .crossfade(true)
                    .scale(Scale.FILL)
                    .build()
            },
            contentDescription = performer.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = performer.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("artist_name_${performer.id}")
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArtistListScreenPreview() {
    VinilosTheme {
        ArtistListScreen()
    }
}
