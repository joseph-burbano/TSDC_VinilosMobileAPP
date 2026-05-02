package com.uniandes.vinilos.ui.collectors

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.repository.CollectorRepository
import com.uniandes.vinilos.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CollectorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumSample = Album(
        id = 1, name = "Kind of Blue", cover = "", releaseDate = "1959",
        description = "Jazz", genre = "Jazz", recordLabel = "Columbia"
    )

    private val sample = listOf(
        Collector(
            id = 1, name = "Alejandro Vance",
            telephone = "3001234567", email = "alejandro@vinilos.com",
            description = "Jazz collector",
            collectorAlbums = listOf(
                CollectorAlbum(id = 1, price = 100, status = "NM", album = albumSample)
            )
        ),
        Collector(id = 2, name = "Marcus Thorne", telephone = "3007654321", email = "marcus@vinilos.com"),
        Collector(id = 3, name = "Sofia Reyes", telephone = "3009999999", email = "sofia@vinilos.com"),
        Collector(id = 4, name = "Juan Pérez", telephone = "3001111111", email = "juan@vinilos.com"),
        Collector(id = 5, name = "Ana Gómez", telephone = "3002222222", email = "ana@vinilos.com")
    )

    // ── Carga inicial ──────────────────────────────────────────────────────────

    @Test
    fun `init carga collectors y actualiza el flow`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertEquals(5, vm.collectors.value.size)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
        coVerify(exactly = 1) { repo.getCollectors() }
    }

    @Test
    fun `isLoading es false al terminar la carga`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `IOException expone mensaje sin conexion`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } throws IOException("offline")

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.collectors.value.isEmpty())
        assertFalse(vm.isLoading.value)
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
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } throws httpError

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertEquals(
            "El servidor respondió con un error (503).",
            vm.error.value
        )
    }

    // ── Paginación ─────────────────────────────────────────────────────────────

    @Test
    fun `visibleCollectors muestra solo la primera pagina inicialmente`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        // pageSize = 4, sample tiene 5
        val visible = vm.visibleCollectors.first()
        assertEquals(4, visible.size)
    }

    @Test
    fun `hasMore es true cuando hay mas collectors que el pageSize`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample // 5 > pageSize(4)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.hasMore.first())
    }

    @Test
    fun `hasMore es false cuando los collectors caben en una pagina`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample.take(3) // 3 < pageSize(4)

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.hasMore.first())
    }

    @Test
    fun `loadMore incrementa los collectors visibles`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertEquals(4, vm.visibleCollectors.first().size)

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(5, vm.visibleCollectors.first().size)
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Test
    fun `refreshCollectors actualiza la lista con datos nuevos`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        val extra = Collector(id = 6, name = "Nuevo Collector", telephone = "", email = "")
        coEvery { repo.getCollectors() } returns sample
        coEvery { repo.refreshCollectors() } returns sample + extra

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()
        assertEquals(5, vm.collectors.value.size)

        vm.refreshCollectors()
        advanceUntilIdle()

        assertEquals(6, vm.collectors.value.size)
        coVerify(exactly = 1) { repo.refreshCollectors() }
    }

    @Test
    fun `refreshCollectors limpia el error previo si tiene exito`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } throws IOException("offline")
        coEvery { repo.refreshCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()
        assertNotNull(vm.error.value)

        vm.refreshCollectors()
        advanceUntilIdle()

        assertNull(vm.error.value)
        assertEquals(5, vm.collectors.value.size)
    }

    @Test
    fun `isRefreshing es true durante refresh y false al terminar`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample
        coEvery { repo.refreshCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isRefreshing.value)

        vm.refreshCollectors()
        advanceUntilIdle()

        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun `refresh mantiene lista visible mientras actualiza`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        val extra = Collector(id = 6, name = "Nuevo Collector", telephone = "", email = "")
        coEvery { repo.getCollectors() } returns sample
        coEvery { repo.refreshCollectors() } returns sample + extra

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()
        assertEquals(5, vm.collectors.value.size)

        vm.refreshCollectors()

        assertEquals(
            "La lista anterior debe permanecer visible durante el refresh",
            5, vm.collectors.value.size
        )

        advanceUntilIdle()
        assertEquals(6, vm.collectors.value.size)
    }

    // ── loadCollector ──────────────────────────────────────────────────────────

    @Test
    fun `loadCollector agrega el coleccionista a collectors si no estaba`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns emptyList()
        coEvery { repo.getCollector(99) } returns Collector(
            id = 99, name = "Nuevo Collector",
            telephone = "3009999999", email = "nuevo@vinilos.com"
        )

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        vm.loadCollector(99)
        advanceUntilIdle()

        assertEquals(1, vm.collectors.value.size)
        assertEquals("Nuevo Collector", vm.collectors.value.first().name)
    }

    @Test
    fun `loadCollector no llama al repository si el id ya existe en collectors`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns sample

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        vm.loadCollector(sample.first().id)
        advanceUntilIdle()

        coVerify(exactly = 0) { repo.getCollector(any()) }
        assertEquals(5, vm.collectors.value.size)
    }

    @Test
    fun `loadCollector no agrega nada si el repository devuelve null`() = runTest {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns emptyList()
        coEvery { repo.getCollector(99) } returns null

        val vm = CollectorViewModel(repo)
        advanceUntilIdle()

        vm.loadCollector(99)
        advanceUntilIdle()

        assertTrue(vm.collectors.value.isEmpty())
    }
}
