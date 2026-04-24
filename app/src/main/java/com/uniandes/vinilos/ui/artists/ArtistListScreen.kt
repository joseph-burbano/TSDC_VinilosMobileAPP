package com.uniandes.vinilos.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.ui.theme.VinilosTheme

@Composable
fun ArtistListScreen(
    onArtistClick: (Int) -> Unit = {},
    viewModel: ArtistViewModel = viewModel(
        factory = ArtistViewModel.factory(LocalContext.current)
    )
) {
    val performers by viewModel.visiblePerformers.collectAsState(initial = emptyList())
    val hasMore by viewModel.hasMore.collectAsState(initial = false)
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Archivo de",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        Text(
            text = "Artistas",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading_indicator"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("error_message"),
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
                    modifier = Modifier
                        .weight(1f)
                        .testTag("artist_list"),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(performers) { performer ->
                        PerformerGridItem(
                            performer = performer,
                            onClick = { onArtistClick(performer.id) }
                        )
                    }
                }
                if (hasMore) {
                    OutlinedButton(
                        onClick = { viewModel.loadMore() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
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

@Composable
fun PerformerGridItem(performer: Performer, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .testTag("artist_item_${performer.id}")
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(performer.image)
                .crossfade(true)
                .build(),
            contentDescription = performer.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.DarkGray)
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
