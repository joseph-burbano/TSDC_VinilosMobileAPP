package com.uniandes.vinilos.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandes.vinilos.ui.theme.VinilosTheme
import com.uniandes.vinilos.util.FakeData

@Composable
fun HomeScreen() {
    val lastAlbum = FakeData.albums.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "ÚLTIMO ÁLBUM AGREGADO",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lastAlbum.name,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = lastAlbum.artists.firstOrNull()?.name ?: "Miles Davis",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "ARTISTAS CONSULTADOS",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FakeData.artists.take(2).forEach { artist ->
            Text(
                text = artist.name,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "ARTISTAS RECOMENDADOS",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FakeData.artists.takeLast(2).forEach { artist ->
            Text(
                text = artist.name,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
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