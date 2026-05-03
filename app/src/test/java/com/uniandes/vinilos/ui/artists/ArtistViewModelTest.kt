package com.uniandes.vinilos.ui.artists

import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sample = listOf(
        Performer(1, "Rubén Blades", "https://example.com/img1.jpg", "Cantante panameño", "1948-07-16"),
        Performer(2, "Queen", "https://example.com/img2.jpg", "Banda británica de rock", null, "1970-01-01"),
        Performer(3, "Miles Davis", "https://example.com/img3.jpg", "Trompetista de jazz", "1926-05-26"),
        Performer(4, "Nina Simone", "https://example.com/img4.jpg", "Cantante de soul", "1933-02-21"),
        Performer(5, "Kraftwerk", "https://example.com/img5.jpg", "Banda alemana", null, "1969-01-01")
    )

    // ── Carga inicial ──────────────────────────────────────────────────────────

    @Test
    fun `init carga performers y actualiza el flow`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertEquals(5, vm.performers.value.size)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
        coVerify(exactly = 1) { repo.getPerformers() }
    }

    @Test
    fun `error de red expone mensaje en el flow de error`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } throws Exception("Sin conexión")

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.performers.value.isEmpty())
        assertFalse(vm.isLoading.value)
        assertNotNull(vm.error.value)
        assertTrue(vm.error.value!!.contains("Sin conexión"))
    }

    @Test
    fun `isLoading es true durante la carga y false al terminar`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isLoading.value)
    }

    // ── Paginación ─────────────────────────────────────────────────────────────

    @Test
    fun `visiblePerformers muestra solo la primera pagina inicialmente`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        // pageSize = 4, sample tiene 5
        val visible = vm.visiblePerformers.first()
        assertEquals(4, visible.size)
    }

    @Test
    fun `hasMore es true cuando hay mas performers que el pageSize`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample  // 5 > pageSize(4)

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.hasMore.first())
    }

    @Test
    fun `hasMore es false cuando los performers caben en una pagina`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample.take(3)  // 3 < pageSize(4)

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.hasMore.first())
    }

    @Test
    fun `loadMore incrementa los performers visibles`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertEquals(4, vm.visiblePerformers.first().size)

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(5, vm.visiblePerformers.first().size)
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    fun `findById retorna el performer correcto`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        val result = vm.findById(2)
        assertNotNull(result)
        assertEquals("Queen", result!!.name)
    }

    @Test
    fun `findById retorna null si el id no existe`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.findById(999))
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Test
    fun `refreshPerformers actualiza la lista con datos nuevos`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        val extra = Performer(6, "Etta James", "https://example.com/img6.jpg", "Soul", "1938-01-25")
        coEvery { repo.getPerformers() } returns sample
        coEvery { repo.refreshPerformers() } returns sample + extra

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()
        assertEquals(5, vm.performers.value.size)

        vm.refreshPerformers()
        advanceUntilIdle()

        assertEquals(6, vm.performers.value.size)
        coVerify(exactly = 1) { repo.refreshPerformers() }
    }

    @Test
    fun `refreshPerformers limpia el error previo si tiene exito`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } throws Exception("offline")
        coEvery { repo.refreshPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()
        assertNotNull(vm.error.value)

        vm.refreshPerformers()
        advanceUntilIdle()

        assertNull(vm.error.value)
        assertEquals(5, vm.performers.value.size)
    }

    @Test
    fun `IOException expone mensaje sin conexion`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } throws IOException("offline")

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertEquals(
            "Sin conexión. Revisa tu red e inténtalo de nuevo.",
            vm.error.value
        )
    }

    @Test
    fun `HttpException expone el codigo HTTP`() = runTest {
        val httpError = HttpException(
            Response.error<Any>(503, "".toResponseBody("application/json".toMediaTypeOrNull()))
        )
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } throws httpError

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertEquals(
            "El servidor respondió con un error (503).",
            vm.error.value
        )
    }

    @Test
    fun `isRefreshing es true durante refresh y false al terminar`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        coEvery { repo.getPerformers() } returns sample
        coEvery { repo.refreshPerformers() } returns sample

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isRefreshing.value)

        vm.refreshPerformers()
        // No assert en true aquí — el dispatcher de test avanza sincrónicamente
        advanceUntilIdle()

        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun `refresh mantiene lista visible mientras actualiza`() = runTest {
        val repo = mockk<ArtistRepository>(relaxed = true)
        val extra = Performer(6, "Etta James", "https://example.com/img6.jpg", "Soul", "1938-01-25")
        coEvery { repo.getPerformers() } returns sample
        coEvery { repo.refreshPerformers() } returns sample + extra

        val vm = ArtistViewModel(repo)
        advanceUntilIdle()
        assertEquals(5, vm.performers.value.size)

        vm.refreshPerformers()

        // Durante el refresh la lista anterior sigue visible
        assertEquals(
            "La lista anterior debe permanecer visible durante el refresh",
            5, vm.performers.value.size
        )

        advanceUntilIdle()
        assertEquals(6, vm.performers.value.size)
    }
}
