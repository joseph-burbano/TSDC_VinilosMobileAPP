package com.uniandes.vinilos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uniandes.vinilos.database.dao.PerformerDao
import com.uniandes.vinilos.database.entities.PerformerEntity

@Database(entities = [PerformerEntity::class], version = 1)
abstract class VinilosDatabase : RoomDatabase() {
    abstract fun performerDao(): PerformerDao

    companion object {
        @Volatile
        private var INSTANCE: VinilosDatabase? = null

        fun getDatabase(context: Context): VinilosDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VinilosDatabase::class.java,
                    "vinilos_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
