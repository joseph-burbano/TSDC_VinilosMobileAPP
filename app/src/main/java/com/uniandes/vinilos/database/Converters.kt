package com.uniandes.vinilos.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Track

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun tracksToJson(tracks: List<Track>): String = gson.toJson(tracks)

    @TypeConverter
    fun jsonToTracks(json: String): List<Track> {
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    fun performersToJson(performers: List<Performer>): String = gson.toJson(performers)

    @TypeConverter
    fun jsonToPerformers(json: String): List<Performer> {
        val type = object : TypeToken<List<Performer>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    fun albumsToJson(albums: List<Album>): String = gson.toJson(albums)

    @TypeConverter
    fun jsonToAlbums(json: String): List<Album> {
        val type = object : TypeToken<List<Album>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    fun collectorAlbumsToJson(items: List<CollectorAlbum>): String = gson.toJson(items)

    @TypeConverter
    fun jsonToCollectorAlbums(json: String): List<CollectorAlbum> {
        val type = object : TypeToken<List<CollectorAlbum>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
