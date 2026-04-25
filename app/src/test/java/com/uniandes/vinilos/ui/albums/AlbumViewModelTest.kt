package com.uniandes.vinilos.ui.albums

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sample = listOf(
        Album(1, "Abbey Road", "", "1969", "desc", "Rock", "Apple", emptyList(), emptyList()),
        Album(2, "Thriller", "", "1982", "desc", "Pop", "Epic", emptyList(), emptyList())
    )

    @Test
    fun `init dispara load y termina en Success con la lista del repositorio`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        assertEquals(AlbumsUiState.Loading, vm.uiState.value)

        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AlbumsUiState.Success)
        assertEquals(2, (state as AlbumsUiState.Success).albums.size)
        coVerify(exactly = 1) { repo.getAlbums() }
    }

    @Test
    fun `IOException del repo se traduce a mensaje sin conexion`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } throws IOException("offline")

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AlbumsUiState.Error)
        assertEquals(
            "Sin conexión. Revisa tu red e inténtalo de nuevo.",
            (state as AlbumsUiState.Error).message
        )
    }

    @Test
    fun `HttpException del repo expone el codigo HTTP`() = runTest {
        val httpError = HttpException(
            Response.error<Any>(503, "".toResponseBody("application/json".toMediaTypeOrNull()))
        )
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } throws httpError

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(
            "El servidor respondió con un error (503).",
            (state as AlbumsUiState.Error).message
        )
    }

    @Test
    fun `refresh sobre Success no parpadea al estado Loading`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample
        coEvery { repo.refreshAlbums() } returns sample + sample.first().copy(id = 99, name = "Nuevo")

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AlbumsUiState.Success)

        vm.refresh()

        assertTrue(
            "La lista anterior debe permanecer visible durante el pull-to-refresh",
            vm.uiState.value is AlbumsUiState.Success
        )
        assertEquals(2, (vm.uiState.value as AlbumsUiState.Success).albums.size)

        advanceUntilIdle()
        assertEquals(3, (vm.uiState.value as AlbumsUiState.Success).albums.size)
    }

    @Test
    fun `isRefreshing es true durante el fetch y false al terminar`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        assertTrue(vm.isRefreshing.value)

        advanceUntilIdle()

        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun `refresh tras error recupera el catalogo y limpia el error`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } throws IOException("offline")
        coEvery { repo.refreshAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is AlbumsUiState.Error)

        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is AlbumsUiState.Success)
        assertEquals(2, (state as AlbumsUiState.Success).albums.size)
        coVerify(exactly = 1) { repo.refreshAlbums() }
    }

}
