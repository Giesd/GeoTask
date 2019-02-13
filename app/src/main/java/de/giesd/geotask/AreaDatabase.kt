package de.giesd.geotask

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase

@Database(entities = [(Area::class)], version = 1)
abstract class AreaDatabase : RoomDatabase() {

    abstract fun areaDao(): AreaDao

    companion object {
        private var instance: AreaDatabase? = null

        fun getInstance(): AreaDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(GeoTaskApplication.appContext,
                    AreaDatabase::class.java, "area-database")
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }
}