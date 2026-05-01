package com.uniandes.vinilos.util

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Track

object FakeData {

    val performers = listOf(
        Performer(1, "Miles Davis", "", "American jazz trumpeter, bandleader and composer", "1926-05-26"),
        Performer(2, "John Coltrane", "", "American jazz saxophonist and composer", "1926-09-23"),
        Performer(3, "Nina Simone", "", "American singer, songwriter, pianist and civil rights activist", "1933-02-21"),
        Performer(4, "Led Zeppelin", "", "English rock band formed in London", "1968-01-01"),
        Performer(5, "The Beatles", "", "English rock band from Liverpool, Merseyside", "1960-01-01")
    )

    val albums = listOf(
        Album(
            id = 1,
            name = "In a Silent Way",
            cover = "",
            releaseDate = "1969",
            description = "A milestone of jazz fusion. Este álbum marcó el giro decisivo de Miles Davis hacia la música eléctrica, fundiendo jazz con rock y música del mundo.",
            genre = "Jazz",
            recordLabel = "Columbia",
            tracks = listOf(
                Track(1, "Shhh/Peaceful", "18:19"),
                Track(2, "In a Silent Way/It's About That Time", "20:05")
            ),
            performers = listOf(performers[0])
        ),
        Album(
            id = 2,
            name = "Kind of Blue",
            cover = "",
            releaseDate = "1959",
            description = "El álbum de jazz más vendido de todos los tiempos. Ampliamente considerado una de las grabaciones más grandes e influyentes en la historia de la música.",
            genre = "Jazz",
            recordLabel = "Columbia",
            tracks = listOf(
                Track(3, "So What", "9:22"),
                Track(4, "Freddie Freeloader", "9:46"),
                Track(5, "Blue in Green", "5:37"),
                Track(6, "All Blues", "11:33"),
                Track(7, "Flamenco Sketches", "9:26")
            ),
            performers = listOf(performers[0], performers[1])
        ),
        Album(
            id = 3,
            name = "Bitches Brew",
            cover = "",
            releaseDate = "1970",
            description = "Un doble álbum emblemático que fusionó la improvisación del jazz con ritmos de rock e instrumentos eléctricos, redefiniendo los límites del género.",
            genre = "Jazz Fusion",
            recordLabel = "Columbia",
            tracks = listOf(
                Track(8, "Pharaoh's Dance", "20:05"),
                Track(9, "Bitches Brew", "26:58"),
                Track(10, "Spanish Key", "17:31"),
                Track(11, "Miles Runs the Voodoo Down", "14:01")
            ),
            performers = listOf(performers[0])
        ),
        Album(
            id = 4,
            name = "I Put a Spell on You",
            cover = "",
            releaseDate = "1965",
            description = "Una poderosa muestra de la voz y el estilo único de Nina Simone, mezclando jazz, blues, folk y música clásica en un álbum atemporal.",
            genre = "Soul",
            recordLabel = "Philips",
            tracks = listOf(
                Track(12, "I Put a Spell on You", "3:26"),
                Track(13, "Tomorrow Is My Turn", "3:09"),
                Track(14, "Ne Me Quitte Pas", "3:47"),
                Track(15, "Feeling Good", "2:56")
            ),
            performers = listOf(performers[2])
        ),
        Album(
            id = 5,
            name = "Led Zeppelin IV",
            cover = "",
            releaseDate = "1971",
            description = "El cuarto álbum de estudio contiene himnos del rock intemporales, incluyendo Stairway to Heaven. Considerado uno de los grandes álbumes de rock de todos los tiempos.",
            genre = "Rock",
            recordLabel = "Atlantic",
            tracks = listOf(
                Track(16, "Black Dog", "4:54"),
                Track(17, "Rock and Roll", "3:40"),
                Track(18, "The Battle of Evermore", "5:51"),
                Track(19, "Stairway to Heaven", "8:02"),
                Track(20, "Misty Mountain Hop", "4:38"),
                Track(21, "Four Sticks", "4:44"),
                Track(22, "Going to California", "3:31"),
                Track(23, "When the Levee Breaks", "7:08")
            ),
            performers = listOf(performers[3])
        ),
        Album(
            id = 6,
            name = "Abbey Road",
            cover = "",
            releaseDate = "1969",
            description = "El undécimo álbum de estudio, ampliamente considerado uno de los mejores jamás grabados. Célebre por su medley del lado B y la icónica portada en el cruce peatonal.",
            genre = "Rock",
            recordLabel = "Apple",
            tracks = listOf(
                Track(24, "Come Together", "4:20"),
                Track(25, "Something", "3:03"),
                Track(26, "Maxwell's Silver Hammer", "3:27"),
                Track(27, "Oh! Darling", "3:27"),
                Track(28, "Octopus's Garden", "2:51"),
                Track(29, "Here Comes the Sun", "3:05"),
                Track(30, "Because", "2:45"),
                Track(31, "You Never Give Me Your Money", "4:02"),
                Track(32, "The End", "2:04")
            ),
            performers = listOf(performers[4])
        ),
        Album(
            id = 7,
            name = "A Love Supreme",
            cover = "",
            releaseDate = "1965",
            description = "Una suite de jazz en cuatro partes, ampliamente considerada la obra maestra de Coltrane. Un viaje espiritual profundo expresado a través de la música.",
            genre = "Jazz",
            recordLabel = "Impulse!",
            tracks = listOf(
                Track(33, "Acknowledgement", "7:47"),
                Track(34, "Resolution", "7:23"),
                Track(35, "Pursuance", "10:51"),
                Track(36, "Psalm", "7:03")
            ),
            performers = listOf(performers[1])
        ),
        Album(
            id = 8,
            name = "Little Girl Blue",
            cover = "",
            releaseDate = "1958",
            description = "El álbum debut que presentó a Nina Simone al mundo, mostrando su extraordinaria formación clásica y su sensibilidad jazzística única.",
            genre = "Jazz",
            recordLabel = "Bethlehem",
            tracks = listOf(
                Track(37, "Mood Indigo", "5:09"),
                Track(38, "He Needs Me", "2:59"),
                Track(39, "Little Girl Blue", "5:15"),
                Track(40, "Love Me or Leave Me", "4:23")
            ),
            performers = listOf(performers[2])
        )
    )

    val collectors = listOf(
    Collector(1, "Alejandro Vance", "3001234567", "alejandro@vinilos.com",
        description = "Preserving the warmth of analog signals since 1998.",
        image = ""),
    Collector(2, "Marcus Thorne", "3007654321", "marcus@vinilos.com",
        description = "Specialized in early Blue Note pressings.",
        image = "")
)
}
