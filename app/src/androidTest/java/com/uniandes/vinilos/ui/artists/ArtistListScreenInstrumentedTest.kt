package com.uniandes.vinilos.ui.artists

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.repository.ArtistRepository
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.performScrollToIndex

@RunWith(AndroidJUnit4::class)
class ArtistListScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakePerformers = listOf(
        Performer(1, "Rubén Blades", "https://example.com/img1.jpg", "Cantante panameño", "1948-07-16"),
        Performer(2, "Queen", "https://example.com/img2.jpg", "Banda británica de rock", null, "1970-01-01")
    )

    private fun createViewModel(performers: List<Performer> = fakePerformers): ArtistViewModel {
        val repository = mockk<ArtistRepository>(relaxed = true)
        coEvery { repository.getPerformers() } returns performers
        return ArtistViewModel(repository)
    }

    // ─── HU03 - T1: Spinner mientras carga ───────────────────────────────────

    @Test
    fun artistList_showsLoadingIndicator_initially() {
        val repository = mockk<ArtistRepository>(relaxed = true)
        coEvery { repository.getPerformers() } coAnswers {
            kotlinx.coroutines.delay(2000)
            fakePerformers
        }
        val viewModel = ArtistViewModel(repository)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }

    // ─── HU03 - T2: Nombres de artistas visibles tras carga ──────────────────

    @Test
    fun artistList_showsArtistNames_whenLoaded() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Rubén Blades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
    }

    // ─── HU03 - T3: Grid con items de artistas ───────────────────────────────

    @Test
    fun artistList_showsGrid_withArtistItems() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("artist_item_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("artist_item_2").assertIsDisplayed()
    }

    // ─── HU03 - T4: Mensaje de error cuando falla el servicio ────────────────

    @Test
    fun artistList_showsErrorMessage_whenLoadFails() {
        val repository = mockk<ArtistRepository>(relaxed = true)
        coEvery { repository.getPerformers() } throws Exception("Sin conexión")
        val viewModel = ArtistViewModel(repository)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(hasTestTag("error_message"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
    }

    // ─── HU03 - T5: Búsqueda filtra por nombre ───────────────────────────────

    @Test
    fun artistList_searchFilters_showsMatchingArtist() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("Rubén")

        composeTestRule.onNodeWithText("Rubén Blades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertDoesNotExist()
    }

    // ─── HU03 - T6: Búsqueda es case-insensitive ─────────────────────────────

    @Test
    fun artistList_searchFilters_isCaseInsensitive() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("queen")

        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rubén Blades").assertDoesNotExist()
    }

    // ─── HU03 - T7: Búsqueda sin resultados muestra grid vacío ──────────────

    @Test
    fun artistList_searchFilters_noResults_showsEmptyGrid() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("xyz_no_existe")

        composeTestRule.onNodeWithText("Rubén Blades").assertDoesNotExist()
        composeTestRule.onNodeWithText("Queen").assertDoesNotExist()
    }

    // ─── HU03 - T8: Limpiar búsqueda restaura lista completa ─────────────────

    @Test
    fun artistList_clearSearch_restoresFullList() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("Rubén")
        composeTestRule.onNodeWithText("Queen").assertDoesNotExist()

        composeTestRule.onNodeWithTag("artist_search").performTextClearance()

        composeTestRule.onNodeWithText("Rubén Blades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
    }

    // ─── HU03 - T9: Click en artista invoca callback con ID correcto ──────────

    @Test
    fun artistList_clickArtist_invokesCallbackWithCorrectId() {
        val viewModel = createViewModel()
        var clickedId: Int? = null

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    onArtistClick = { clickedId = it },
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_item_1"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_item_1").performClick()

        assert(clickedId == 1)
    }

    // ─── HU03 - T10: Botón cargar más visible con 7+ artistas ────────────────

    @Test
    fun artistList_loadMoreButton_isVisibleWhenHasMore() {
        val manyPerformers = (1..5).map {
            Performer(it, "A$it", "https://example.com/$it.jpg", "Bio", "2000-01-01")
        }
        val viewModel = createViewModel(manyPerformers)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("artist_list")
            .performScrollToIndex(5)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("load_more_button"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("load_more_button").assertExists()   
    }

    // ─── HU03 - T11: Botón cargar más no visible con pocos artistas ──────────

    @Test
    fun artistList_loadMoreButton_notVisibleWithFewArtists() {
        val viewModel = createViewModel(fakePerformers)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("load_more_button").assertDoesNotExist()
    }

    // ─── HU03 - T12: Botón cargar más se oculta al buscar ────────────────────

    @Test
    fun artistList_loadMoreButton_hidesWhenSearchIsActive() {
        val manyPerformers = (1..5).map {
            Performer(it, "A$it", "https://example.com/$it.jpg", "Bio", "2000-01-01")
        }
        val viewModel = createViewModel(manyPerformers)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("artist_list")
            .performScrollToIndex(5)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("load_more_button"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("load_more_button").assertExists()

        composeTestRule.onNodeWithTag("artist_search").performTextInput("A")
        composeTestRule.onNodeWithTag("load_more_button").assertDoesNotExist()
    }

    // ─── HU03 - T13 (nuevo): Rol COLLECTOR renderiza correctamente ───────────

    @Test
    fun artistList_renderizaCorrectamente_conRolCollector() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Rubén Blades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
    }
}
