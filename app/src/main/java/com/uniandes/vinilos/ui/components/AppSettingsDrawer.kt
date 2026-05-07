package com.uniandes.vinilos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniandes.vinilos.model.UserRole

@Composable
fun AppSettingsDrawer(
    userRole: UserRole?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBecomeCollector: () -> Unit,
    onLeaveCollector: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.75f)
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Toggle tema
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = null
                    )
                },
                label = {
                    Text(if (isDarkTheme) "Cambiar a tema claro" else "Cambiar a tema oscuro")
                },
                selected = false,
                onClick = {
                    onToggleTheme()
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Rol
            when (userRole) {
                UserRole.VISITOR -> {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null
                            )
                        },
                        label = { Text("Hacerse coleccionista") },
                        selected = false,
                        onClick = {
                            onBecomeCollector()
                            onCloseDrawer()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                UserRole.COLLECTOR -> {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = null
                            )
                        },
                        label = { Text("Salir del modo coleccionista") },
                        selected = false,
                        onClick = {
                            onLeaveCollector()
                            onCloseDrawer()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                null -> Unit
            }
        }
    }
}
