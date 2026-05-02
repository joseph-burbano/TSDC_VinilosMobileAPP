package com.uniandes.vinilos.ui.albums

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelDetailTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trackSample = Track(
        id = 1, name = "Come Together", duration = "4:19"
    )

    private val performerSample = Performer(
        id = 1, name = "The Beatles",
        image = "", description = "Banda británica",
        creationDate = "1960-01-01"
    )

    private val sample = listOf(
        Album(
            id = 1, name = "Abbey Road", cover = "", releaseDate = "1969",
            description = "Classic rock album", genre = "Rock", recordLabel = "Apple",
            tracks = listOf(trackSample),
            performers = listOf(performerSample)
        ),
        Album(
            id = 2, name = "Thriller", cover = "", releaseDate = "1982",
            description = "Best selling album", genre = "Pop", recordLabel = "Epic",
            tracks = emptyList(), performers = emptyList()
        )
    )

    @Test
    fun `findById devuelve el album cuando esta en Success`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val album = vm.findById(2)
        assertNotNull(album)
        assertEquals("Thriller", album!!.name)
    }

    @Test
    fun `findById devuelve null cuando esta en Error o Loading`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } throws IOException("x")

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.findById(1))
    }

    @Test
    fun `findById devuelve null para id inexistente`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.findById(9999))
    }

    @Test
    fun `findById retorna null mientras la lista aun no ha cargado`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        // NO llamamos advanceUntilIdle

        assertNull(vm.findById(1))
    }

    @Test
    fun `findById diferencia correctamente entre dos albums con ids distintos`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val resultado1 = vm.findById(1)
        val resultado2 = vm.findById(2)

        assertEquals("Abbey Road", resultado1?.name)
        assertEquals("Thriller", resultado2?.name)
    }

    @Test
    fun `findById retorna los tracks del album`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)
        assertEquals(1, result?.tracks?.size)
        assertEquals("Come Together", result?.tracks?.first()?.name)
    }

    @Test
    fun `findById retorna los performers del album`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)
        assertEquals(1, result?.performers?.size)
        assertEquals("The Beatles", result?.performers?.first()?.name)
    }

    @Test
    fun `findById retorna la descripcion correcta del album`() = runTest {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } returns sample

        val vm = AlbumViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)
        assertNotNull(result)
        assertEquals("Classic rock album", result?.description)
    }
}
