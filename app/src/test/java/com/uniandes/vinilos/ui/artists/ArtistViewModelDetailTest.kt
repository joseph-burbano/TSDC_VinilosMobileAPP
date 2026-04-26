package com.uniandes.vinilos.ui.artists

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Tests para el método findById que usa ArtistDetailScreen.
 * findById busca un Performer por ID en la lista ya cargada.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArtistViewModelDetailTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Datos de prueba: un músico con álbumes
    private val albumSample = Album(
        id = 10, name = "Kind of Blue", cover = "", releaseDate = "1959-08-17",
        description = "Jazz masterpiece", genre = "Jazz", recordLabel = "Columbia"
    )
    private val performerSample = Performer(
        id = 1, name = "Miles Davis", image = "", description = "Jazz musician",
        birthDate = "1926-05-26", albums = listOf(albumSample)
    )
    private val performerSample2 = Performer(
        id = 2, name = "Rubén Blades", image = "", description = "Cantante panameño",
        birthDate = "1948-07-16"
    )

    @Test
    fun `findById retorna el performer correcto cuando esta en la lista cargada`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns listOf(performerSample, performerSample2)

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        // Buscar por ID existente
        val result = vm.findById(1)

        assertEquals(performerSample, result)
        assertEquals("Miles Davis", result?.name)
    }

    @Test
    fun `findById retorna los albums del performer`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns listOf(performerSample)

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(1)

        assertEquals(1, result?.albums?.size)
        assertEquals("Kind of Blue", result?.albums?.first()?.name)
    }

    @Test
    fun `findById retorna null cuando el id no existe en la lista`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns listOf(performerSample, performerSample2)

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(999)

        assertNull(result)
    }

    @Test
    fun `findById retorna null mientras la lista aun no ha cargado`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns listOf(performerSample)

        val vm = ArtistViewModel(repo)
        // NO llamamos advanceUntilIdle → la corutina de carga no ha terminado

        val result = vm.findById(1)

        // La lista está vacía todavía, por lo que findById no puede encontrar nada
        assertNull(result)
    }

    @Test
    fun `findById diferencia correctamente entre dos performers con ids distintos`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns listOf(performerSample, performerSample2)

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        val resultado1 = vm.findById(1)
        val resultado2 = vm.findById(2)

        assertEquals("Miles Davis", resultado1?.name)
        assertEquals("Rubén Blades", resultado2?.name)
    }
}
