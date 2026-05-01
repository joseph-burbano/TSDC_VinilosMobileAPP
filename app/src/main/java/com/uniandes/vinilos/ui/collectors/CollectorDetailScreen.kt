package com.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import androidx.compose.runtime.LaunchedEffect

object CollectorDetailTestTags {
    const val SCREEN = "collector_detail_screen"
    const val LOADING = "collector_detail_loading"
    const val NAME = "collector_detail_name"
    const val IMAGE = "collector_detail_image"
    const val BACK = "collector_detail_back"
    const val STATS = "collector_detail_stats"
    const val VAULT = "collector_detail_vault"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDetailScreen(
    collectorId: Int,
    viewModel: CollectorViewModel,
    onBack: () -> Unit = {}
) {
    LaunchedEffect(collectorId) {
        viewModel.loadCollector(collectorId)
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val collector = viewModel.findById(collectorId)

    if (isLoading || collector == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { testTag = CollectorDetailTestTags.LOADING },
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        return
    }

    Scaffold(
        modifier = Modifier.semantics { testTag = CollectorDetailTestTags.SCREEN },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Vinilos",
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Volver"
                            testTag = CollectorDetailTestTags.BACK
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            HeroSection(collector)
            Spacer(Modifier.height(24.dp))
            StatsSection(collector)
            Spacer(Modifier.height(32.dp))
            VaultSection(collector.collectorAlbums)
            Spacer(Modifier.height(24.dp))
            SeeMoreButton()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroSection(collector: Collector) {
    Column {
        AsyncImage(
            model = collector.image?.ifBlank { null },
            contentDescription = collector.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .semantics { testTag = CollectorDetailTestTags.IMAGE }
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                "ELITE CURATOR",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "REGISTRY ID: #${collector.id.toString().padStart(6, '0')}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            collector.name,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { testTag = CollectorDetailTestTags.NAME }
        )
        if (!collector.description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                collector.description!!,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StatsSection(collector: Collector) {
    val albums = collector.collectorAlbums
    val avgGrade = albums.mapNotNull { it.status.takeIf { s -> s.isNotBlank() } }
        .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "—"

    Column(modifier = Modifier.semantics { testTag = CollectorDetailTestTags.STATS }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("COLLECTION", albums.size.toString(), "LPs", Modifier.weight(1f))
            StatCard(
                "WANTLIST",
                collector.favoritePerformers.size.toString(),
                "Rarities",
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("AVG. GRADE", avgGrade, "Condition", Modifier.weight(1f))
            StatCard(
                "FOLLOWERS",
                collector.comments.size.toString(),
                "Audience",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(suffix, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun VaultSection(albums: List<CollectorAlbum>) {
    Column(modifier = Modifier.semantics { testTag = CollectorDetailTestTags.VAULT }) {
        Text(
            "The Vault",
            fontSize = 28.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Rare pressings and personal milestones.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                "BROWSE ALL",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        if (albums.isEmpty()) {
            Text(
                "Sin álbumes en la colección todavía.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            albums.forEach { item ->
                VaultAlbumCard(item)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun VaultAlbumCard(item: CollectorAlbum) {
    val album = item.album
    val ref =
        "${album?.id?.toString()?.padStart(2, '0') ?: "—"}-${album?.releaseDate?.take(4) ?: "----"}"
    Column {
        Box {
            AsyncImage(
                model = album?.cover?.ifBlank { null },
                contentDescription = album?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Icon(
                imageVector = if (item.status == "NM") Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            ref,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            album?.name ?: "—",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            album?.performers?.firstOrNull()?.name ?: "Artista desconocido",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun SeeMoreButton() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.clickable { /* Navegación pendiente */ }
        ) {
            Text(
                "VER MÁS",
                color = MaterialTheme.colorScheme.background,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 14.dp)
            )
        }
    }
}
