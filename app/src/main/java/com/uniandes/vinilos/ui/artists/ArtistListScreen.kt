package com.uniandes.vinilos.ui.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.ui.theme.VinilosTheme
import com.uniandes.vinilos.util.FakeData

@Composable
fun ArtistListScreen(onArtistClick: (Int) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "ARCHIVE 003",
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
        LazyColumn {
            items(FakeData.performers) { performer ->
                PerformerItem(
                    performer = performer,
                    onClick = { onArtistClick(performer.id) }
                )
            }
        }
    }
}

@Composable
fun PerformerItem(performer: Performer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = performer.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = performer.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArtistListScreenPreview() {
    VinilosTheme {
        ArtistListScreen()
    }
}
