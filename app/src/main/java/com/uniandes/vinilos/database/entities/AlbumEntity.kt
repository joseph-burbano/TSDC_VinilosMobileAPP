package com.uniandes.vinilos.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uniandes.vinilos.model.Artist
import com.uniandes.vinilos.model.Track

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
    val tracks: List<Track>,
    val artists: List<Artist>
)
