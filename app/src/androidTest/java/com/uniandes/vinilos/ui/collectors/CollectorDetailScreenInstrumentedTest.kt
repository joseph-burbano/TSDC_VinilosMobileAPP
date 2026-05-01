package com.uniandes.vinilos.ui.collectors

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.mockk.coVerify

@RunWith(AndroidJUnit4::class)
class CollectorDetailScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbum = Album(
        id = 10, name = "Kind of Blue", cover = "", releaseDate = "1959",
        description = "Jazz masterpiece", genre = "Jazz", recordLabel = "Columbia"
    )
    private val sampleCollector = Collector(
        id = 1,
        name = "Alejandro Vance",
        telephone = "3001234567",
        email = "alejandro@vinilos.com",
        description = "Preserving the warmth of analog signals since 1998.",
        image = "",
        collectorAlbums = listOf(
            CollectorAlbum(id = 1, price = 100, status = "NM", album = sampleAlbum)
        )
    )

    private fun mockViewModel(
        isLoading: Boolean = false,
        collector: Collector? = sampleCollector
    ): CollectorViewModel {
        val vm = mockk<CollectorViewModel>(relaxed = true)
        every { vm.isLoading } returns MutableStateFlow(isLoading)
        every { vm.findById(any()) } returns collector
        return vm
    }

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoIsLoadingEsTrue() {
        val vm = mockViewModel(isLoading = true)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(collectorId = 1, viewModel = vm)
            }
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoCollectorEsNull() {
        val vm = mockViewModel(isLoading = false, collector = null)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(collectorId = 999, viewModel = vm)
            }
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraNombreDelCollector_cuandoCargaExitosa() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(collectorId = sampleCollector.id, viewModel = vm)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.NAME)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraSeccionStats_cuandoCollectorTieneAlbumes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(collectorId = sampleCollector.id, viewModel = vm)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.STATS)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraSeccionVault_cuandoCollectorTieneAlbumes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(collectorId = sampleCollector.id, viewModel = vm)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(CollectorDetailTestTags.VAULT)
            .assertExists()
    }

    @Test
    fun detailScreen_botonAtras_invocaCallbackOnBack() {
        var backInvoked = false
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorDetailScreen(
                    collectorId = sampleCollector.id,
                    viewModel = vm,
                    onBack = { backInvoked = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(CollectorDetailTestTags.BACK))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CollectorDetailTestTags.BACK).performClick()
        assertTrue(backInvoked)
    }

    @Test
fun `loadCollector agrega el coleccionista a _collectors si no estaba`() = runTest {
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
fun `loadCollector no llama al repository si el id ya existe en _collectors`() = runTest {
    val repo = mockk<CollectorRepository>(relaxed = true)
    coEvery { repo.getCollectors() } returns listOf(collectorSample)

    val vm = CollectorViewModel(repo)
    advanceUntilIdle()

    vm.loadCollector(collectorSample.id)
    advanceUntilIdle()

    coVerify(exactly = 0) { repo.getCollector(any()) }
    assertEquals(1, vm.collectors.value.size)
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
