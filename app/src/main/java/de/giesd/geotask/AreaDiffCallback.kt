package de.giesd.geotask

import android.support.v7.util.DiffUtil

class AreaDiffCallback(private val oldAreas: List<Area>, private val newAreas: List<Area>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldAreas.size

    override fun getNewListSize(): Int = newAreas.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldAreas[oldItemPosition].id == newAreas[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldArea = oldAreas[oldItemPosition]
        val newArea = newAreas[newItemPosition]
        return oldArea.name == newArea.name && oldArea.active == newArea.active &&
                oldArea.latitude == newArea.latitude && oldArea.longitude == newArea.longitude &&
                oldArea.description == newArea.description &&
                oldArea.inside == newArea.inside
    }
}