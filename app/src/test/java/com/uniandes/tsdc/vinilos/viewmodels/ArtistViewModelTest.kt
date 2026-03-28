package com.uniandes.tsdc.vinilos.viewmodels

import com.uniandes.tsdc.vinilos.models.Artist
import org.junit.Assert.*
import org.junit.Test

class ArtistViewModelTest {

    @Test
    fun `artist data class properties are correct`() {
        val artist = Artist(
            id = 1,
            name = "Test Artist",
            image = "http://example.com/image.jpg",
            description = "A test artist",
            birthDate = "1990-01-01"
        )
        assertEquals(1, artist.id)
        assertEquals("Test Artist", artist.name)
        assertEquals("1990-01-01", artist.birthDate)
    }

    @Test
    fun `artist equality works correctly`() {
        val artist1 = Artist(1, "Test", "url", "desc", "1990-01-01")
        val artist2 = Artist(1, "Test", "url", "desc", "1990-01-01")
        val artist3 = Artist(2, "Other", "url2", "desc2", "2000-01-01")
        assertEquals(artist1, artist2)
        assertNotEquals(artist1, artist3)
    }
}
