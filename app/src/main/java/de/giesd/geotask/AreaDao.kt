package de.giesd.geotask

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas ORDER BY LOWER(name) ASC")
    fun getAllAsync(): LiveData<List<Area>>

    @Query("SELECT * FROM areas WHERE active = 1")
    fun getAllActive(): List<Area>

    @Query("SELECT * FROM areas WHERE id = :id LIMIT 1")
    fun getById(id: Int): Area?

    @Query("SELECT * FROM areas WHERE id = :id LIMIT 1")
    fun getByIdAsync(id: Int): LiveData<Area>

    @Query("UPDATE areas SET active = :active WHERE id = :id")
    fun setActive(id: Int, active: Boolean)

    @Query("UPDATE areas SET active = (NOT active) WHERE id = :id")
    fun toggleState(id: Int)

    @Query("UPDATE areas SET description = :description WHERE id = :id")
    fun updateDescription(id: Int, description: String)

    @Query("UPDATE areas SET inside = :inside WHERE id = :id")
    fun setInside(id: Int, inside: Boolean)

    @Insert
    fun insert(area: Area): Long

    @Insert
    fun insert(areas: List<Area>): List<Long>

    @Update
    fun update(area: Area)

    @Delete
    fun delete(area: Area)

    @Query("DELETE FROM areas WHERE id IN (:ids)")
    fun deleteAllById(ids: List<Int>)

}