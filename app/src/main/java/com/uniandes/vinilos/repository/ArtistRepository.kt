package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.PerformerDao
import com.uniandes.vinilos.database.toEntity
import com.uniandes.vinilos.database.toPerformer
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.VinilosApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ArtistRepository(
    private val dao: PerformerDao,
    private val api: VinilosApi = NetworkServiceAdapter.api
) {
    suspend fun getPerformers(): List<Performer> {
        val cached = dao.getAll()
        if (cached.isNotEmpty()) {
            return cached.map { it.toPerformer() }
        }
        val remote = coroutineScope {
            val musicians = async { api.getMusicians() }
            val bands = async { api.getBands() }
            musicians.await() + bands.await()
        }
        dao.insertAll(remote.map { it.toEntity() })
        return remote
    }

    suspend fun refreshPerformers(): List<Performer> {
        dao.deleteAll()
        val remote = coroutineScope {
            val musicians = async { api.getMusicians() }
            val bands = async { api.getBands() }
            musicians.await() + bands.await()
        }
        dao.insertAll(remote.map { it.toEntity() })
        return remote
    }
}
