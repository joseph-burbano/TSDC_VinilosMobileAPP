package com.uniandes.vinilos.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "performers")
data class PerformerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val image: String,
    val description: String,
    val birthDate: String? = null,
    val creationDate: String? = null
)
