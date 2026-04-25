package com.uniandes.vinilos.ui.albums

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Artist
import com.uniandes.vinilos.repository.AlbumRepository
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumListScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbums = listOf(
        Album(
            id = 1, name = "Abbey Road", cover = "", releaseDate = "1969",
            description = "desc", genre = "Rock", recordLabel = "Apple",
            tracks = emptyList(),
            artists = listOf(Artist(1, "The Beatles", "", "Banda", ""))
        ),
        Album(
            id = 2, name = "Thriller", cover = "", releaseDate = "1982",
            description = "desc", genre = "Pop", recordLabel = "Epic",
            tracks = emptyList(),
            artists = listOf(Artist(2, "Michael Jackson", "", "Solista", ""))
        )
    )

    private fun viewModelWith(
        repoBlock: AlbumRepository.() -> Unit
    ): AlbumViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return AlbumViewModel(repo)
    }

    @Test
    fun listScreen_muestraSkeletonMientrasCarga() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } coAnswers {
                delay(2_000)
                sampleAlbums
            }
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithTag(AlbumListTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun listScreen_muestraAlbumesConSusNombres_cuandoCargaExitosa() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns sampleAlbums
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("Thriller").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 encontrados").assertIsDisplayed()
        composeTestRule.onNodeWithTag(AlbumListTestTags.cardFor(1)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(AlbumListTestTags.cardFor(2)).assertIsDisplayed()
    }

    @Test
    fun listScreen_muestraMensajeDeError_cuandoFallaElServicio() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } throws java.io.IOException("sin red")
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.ERROR))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(AlbumListTestTags.ERROR).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin conexión. Revisa tu red e inténtalo de nuevo.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun listScreen_botonReintentar_disparaUnNuevoFetch() {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } throws java.io.IOException("offline")
        coEvery { repo.refreshAlbums() } returns sampleAlbums
        val viewModel = AlbumViewModel(repo)

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.ERROR))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Reintentar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
    }

    @Test
    fun listScreen_listaVacia_muestraEstadoVacio() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns emptyList()
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("No se encontraron álbumes").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 encontrados").assertIsDisplayed()
    }
}
