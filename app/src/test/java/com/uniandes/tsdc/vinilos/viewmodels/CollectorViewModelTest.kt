package com.uniandes.tsdc.vinilos.viewmodels

import com.uniandes.tsdc.vinilos.models.Collector
import org.junit.Assert.*
import org.junit.Test

class CollectorViewModelTest {

    @Test
    fun `collector data class properties are correct`() {
        val collector = Collector(
            id = 1,
            name = "Test Collector",
            telephone = "1234567890",
            email = "test@example.com"
        )
        assertEquals(1, collector.id)
        assertEquals("Test Collector", collector.name)
        assertEquals("1234567890", collector.telephone)
        assertEquals("test@example.com", collector.email)
    }

    @Test
    fun `collector equality works correctly`() {
        val collector1 = Collector(1, "Test", "123", "test@test.com")
        val collector2 = Collector(1, "Test", "123", "test@test.com")
        val collector3 = Collector(2, "Other", "456", "other@test.com")
        assertEquals(collector1, collector2)
        assertNotEquals(collector1, collector3)
    }
}
