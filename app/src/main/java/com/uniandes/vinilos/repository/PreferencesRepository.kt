package com.uniandes.vinilos.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.uniandes.vinilos.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_ROLE       = stringPreferencesKey("user_role")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val userRole: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[KEY_ROLE]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
    }

    suspend fun setUserRole(role: UserRole) {
        dataStore.edit { it[KEY_ROLE] = role.name }
    }

    suspend fun clearUserRole() {
        dataStore.edit { it.remove(KEY_ROLE) }
    }

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME] ?: false
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }
}
