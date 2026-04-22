package com.uniandes.vinilos

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import com.uniandes.vinilos.repository.ArtistRepository
import kotlin.time.Duration.Companion.seconds

class ArtistRepositoryTest {

    private val repository = ArtistRepository()


@Test
    fun `getMusicians returns non-empty list`() = runTest(timeout = 30.seconds) {
        val performers = repository.getMusicians()
        assertTrue("La lista no debe estar vacía", performers.isNotEmpty())
        performers.forEach { println("- ${it.name}") }
    }

    @Test
    fun `getBands returns non-empty list`() = runTest {
        val performers = repository.getBands()
        assertTrue("La lista no debe estar vacía", performers.isNotEmpty())
        println("Bandas encontradas: ${performers.size}")
        performers.forEach { println("- ${it.name}") }
    }

    @Test
    fun `getPerformers combines musicians and bands`() = runTest {
        val performers = repository.getPerformers()
        assertTrue("La lista combinada no debe estar vacía", performers.isNotEmpty())
        println("Total performers: ${performers.size}")
    }
}
