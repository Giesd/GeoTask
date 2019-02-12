package de.giesd.geotask

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [(Area::class)], version = 1)
abstract class AreaDatabase : RoomDatabase() {

    abstract fun areaDao(): AreaDao

    companion object {
        private var instance: AreaDatabase? = null

        fun getInstance(context: Context): AreaDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext,
                    AreaDatabase::class.java, "area-database")
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }
}