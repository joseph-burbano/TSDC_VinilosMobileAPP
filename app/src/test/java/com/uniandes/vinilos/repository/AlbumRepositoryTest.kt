package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.entities.AlbumEntity
import com.uniandes.vinilos.model.Artist
import com.uniandes.vinilos.network.VinilosApi
import com.uniandes.vinilos.network.dto.AlbumDto
import com.uniandes.vinilos.network.dto.PerformerDto
import com.uniandes.vinilos.network.dto.TrackDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AlbumRepositoryTest {

    private lateinit var dao: AlbumDao
    private lateinit var api: VinilosApi
    private lateinit var repository: AlbumRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = AlbumRepository(dao, api)
    }

    @Test
    fun `getAlbums devuelve cache cuando el DAO no esta vacio`() = runTest {
        coEvery { dao.getAll() } returns listOf(ABBEY_ROAD_ENTITY)

        val result = repository.getAlbums()

        assertEquals(1, result.size)
        assertEquals("Abbey Road", result[0].name)
        coVerify(exactly = 0) { api.getAlbums() }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `getAlbums llama al API cuando el cache esta vacio y persiste el resultado`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_DTO)

        val result = repository.getAlbums()

        assertEquals(1, result.size)
        assertEquals("Abbey Road", result.first().name)
        coVerify(exactly = 1) { api.getAlbums() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getAlbums mapea performers a artists segun la convencion del proyecto`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_DTO)

        val album = repository.getAlbums().first()

        assertEquals(1, album.artists.size)
        assertEquals("The Beatles", album.artists.first().name)
    }

    @Test
    fun `getAlbums trunca releaseDate al anio`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_DTO)

        val album = repository.getAlbums().first()

        assertEquals("1969", album.releaseDate)
    }

    @Test
    fun `getAlbums convierte campos nullables del DTO en strings vacios`() = runTest {
        val dtoConNulls = AlbumDto(
            id = 99, name = "Sin datos",
            cover = null, releaseDate = null, description = null,
            genre = null, recordLabel = null,
            performers = listOf(PerformerDto(1, "X", null, null)),
            tracks = emptyList()
        )
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns listOf(dtoConNulls)

        val album = repository.getAlbums().first()

        assertEquals("", album.cover)
        assertEquals("", album.releaseDate)
        assertEquals("", album.genre)
        assertEquals("", album.recordLabel)
    }

    @Test
    fun `refreshAlbums borra el cache y vuelve a llamar al API`() = runTest {
        coEvery { api.getAlbums() } returns listOf(ABBEY_ROAD_DTO)

        repository.refreshAlbums()

        coVerify(exactly = 1) { dao.deleteAll() }
        coVerify(exactly = 1) { api.getAlbums() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getAlbums devuelve lista vacia cuando el API devuelve vacio`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } returns emptyList()

        val result = repository.getAlbums()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAlbums propaga las excepciones del API`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getAlbums() } throws IOException("sin red")

        val thrown = assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getAlbums() }
        }
        assertEquals("sin red", thrown.message)
    }

    private companion object {
        val ABBEY_ROAD_DTO = AlbumDto(
            id = 1,
            name = "Abbey Road",
            cover = "https://img/cover.png",
            releaseDate = "1969-09-26T00:00:00.000Z",
            description = "Último álbum grabado por The Beatles.",
            genre = "Rock",
            recordLabel = "Apple Records",
            performers = listOf(
                PerformerDto(10, "The Beatles", "https://img/beatles.png", "Banda")
            ),
            tracks = listOf(TrackDto(1, "Come Together", "4:20"))
        )

        val ABBEY_ROAD_ENTITY = AlbumEntity(
            id = 1,
            name = "Abbey Road",
            cover = "",
            releaseDate = "1969",
            description = "Cached.",
            genre = "Rock",
            recordLabel = "Apple",
            tracks = emptyList(),
            artists = listOf(Artist(10, "The Beatles", "", "", ""))
        )
    }
}
