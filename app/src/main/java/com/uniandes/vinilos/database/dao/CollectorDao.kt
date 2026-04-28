package com.uniandes.vinilos.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uniandes.vinilos.database.entities.CollectorEntity

@Dao
interface CollectorDao {

    @Query("SELECT * FROM collectors")
    suspend fun getAll(): List<CollectorEntity>

    @Query("SELECT * FROM collectors WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): CollectorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collectors: List<CollectorEntity>)

    @Query("DELETE FROM collectors")
    suspend fun deleteAll()
}
