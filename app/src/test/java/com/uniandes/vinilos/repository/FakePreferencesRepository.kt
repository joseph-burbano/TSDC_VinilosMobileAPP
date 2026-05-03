package com.uniandes.vinilos.testing

import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.repository.IPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesRepository : IPreferencesRepository {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    private val _isDarkTheme = MutableStateFlow(false)

    override val userRole: Flow<UserRole?> = _userRole
    override val isDarkTheme: Flow<Boolean> = _isDarkTheme

    override suspend fun setUserRole(role: UserRole) {
        _userRole.value = role
    }

    override suspend fun clearUserRole() {
        _userRole.value = null
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }
}
