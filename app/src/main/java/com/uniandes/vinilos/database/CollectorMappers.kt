package com.uniandes.vinilos.database

import com.uniandes.vinilos.database.entities.CollectorEntity
import com.uniandes.vinilos.model.Collector

fun CollectorEntity.toCollector() = Collector(
    id = id,
    name = name,
    telephone = telephone,
    email = email,
    birthDate = birthDate,
    description = description,
    image = image,
    collectorAlbums = collectorAlbums,
    favoritePerformers = favoritePerformers
)

fun Collector.toEntity() = CollectorEntity(
    id = id,
    name = name,
    telephone = telephone,
    email = email,
    birthDate = birthDate,
    description = description,
    image = image,
    collectorAlbums = collectorAlbums,
    favoritePerformers = favoritePerformers
)
