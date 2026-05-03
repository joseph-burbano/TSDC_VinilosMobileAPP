package com.uniandes.vinilos.ui

import com.uniandes.vinilos.AppViewModel
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.testing.FakePreferencesRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakePreferencesRepository
    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        fakeRepo = FakePreferencesRepository()
        viewModel = AppViewModel(fakeRepo)
    }

    @Test
    fun `userRole inicia en null`() = runTest {
        assertNull(viewModel.userRole.value)
    }

    @Test
    fun `setUserRole actualiza el rol a VISITOR`() = runTest {
        viewModel.setUserRole(UserRole.VISITOR)
        advanceUntilIdle()                       
        assertEquals(UserRole.VISITOR, viewModel.userRole.value)
    }

    @Test
    fun `setUserRole actualiza el rol a COLLECTOR`() = runTest {
        viewModel.setUserRole(UserRole.COLLECTOR)
        advanceUntilIdle()                       
        assertEquals(UserRole.COLLECTOR, viewModel.userRole.value)
    }

    @Test
    fun `clearUserRole regresa el rol a null`() = runTest {
        viewModel.setUserRole(UserRole.COLLECTOR)
        viewModel.clearUserRole()
        assertNull(viewModel.userRole.value)
    }

    @Test
    fun `isDarkTheme inicia en false`() = runTest {
        assertEquals(false, viewModel.isDarkTheme.value)
    }

    @Test
    fun `toggleDarkTheme cambia de false a true`() = runTest {
        viewModel.toggleDarkTheme()
        advanceUntilIdle()   
        assertTrue(viewModel.isDarkTheme.value)
    }

    @Test
    fun `toggleDarkTheme cambia de true a false`() = runTest {
        viewModel.setDarkTheme(true)
        viewModel.toggleDarkTheme()
        assertEquals(false, viewModel.isDarkTheme.value)
    }

    @Test
    fun `isReady es true cuando el repositorio emite`() = runTest {
        assertTrue(viewModel.isReady.value)
    }
}
