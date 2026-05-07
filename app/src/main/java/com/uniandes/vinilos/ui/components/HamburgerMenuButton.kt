package com.uniandes.vinilos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun HamburgerMenuButton(
    onMenuClick: () -> Unit
) {
    IconButton(onClick = onMenuClick) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Abrir menú"
        )
    }
}
