package com.uniandes.vinilos.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VinilosApiContractTest {

    private lateinit var server: MockWebServer
    private lateinit var api: VinilosApi

    @Before
    fun start() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VinilosApi::class.java)
    }

    @After
    fun stop() {
        server.shutdown()
    }

    @Test
    fun `getAlbums hace GET a la ruta albums`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        api.getAlbums()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/albums", request.path)
    }

    @Test
    fun `getAlbums deserializa correctamente la respuesta del backend`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SAMPLE_ALBUMS_JSON))

        val albums = api.getAlbums()

        assertEquals(1, albums.size)
        val album = albums.first()
        assertEquals(1, album.id)
        assertEquals("Buscando América", album.name)
        assertEquals("Rock", album.genre)
        assertEquals("Elektra", album.recordLabel)
        assertEquals(1, album.performers.size)
        assertEquals("Rubén Blades", album.performers.first().name)
        assertEquals(2, album.tracks.size)
        assertEquals("Decisiones", album.tracks.first().name)
    }

    @Test
    fun `getAlbums tolera campos opcionales nulos del backend`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(ALBUM_WITH_NULLS_JSON))

        val albums = api.getAlbums()

        assertEquals(1, albums.size)
        val album = albums.first()
        assertEquals("Sin metadatos", album.name)
        assertTrue(album.tracks.isEmpty())
        assertTrue(album.performers.isEmpty())
    }

    private companion object {
        const val SAMPLE_ALBUMS_JSON = """
        [
          {
            "id": 1,
            "name": "Buscando América",
            "cover": "https://image",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "Álbum del cantautor Rubén Blades",
            "genre": "Rock",
            "recordLabel": "Elektra",
            "performers": [
              { "id": 100, "name": "Rubén Blades", "image": "img", "description": "Cantautor" }
            ],
            "tracks": [
              { "id": 1, "name": "Decisiones", "duration": "5:05" },
              { "id": 2, "name": "Desapariciones", "duration": "6:25" }
            ]
          }
        ]
        """

        const val ALBUM_WITH_NULLS_JSON = """
        [
          {
            "id": 99,
            "name": "Sin metadatos",
            "cover": null,
            "releaseDate": null,
            "description": null,
            "genre": null,
            "recordLabel": null,
            "performers": [],
            "tracks": []
          }
        ]
        """
    }
}
