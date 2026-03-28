package com.uniandes.tsdc.vinilos.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.uniandes.tsdc.vinilos.models.Album
import com.uniandes.tsdc.vinilos.models.Artist
import com.uniandes.tsdc.vinilos.models.Collector
import org.json.JSONArray

class NetworkServiceAdapter(context: Context) {

    companion object {
        private const val BASE_URL = "https://back-vynils-heroku.herokuapp.com/"

        @Volatile
        private var INSTANCE: NetworkServiceAdapter? = null

        fun getInstance(context: Context): NetworkServiceAdapter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkServiceAdapter(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun getAlbums(onSuccess: (List<Album>) -> Unit, onError: (Exception) -> Unit) {
        val url = "${BASE_URL}albums"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val albums = parseAlbums(response)
                    onSuccess(albums)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(Exception(error.message ?: "Unknown error"))
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    fun getArtists(onSuccess: (List<Artist>) -> Unit, onError: (Exception) -> Unit) {
        val url = "${BASE_URL}musicians"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val artists = parseArtists(response)
                    onSuccess(artists)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(Exception(error.message ?: "Unknown error"))
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    fun getCollectors(onSuccess: (List<Collector>) -> Unit, onError: (Exception) -> Unit) {
        val url = "${BASE_URL}collectors"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val collectors = parseCollectors(response)
                    onSuccess(collectors)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(Exception(error.message ?: "Unknown error"))
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun parseAlbums(jsonArray: JSONArray): List<Album> {
        val albums = mutableListOf<Album>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            albums.add(
                Album(
                    id = json.getInt("id"),
                    name = json.getString("name"),
                    cover = json.getString("cover"),
                    releaseDate = json.getString("releaseDate"),
                    description = json.getString("description"),
                    genre = json.getString("genre"),
                    recordLabel = json.getString("recordLabel")
                )
            )
        }
        return albums
    }

    private fun parseArtists(jsonArray: JSONArray): List<Artist> {
        val artists = mutableListOf<Artist>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            artists.add(
                Artist(
                    id = json.getInt("id"),
                    name = json.getString("name"),
                    image = json.getString("image"),
                    description = json.getString("description"),
                    birthDate = json.getString("birthDate")
                )
            )
        }
        return artists
    }

    private fun parseCollectors(jsonArray: JSONArray): List<Collector> {
        val collectors = mutableListOf<Collector>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            collectors.add(
                Collector(
                    id = json.getInt("id"),
                    name = json.getString("name"),
                    telephone = json.getString("telephone"),
                    email = json.getString("email")
                )
            )
        }
        return collectors
    }
}
