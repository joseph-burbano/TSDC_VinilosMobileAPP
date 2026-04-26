package com.uniandes.vinilos.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uniandes.vinilos.database.entities.AlbumEntity

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums")
    suspend fun getAll(): List<AlbumEntity>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun deleteAll()
}
