package com.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.ui.theme.VinilosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorListScreen(
    viewModel: CollectorViewModel = viewModel(
        factory = CollectorViewModel.factory(LocalContext.current)
    ),
    onCollectorClick: (Int) -> Unit = {}
) {
    val visibleCollectors by viewModel.visibleCollectors.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle(
        initialValue = false
    )
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val allCollectors by viewModel.collectors.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val displayCollectors = if (searchQuery.isBlank()) {
        visibleCollectors
    } else {
        allCollectors.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.orEmpty().contains(searchQuery, ignoreCase = true) ||
            it.favoritePerformers.any { p ->
                p.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredCount = if (searchQuery.isBlank()) allCollectors.size
                        else displayCollectors.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .testTag("collector_list_loading"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.semantics {
                        contentDescription = "collector_loading"
                    }
                )
            }
            error != null -> Box(
                Modifier
                    .fillMaxSize()
                    .testTag("collector_list_error"),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }
            else -> PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshCollectors() }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collector_list"),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 24.dp,
                                bottom = 4.dp
                            )
                        ) {
                            Text(
                                text = "DIRECTORIO DE",
                                fontSize = 12.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            var fontSize by remember { mutableStateOf(48.sp) }
                            Text(
                                text = "Coleccionistas",
                                fontSize = fontSize,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                onTextLayout = { result ->
                                    if (result.hasVisualOverflow) {
                                        fontSize *= 0.9f
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "$filteredCount encontrados",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.End)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar coleccionistas...") },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .testTag("collector_search"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    items(displayCollectors) { collector ->
                        CollectorItem(
                            collector = collector,
                            onClick = { onCollectorClick(collector.id) }
                        )
                    }
                    if (hasMore && searchQuery.isBlank()) {
                        item {
                            OutlinedButton(
                                onClick = { viewModel.loadMore() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                                    .testTag("collector_load_more"),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "MOSTRAR MÁS",
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

@Composable
fun CollectorItem(collector: Collector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() }
            .semantics { contentDescription = "collector_item_${collector.id}" },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collector.name.first().uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collector.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("collector_name_${collector.id}")
                )
                collector.favoritePerformers.firstOrNull()?.let { performer ->
                    Text(
                        text = "Fan de: ${performer.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (collector.collectorAlbums.isNotEmpty()) {
                    Text(
                        text = "${collector.collectorAlbums.size} álbumes en colección",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CollectorListScreenPreview() {
    VinilosTheme {
        CollectorListScreen()
    }
}
