package com.uniandes.vinilos

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
import com.uniandes.vinilos.ui.artists.ArtistListScreen
import com.uniandes.vinilos.ui.artists.ArtistViewModel
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import com.uniandes.vinilos.repository.ArtistRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
                ArtistListScreen(viewModel = viewModel)
            }
        }
        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }

    @Test
    fun artistList_showsArtistNames_whenLoaded() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Rubén Blades")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Queen")
            .assertIsDisplayed()
    }

    @Test
    fun artistList_showsGrid_withArtistItems() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("artist_list")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("artist_item_1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("artist_item_2")
            .assertIsDisplayed()
    }

    @Test
    fun artistList_showsErrorMessage_whenLoadFails() {
        val repository = mockk<ArtistRepository>(relaxed = true)
        coEvery { repository.getPerformers() } throws Exception("Sin conexión")
        val viewModel = ArtistViewModel(repository)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("error_message"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("error_message")
            .assertIsDisplayed()
    }

    // ── BÚSQUEDA ──────────────────────────────────────────────────────────────────
    @Test
    fun artistList_searchFilters_showsMatchingArtist() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("Rubén")

        composeTestRule.onNodeWithText("Rubén Blades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertDoesNotExist()
    }

    @Test
    fun artistList_searchFilters_isCaseInsensitive() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("queen")

        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rubén Blades").assertDoesNotExist()
    }

    @Test
    fun artistList_searchFilters_noResults_showsEmptyGrid() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("artist_search").performTextInput("xyz_no_existe")

        composeTestRule.onNodeWithText("Rubén Blades").assertDoesNotExist()
        composeTestRule.onNodeWithText("Queen").assertDoesNotExist()
    }

    @Test
    fun artistList_clearSearch_restoresFullList() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
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

    // ── NAVEGACIÓN ────────────────────────────────────────────────────────────────

    @Test
    fun artistList_clickArtist_invokesCallbackWithCorrectId() {
        val viewModel = createViewModel()
        var clickedId: Int? = null

        composeTestRule.setContent {
            VinilosTheme {
                ArtistListScreen(
                    viewModel = viewModel,
                    onArtistClick = { clickedId = it }
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

    // ── PAGINACIÓN ────────────────────────────────────────────────────────────────

    @Test
    fun artistList_loadMoreButton_isVisibleWhenHasMore() {
        // El ViewModel pagina de a 6 por defecto; con 7+ artistas aparece el botón
        val manyPerformers = (1..7).map {
            Performer(it, "Artist $it", "https://example.com/$it.jpg", "Bio", "2000-01-01")
        }
        val viewModel = createViewModel(manyPerformers)

        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("load_more_button"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("load_more_button").assertIsDisplayed()
    }

    @Test
    fun artistList_loadMoreButton_notVisibleWithFewArtists() {
        // Con 2 artistas no debe aparecer el botón
        val viewModel = createViewModel(fakePerformers)

        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("artist_list"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("load_more_button").assertDoesNotExist()
    }

    @Test
    fun artistList_loadMoreButton_hidesWhenSearchIsActive() {
        val manyPerformers = (1..7).map {
            Performer(it, "Artist $it", "https://example.com/$it.jpg", "Bio", "2000-01-01")
        }
        val viewModel = createViewModel(manyPerformers)

        composeTestRule.setContent {
            VinilosTheme { ArtistListScreen(viewModel = viewModel) }
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("load_more_button"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // El botón existe antes de buscar
        composeTestRule.onNodeWithTag("load_more_button").assertIsDisplayed()

        // Al escribir en el search, el botón desaparece
        composeTestRule.onNodeWithTag("artist_search").performTextInput("Artist")
        composeTestRule.onNodeWithTag("load_more_button").assertDoesNotExist()
    }
}
