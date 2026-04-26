package com.uniandes.vinilos.ui.artists

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistDetailScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Datos de prueba: músico con un álbum
    private val sampleAlbum = Album(
        id = 10, name = "Kind of Blue", cover = "", releaseDate = "1959-08-17",
        description = "Jazz masterpiece", genre = "Jazz", recordLabel = "Columbia"
    )
    private val samplePerformer = Performer(
        id = 1, name = "Miles Davis", image = "", description = "American jazz trumpeter.",
        birthDate = "1926-05-26", albums = listOf(sampleAlbum)
    )

    // Helper: crea un ViewModel mockeado con estado configurado
    private fun mockViewModel(
        isLoading: Boolean = false,
        performer: Performer? = samplePerformer
    ): ArtistViewModel {
        val vm = mockk<ArtistViewModel>(relaxed = true)
        every { vm.isLoading } returns MutableStateFlow(isLoading)
        every { vm.findById(any()) } returns performer
        return vm
    }

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoIsLoadingEsTrue() {
        val vm = mockViewModel(isLoading = true)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(artistId = 1, viewModel = vm)
            }
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraIndicadorDeCarga_cuandoPerformerEsNull() {
        val vm = mockViewModel(isLoading = false, performer = null)

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(artistId = 999, viewModel = vm)
            }
        }

        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraNombreDelArtista_cuandoCargaExitosa() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(artistId = samplePerformer.id, viewModel = vm)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // El nombre aparece dos veces en pantalla (hero + descripción),
        // usamos el testTag para apuntar específicamente al nodo del hero
        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.NAME)
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraSectionDiscografia_cuandoArtistaTieneAlbumes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(artistId = samplePerformer.id, viewModel = vm)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // La sección de álbumes puede estar fuera del viewport inicial
        // (la pantalla es scrollable), assertExists verifica que esté en la composición
        composeTestRule
            .onNodeWithTag(ArtistDetailTestTags.ALBUMS)
            .assertExists()

        composeTestRule
            .onNodeWithText("Kind of Blue")
            .assertExists()
    }

    @Test
    fun detailScreen_botonAtras_invocaCallbackOnBack() {
        var backInvoked = false
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                ArtistDetailScreen(
                    artistId = samplePerformer.id,
                    viewModel = vm,
                    onBack = { backInvoked = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(ArtistDetailTestTags.BACK))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ArtistDetailTestTags.BACK).performClick()
        assertTrue(backInvoked)
    }
}
