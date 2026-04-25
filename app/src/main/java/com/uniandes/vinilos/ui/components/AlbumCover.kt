package com.uniandes.vinilos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun AlbumCover(
    coverUrl: String,
    fallbackColor: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp
) {
    if (coverUrl.isBlank()) {
        CoverPlaceholder(color = fallbackColor, iconSize = iconSize, modifier = modifier)
        return
    }

    SubcomposeAsyncImage(
        model = coverUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(fallbackColor),
        loading = { CoverPlaceholder(color = fallbackColor, iconSize = iconSize, showIcon = false) },
        error = { CoverPlaceholder(color = fallbackColor, iconSize = iconSize) }
    )
}

@Composable
private fun CoverPlaceholder(
    color: Color,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Filled.Album,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
