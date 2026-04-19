package com.uniandes.vinilos.util

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Artist
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Track

object FakeData {

    val albums = listOf(
        Album(
            id = 1,
            name = "In a Silent Way",
            cover = "",
            releaseDate = "1969",
            description = "A milestone of jazz fusion",
            genre = "Jazz",
            recordLabel = "Columbia",
            tracks = listOf(
                Track(1, "Shhh/Peaceful", "18:19"),
                Track(2, "In a Silent Way", "20:05")
            )
        ),
        Album(
            id = 2,
            name = "Kind of Blue",
            cover = "",
            releaseDate = "1959",
            description = "The best-selling jazz album of all time",
            genre = "Jazz",
            recordLabel = "Columbia",
            tracks = listOf(
                Track(3, "So What", "9:22"),
                Track(4, "Blue in Green", "5:37")
            )
        ),
        Album(
            id = 3,
            name = "Bitches Brew",
            cover = "",
            releaseDate = "1970",
            description = "A landmark of jazz fusion",
            genre = "Jazz Fusion",
            recordLabel = "Columbia"
        )
    )

    val artists = listOf(
        Artist(
            id = 1,
            name = "Miles Davis",
            image = "",
            description = "American jazz trumpeter and composer",
            birthDate = "1926-05-26"
        ),
        Artist(
            id = 2,
            name = "John Coltrane",
            image = "",
            description = "American jazz saxophonist and composer",
            birthDate = "1926-09-23"
        ),
        Artist(
            id = 3,
            name = "Nina Simone",
            image = "",
            description = "American singer, songwriter and pianist",
            birthDate = "1933-02-21"
        )
    )

    val collectors = listOf(
        Collector(
            id = 1,
            name = "Alejandro Vance",
            telephone = "3001234567",
            email = "alejandro@vinilos.com"
        ),
        Collector(
            id = 2,
            name = "Marcus Thorne",
            telephone = "3007654321",
            email = "marcus@vinilos.com"
        )
    )
}