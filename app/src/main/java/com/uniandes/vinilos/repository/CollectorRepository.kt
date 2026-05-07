package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.CollectorDao
import com.uniandes.vinilos.database.toCollector
import com.uniandes.vinilos.database.toEntity
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.VinilosApi

class CollectorRepository(
    private val dao: CollectorDao,
    private val api: VinilosApi = NetworkServiceAdapter.api
) {

    suspend fun getCollectors(): List<Collector> {
        val cached = dao.getAll()
        if (cached.isNotEmpty()) return cached.map { it.toCollector() }
        return fetchAndCache()
    }

    suspend fun refreshCollectors(): List<Collector> {
        dao.deleteAll()
        return fetchAndCache()
    }

    suspend fun getCollector(id: Int): Collector? {
        val cached = dao.findById(id)
        if (cached != null) return cached.toCollector()
        return try {
            val remote = api.getCollector(id)
            dao.insertAll(listOf(remote.toEntity()))
            remote
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchAndCache(): List<Collector> {
        val collectors = api.getCollectors()
        dao.insertAll(collectors.map { it.toEntity() })
        return collectors
    }
}
