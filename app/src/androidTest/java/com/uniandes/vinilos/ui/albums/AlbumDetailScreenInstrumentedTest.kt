package com.uniandes.vinilos.ui.albums

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumDetailScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbum = Album(
        id = 5,
        name = "Poeta del pueblo",
        cover = "",
        releaseDate = "1984",
        description = "Recopilación de 27 composiciones del cosmos Blades.",
        genre = "Salsa",
        recordLabel = "Elektra",
        tracks = listOf(
            Track(1, "Pedro Navaja", "7:21"),
            Track(2, "Plástico", "6:34")
        ),
        performers = listOf(
            Performer(
                id = 9,
                name = "Rubén Blades Bellido de Luna",
                image = "",
                description = "Es un cantante, compositor, músico, actor, abogado, político"
            )
        )
    )

    private fun viewModelWith(
        repoBlock: AlbumRepository.() -> Unit
    ): AlbumViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return AlbumViewModel(repo)
    }

    @Test
    fun detailScreen_muestraSpinnerMientrasCarga() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } coAnswers {
                delay(2_000)
                listOf(sampleAlbum)
            }
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumDetailScreen(albumId = sampleAlbum.id, viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag(AlbumDetailTestTags.SCREEN).assertDoesNotExist()
    }

    @Test
    fun detailScreen_renderizaInformacionDelAlbum_cuandoCargaExitosa() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns listOf(sampleAlbum)
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumDetailScreen(albumId = sampleAlbum.id, viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Poeta del pueblo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()
        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elektra").assertIsDisplayed()
        composeTestRule.onNodeWithText("ARTISTAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("CANCIONES").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pedro Navaja").assertExists()
    }

    @Test
    fun detailScreen_botonVolver_invocaCallbackOnBack() {
        var backInvoked = false
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns listOf(sampleAlbum)
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumDetailScreen(
                    albumId = sampleAlbum.id,
                    viewModel = viewModel,
                    onBack = { backInvoked = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumDetailTestTags.BACK))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(AlbumDetailTestTags.BACK).performClick()
        assertTrue(backInvoked)
    }

    @Test
    fun detailScreen_muestraMensajeDeError_cuandoFallaElServicio() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } throws java.io.IOException("sin red")
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumDetailScreen(albumId = sampleAlbum.id, viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("Sin conexión. Revisa tu red e inténtalo de nuevo.")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Sin conexión. Revisa tu red e inténtalo de nuevo.")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraAlbumNoEncontrado_cuandoIdNoExiste() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns listOf(sampleAlbum)
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumDetailScreen(albumId = 9999, viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("Álbum no encontrado")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Álbum no encontrado").assertIsDisplayed()
    }
}
