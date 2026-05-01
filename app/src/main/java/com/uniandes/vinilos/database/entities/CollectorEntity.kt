package com.uniandes.vinilos.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.model.Performer

@Entity(tableName = "collectors")
data class CollectorEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val telephone: String = "",
    val email: String = "",
    val birthDate: String? = null,
    val description: String = "",
    val image: String = "",
    val collectorAlbums: List<CollectorAlbum> = emptyList(),
    val favoritePerformers: List<Performer> = emptyList()
)
