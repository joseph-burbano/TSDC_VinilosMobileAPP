package com.uniandes.vinilos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.entities.AlbumEntity

@Database(entities = [AlbumEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class VinilosDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile
        private var INSTANCE: VinilosDatabase? = null

        fun getDatabase(context: Context): VinilosDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VinilosDatabase::class.java,
                    "vinilos_database"
                ).build().also { INSTANCE = it }
            }
    }
}
