package com.uniandes.vinilos

import com.uniandes.vinilos.database.dao.PerformerDao
import com.uniandes.vinilos.database.entities.PerformerEntity
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.network.VinilosApi
import com.uniandes.vinilos.repository.ArtistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArtistRepositoryTest {

    private lateinit var dao: PerformerDao
    private lateinit var api: VinilosApi
    private lateinit var repository: ArtistRepository

    private val fakeEntities = listOf(
        PerformerEntity(1, "Miles Davis", "", "Jazz trumpeter", "1926-05-26", null),
        PerformerEntity(2, "John Coltrane", "", "Jazz saxophonist", "1926-09-23", null)
    )

    private val fakePerformers = listOf(
        Performer(3, "Nina Simone", "", "Singer and pianist", "1933-02-21", null)
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repository = ArtistRepository(dao, api)
    }

    @Test
    fun `getPerformers returns cached data when dao is not empty`() = runTest {
        coEvery { dao.getAll() } returns fakeEntities

        val result = repository.getPerformers()

        assertEquals(2, result.size)
        assertEquals("Miles Davis", result[0].name)
        coVerify(exactly = 0) { api.getMusicians() }
        coVerify(exactly = 0) { api.getBands() }
    }

    @Test
    fun `getPerformers calls api when cache is empty`() = runTest {
        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getMusicians() } returns fakePerformers
        coEvery { api.getBands() } returns emptyList()

        val result = repository.getPerformers()

        assertTrue(result.isNotEmpty())
        coVerify(exactly = 1) { api.getMusicians() }
        coVerify(exactly = 1) { api.getBands() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    @Test
    fun `getPerformers combines musicians and bands from api`() = runTest {
        val musicians = listOf(Performer(1, "Miles Davis", "", "Trumpeter", null, null))
        val bands = listOf(Performer(2, "The Beatles", "", "Rock band", null, "1960-01-01"))

        coEvery { dao.getAll() } returns emptyList()
        coEvery { api.getMusicians() } returns musicians
        coEvery { api.getBands() } returns bands

        val result = repository.getPerformers()

        assertEquals(2, result.size)
    }

    @Test
    fun `refreshPerformers deletes cache and calls api`() = runTest {
        coEvery { api.getMusicians() } returns fakePerformers
        coEvery { api.getBands() } returns emptyList()

        repository.refreshPerformers()

        coVerify(exactly = 1) { dao.deleteAll() }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }
}
