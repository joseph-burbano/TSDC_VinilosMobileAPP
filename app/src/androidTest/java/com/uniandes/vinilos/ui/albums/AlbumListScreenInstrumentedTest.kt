package com.uniandes.vinilos.ui.albums

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
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
            performers = listOf(Performer(1, "The Beatles", "", "Banda"))
        ),
        Album(
            id = 2, name = "Thriller", cover = "", releaseDate = "1982",
            description = "desc", genre = "Pop", recordLabel = "Epic",
            tracks = emptyList(),
            performers = listOf(Performer(2, "Michael Jackson", "", "Solista"))
        )
    )

    private fun viewModelWith(
        repoBlock: AlbumRepository.() -> Unit
    ): AlbumViewModel {
        val repo = mockk<AlbumRepository>(relaxed = true).apply(repoBlock)
        return AlbumViewModel(repo)
    }

    // ─── HU01 - T1: Skeleton mientras carga ──────────────────────────────────

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
                AlbumListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(AlbumListTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── HU01 - T2: Lista de álbumes con nombres tras carga exitosa ───────────

    @Test
    fun listScreen_muestraAlbumesConSusNombres_cuandoCargaExitosa() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns sampleAlbums
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 encontrados").assertIsDisplayed()
        composeTestRule.onNodeWithTag(AlbumListTestTags.cardFor(1)).assertIsDisplayed()
    }

    // ─── HU01 - T3: Mensaje de error cuando falla el servicio ────────────────

    @Test
    fun listScreen_muestraMensajeDeError_cuandoFallaElServicio() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } throws java.io.IOException("sin red")
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
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

    // ─── HU01 - T4: Botón reintentar dispara nuevo fetch ─────────────────────

    @Test
    fun listScreen_botonReintentar_disparaUnNuevoFetch() {
        val repo = mockk<AlbumRepository>(relaxed = true)
        coEvery { repo.getAlbums() } throws java.io.IOException("offline")
        coEvery { repo.refreshAlbums() } returns sampleAlbums
        val viewModel = AlbumViewModel(repo)

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
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

    // ─── HU01 - T5: Lista vacía muestra estado vacío ─────────────────────────

    @Test
    fun listScreen_listaVacia_muestraEstadoVacio() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns emptyList()
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.VISITOR
                )
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

    // ─── HU01 - T6 (nuevo): Rol COLLECTOR ve el botón + flotante ─────────────

    @Test
    fun listScreen_renderizaCorrectamente_conRolCollector() {
        val viewModel = viewModelWith {
            coEvery { getAlbums() } returns sampleAlbums
        }

        composeTestRule.setContent {
            VinilosTheme {
                AlbumListScreen(
                    viewModel = viewModel,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(AlbumListTestTags.LIST))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Con rol COLLECTOR la lista carga correctamente
        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 encontrados").assertIsDisplayed()
    }
}
