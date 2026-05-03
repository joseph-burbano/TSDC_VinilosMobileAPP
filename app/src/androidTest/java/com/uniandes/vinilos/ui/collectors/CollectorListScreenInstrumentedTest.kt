package com.uniandes.vinilos.ui.collectors

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.repository.CollectorRepository
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectorListScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeCollectors = listOf(
        Collector(
            id = 100,
            name = "Manolo Bellon",
            telephone = "3502457896",
            email = "manollo@caracol.com.co",
            favoritePerformers = listOf(
                Performer(100, "Rubén Blades Bellido de Luna", "", "Cantante panameño", "1948-07-16")
            )
        ),
        Collector(
            id = 101,
            name = "Jaime Monsalve",
            telephone = "3012357936",
            email = "jmonsalve@rtvc.com.co",
            favoritePerformers = listOf(
                Performer(101, "Queen", "", "Banda británica", null, "1970-01-01")
            )
        )
    )

    private fun createViewModel(
        collectors: List<Collector> = fakeCollectors
    ): CollectorViewModel {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } returns collectors
        return CollectorViewModel(repo)
    }

    // ─── HU05 - T1: Spinner mientras carga ───────────────────────────────────

    @Test
    fun listScreen_muestraIndicadorDeCarga_inicialmente() {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } coAnswers {
            delay(2_000)
            fakeCollectors
        }
        val viewModel = CollectorViewModel(repo)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag("collector_list_loading")
            .assertIsDisplayed()
    }

    // ─── HU05 - T2: Nombres de coleccionistas visibles tras carga ────────────

    @Test
    fun listScreen_muestraNombresDeColeccionistas_cuandoCargaExitosa() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 encontrados").assertIsDisplayed()
    }

    // ─── HU05 - T3: Artista favorito visible en cada item ────────────────────

    @Test
    fun listScreen_muestraArtistaFavorito_enCadaItem() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Fan de: Rubén Blades Bellido de Luna")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Fan de: Queen")
            .assertIsDisplayed()
    }

    // ─── HU05 - T4: Mensaje de error cuando falla el servicio ────────────────

    @Test
    fun listScreen_muestraMensajeDeError_cuandoFallaElServicio() {
        val repo = mockk<CollectorRepository>(relaxed = true)
        coEvery { repo.getCollectors() } throws java.io.IOException("sin red")
        val viewModel = CollectorViewModel(repo)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("collector_list_error"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("collector_list_error")
            .assertIsDisplayed()
    }

    // ─── HU05 - T5: Click en item invoca callback con ID correcto ────────────

    @Test
    fun listScreen_clickEnItem_invocaCallbackConIdCorrecto() {
        val viewModel = createViewModel()
        var clickedId: Int? = null

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    onCollectorClick = { clickedId = it },
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Manolo Bellon")
            .performClick()

        assert(clickedId == 100)
    }

    // ─── HU05 - T6: Búsqueda filtra por nombre ───────────────────────────────

    @Test
    fun listScreen_busqueda_muestraSoloCoincidencias_porNombre() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_search").performTextInput("Manolo")

        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertDoesNotExist()
    }

    // ─── HU05 - T7: Búsqueda es case-insensitive ─────────────────────────────

    @Test
    fun listScreen_busqueda_esCaseInsensitive() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_search").performTextInput("jaime")

        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manolo Bellon").assertDoesNotExist()
    }

    // ─── HU05 - T8: Búsqueda por artista favorito ────────────────────────────

    @Test
    fun listScreen_busqueda_porArtistaFavorito() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_search").performTextInput("Queen")

        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manolo Bellon").assertDoesNotExist()
    }

    // ─── HU05 - T9: Búsqueda sin resultados muestra lista vacía ──────────────

    @Test
    fun listScreen_busqueda_sinResultados_muestraListaVacia() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_search")
            .performTextInput("xyz_no_existe")

        composeTestRule.onNodeWithText("Manolo Bellon").assertDoesNotExist()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertDoesNotExist()
    }

    // ─── HU05 - T10: Limpiar búsqueda restaura lista completa ────────────────

    @Test
    fun listScreen_limpiarBusqueda_restauraListaCompleta() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_search").performTextInput("Manolo")
        composeTestRule.onNodeWithText("Jaime Monsalve").assertDoesNotExist()

        composeTestRule.onNodeWithTag("collector_search").performTextClearance()

        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
    }

    // ─── HU05 - T11: Botón mostrar más visible cuando hay más ────────────────

    @Test
    fun listScreen_botonMostrarMas_visibleCuandoHayMas() {
        val manyCollectors = (1..5).map {
            Collector(it, "Collector $it", telephone = "", email = "c$it@test.com")
        }
        val viewModel = createViewModel(manyCollectors)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_load_more"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_load_more").assertIsDisplayed()
    }

    // ─── HU05 - T12: Botón mostrar más no visible con pocos datos ────────────

    @Test
    fun listScreen_botonMostrarMas_noVisibleConPocosDatos() {
        val viewModel = createViewModel(fakeCollectors)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_load_more").assertDoesNotExist()
    }

    // ─── HU05 - T13: Botón mostrar más se oculta al buscar ───────────────────

    @Test
    fun listScreen_botonMostrarMas_seOcultaAlBuscar() {
        val manyCollectors = (1..5).map {
            Collector(it, "Collector $it", telephone = "", email = "c$it@test.com")
        }
        val viewModel = createViewModel(manyCollectors)

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_load_more"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("collector_load_more").assertIsDisplayed()

        composeTestRule.onNodeWithTag("collector_search").performTextInput("Collector")
        composeTestRule.onNodeWithTag("collector_load_more").assertDoesNotExist()
    }

    // ─── HU05 - T14 (nuevo): Rol COLLECTOR renderiza correctamente ───────────

    @Test
    fun listScreen_renderizaCorrectamente_conRolCollector() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                CollectorListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("collector_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
    }
}
