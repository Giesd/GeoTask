package de.giesd.geotask

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import kotlin.math.max
import kotlin.math.min

@Entity(tableName = "areas")
data class Area(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val active: Boolean,
    val inside: Boolean,
    val description: String) {

    companion object {
        const val MIN_RADIUS = 20f
        const val MAX_RADIUS = 1000f
        const val DEFAULT_RADIUS = 50f

        fun default() = Area(0, "", 0.0, 0.0, DEFAULT_RADIUS, false, false, "")
    }

    fun withName(name: String): Area =
            Area(id, name, latitude, longitude, radius, active, inside, description)

    fun withLatitude(latitude: Double): Area =
        Area(id, name, latitude, longitude, radius, active, inside, description)

    fun withLongitude(longitude: Double): Area =
        Area(id, name, latitude, longitude, radius, active, inside, description)

    fun withRadius(radius: Float): Area {
        val clampedRadius = max(MIN_RADIUS, min(MAX_RADIUS, radius))
        return Area(id, name, latitude, longitude, clampedRadius, active, inside, description)
    }

    fun withActive(active: Boolean): Area =
        Area(id, name, latitude, longitude, radius, active, inside, description)

    fun withInside(inside: Boolean): Area =
            Area(id, name, latitude, longitude, radius, active, inside, description)

    fun withDescription(description: String): Area =
        Area(id, name, latitude, longitude, radius, active, inside, description)

}