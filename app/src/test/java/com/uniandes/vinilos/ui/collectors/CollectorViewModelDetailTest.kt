package com.uniandes.vinilos.ui.collectors

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.CollectorRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectorViewModelDetailTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumSample = Album(
        id = 10, name = "Kind of Blue", cover = "", releaseDate = "1959",
        description = "Jazz masterpiece", genre = "Jazz", recordLabel = "Columbia"
    )

    private val performerSample = Performer(
        id = 100, name = "Rubén Blades Bellido de Luna",
        image = "", description = "Cantante panameño",
        birthDate = "1948-07-16"
    )

    private val collectorSample = Collector(
        id = 1, name = "Alejandro Vance",
        telephone = "3001234567", email = "alejandro@vinilos.com",
        description = "Jazz collector",
        favoritePerformers = listOf(performerSample),
        collectorAlbums = listOf(
            CollectorAlbum(id = 1, price = 100, status = "NM", album = albumSample)
        )
    )

    private val collectorSample2 = Collector(
        id = 2, name = "Marcus Thorne",
        telephone = "3007654321", email = "marcus@vinilos.com"
    )

    @Test
    fun `findById retorna el coleccionista correcto cuando esta en la lista cargada`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample, collectorSample2)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)
        assertEquals(collectorSample, result)
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
    fun `findById retorna los artistas favoritos del coleccionista`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)
        assertEquals(1, result?.favoritePerformers?.size)
        assertEquals("Rubén Blades Bellido de Luna", result?.favoritePerformers?.first()?.name)
    }

    @Test
    fun `findById retorna null cuando el id no existe en la lista`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample, collectorSample2)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.findById(999))
    }

    @Test
    fun `findById retorna null mientras la lista aun no ha cargado`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample)

        val vm = CollectorViewModel(repo)
        // NO llamamos advanceUntilIdle → la corutina de carga no ha terminado

        assertNull(vm.findById(1))
    }

    @Test
    fun `findById diferencia correctamente entre dos coleccionistas con ids distintos`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample, collectorSample2)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val resultado1 = vm.findById(1)
        val resultado2 = vm.findById(2)

        assertEquals("Alejandro Vance", resultado1?.name)
        assertEquals("Marcus Thorne", resultado2?.name)
    }

    @Test
    fun `findById retorna coleccionista con descripcion correcta`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns listOf(collectorSample)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)
        assertNotNull(result)
        assertEquals("Jazz collector", result?.description)
    }
}
