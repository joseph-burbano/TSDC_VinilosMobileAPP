package com.uniandes.vinilos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.dao.PerformerDao
import com.uniandes.vinilos.database.entities.AlbumEntity
import com.uniandes.vinilos.database.entities.PerformerEntity

@Database(entities = [AlbumEntity::class, PerformerEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class VinilosDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun performerDao(): PerformerDao

    companion object {
        @Volatile
        private var INSTANCE: VinilosDatabase? = null

        fun getDatabase(context: Context): VinilosDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VinilosDatabase::class.java,
                    "vinilos_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE performers ADD COLUMN albums TEXT NOT NULL DEFAULT '[]'"
        )
    }
}
