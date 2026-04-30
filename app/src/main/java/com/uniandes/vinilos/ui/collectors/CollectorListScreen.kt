package com.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniandes.vinilos.model.Collector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorListScreen(
    viewModel: CollectorViewModel,
    onCollectorClick: (Int) -> Unit = {}
) {
    val collectors by viewModel.collectors.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = "collector_loading" }
            )
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
        }
        else -> PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshCollectors() }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)) {
                        Text(
                            text = "ARCHIVE DIRECTORY",
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "The People of\nthe Needle",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Curating the global network of archivists,\ndiggers, and auditory historians.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                items(collectors) { collector ->
                    CollectorItem(
                        collector = collector,
                        onClick = { onCollectorClick(collector.id) }
                    )
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .semantics { contentDescription = "collector_item_${collector.id}" },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = collector.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = collector.email,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (collector.collectorAlbums.isNotEmpty()) {
                Text(
                    text = "${collector.collectorAlbums.size} álbumes en colección",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
