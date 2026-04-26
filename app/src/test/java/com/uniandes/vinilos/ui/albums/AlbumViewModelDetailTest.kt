package com.uniandes.vinilos.ui.albums

import com.uniandes.vinilos.model.Album
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

    private val sample = listOf(
        Album(1, "Abbey Road", "", "1969", "desc", "Rock", "Apple", emptyList(), emptyList()),
        Album(2, "Thriller", "", "1982", "desc", "Pop", "Epic", emptyList(), emptyList())
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
}
