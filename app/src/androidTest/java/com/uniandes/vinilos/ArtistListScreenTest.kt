package com.uniandes.vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.ui.artists.ArtistListScreen
import com.uniandes.vinilos.ui.artists.ArtistViewModel
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import com.uniandes.vinilos.repository.ArtistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistListScreenTest {

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
}
