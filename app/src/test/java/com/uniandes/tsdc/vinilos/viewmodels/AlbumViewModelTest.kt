package com.uniandes.tsdc.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.uniandes.tsdc.vinilos.models.Album
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AlbumViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `album data class properties are correct`() {
        val album = Album(
            id = 1,
            name = "Test Album",
            cover = "http://example.com/cover.jpg",
            releaseDate = "2023-01-01",
            description = "A test album",
            genre = "Rock",
            recordLabel = "Test Label"
        )
        assertEquals(1, album.id)
        assertEquals("Test Album", album.name)
        assertEquals("Rock", album.genre)
        assertEquals("Test Label", album.recordLabel)
    }

    @Test
    fun `album equality works correctly`() {
        val album1 = Album(1, "Test", "url", "2023", "desc", "Rock", "Label")
        val album2 = Album(1, "Test", "url", "2023", "desc", "Rock", "Label")
        val album3 = Album(2, "Other", "url2", "2024", "desc2", "Pop", "Label2")
        assertEquals(album1, album2)
        assertNotEquals(album1, album3)
    }
}
