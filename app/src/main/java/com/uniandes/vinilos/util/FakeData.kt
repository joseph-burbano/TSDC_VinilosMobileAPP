package com.uniandes.vinilos.util

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Track

object FakeData {

    val performers = listOf(
        Performer(
            id = 1,
            name = "Miles Davis",
            image = "",
            description = "American jazz trumpeter and composer",
            birthDate = "1926-05-26"
        ),
        Performer(
            id = 2,
            name = "John Coltrane",
            image = "",
            description = "American jazz saxophonist and composer",
            birthDate = "1926-09-23"
        ),
        Performer(
            id = 3,
            name = "Nina Simone",
            image = "",
            description = "American singer, songwriter and pianist",
            birthDate = "1933-02-21"
        )
    )

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
        )
    )

    val collectors = listOf(
        Collector(
            id = 1,
            name = "Alejandro Vance",
            telephone = "3001234567",
            email = "alejandro@vinilos.com"
        )
    )
}
