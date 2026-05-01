package com.uniandes.vinilos.ui.collectors

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.repository.CollectorRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumSample = Album(
        id = 1, name = "Kind of Blue", cover = "", releaseDate = "1959",
        description = "Jazz", genre = "Jazz", recordLabel = "Columbia"
    )
    private val collectorSample = Collector(
        id = 1, name = "Alejandro Vance",
        telephone = "3001234567", email = "alejandro@vinilos.com",
        description = "Jazz collector",
        collectorAlbums = listOf(CollectorAlbum(id = 1, price = 100, status = "NM", album = albumSample))
    )
    private val collectorSample2 = Collector(
        id = 2, name = "Marcus Thorne",
        telephone = "3007654321", email = "marcus@vinilos.com"
    )

    @Test
    fun `loadCollectors carga la lista desde el repository`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample, collectorSample2)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertEquals(2, vm.collectors.value.size)
        assertEquals("Alejandro Vance", vm.collectors.value[0].name)
    }

    @Test
    fun `findById retorna el coleccionista correcto cuando existe en la lista`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample, collectorSample2)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)

        assertNotNull(result)
        assertEquals("Alejandro Vance", result?.name)
    }

    @Test
    fun `findById retorna los albums del coleccionista`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)

        assertEquals(1, result?.collectorAlbums?.size)
        assertEquals("Kind of Blue", result?.collectorAlbums?.first()?.album?.name)
    }

    @Test
    fun `findById retorna null cuando el id no existe`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(999)

        assertNull(result)
    }

    @Test
    fun `findById retorna null mientras la lista aun no ha cargado`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample)

        val vm = CollectorViewModel(repo)

        val result = vm.findById(1)

        assertNull(result)
    }

    @Test
    fun `loadCollectors guarda mensaje de error cuando el repository falla`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } throws java.io.IOException("network down")

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.collectors.value.isEmpty())
        assertNotNull(vm.error.value)
    }
}
