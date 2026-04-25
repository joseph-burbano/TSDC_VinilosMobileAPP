package com.uniandes.vinilos.database.dao

import androidx.room.*
import com.uniandes.vinilos.database.entities.PerformerEntity

@Dao
interface PerformerDao {
    @Query("SELECT * FROM performers")
    suspend fun getAll(): List<PerformerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(performers: List<PerformerEntity>)

    @Query("DELETE FROM performers")
    suspend fun deleteAll()
}
