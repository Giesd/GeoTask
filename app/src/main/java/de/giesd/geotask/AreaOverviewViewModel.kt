package de.giesd.geotask

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.support.annotation.RequiresPermission

class AreaOverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val areaDao = AreaDatabase.getInstance().areaDao()
    val areas = areaDao.getAllAsync()
    private val _lastDeleted: MutableLiveData<List<Area>> = MutableLiveData()
    val lastDeleted: LiveData<List<Area>>
        get() = _lastDeleted

    @Deprecated("This method is deprecated.")
    fun toggleAreaActive(id: Int) {
        ToggleAsyncTask(areaDao, id).execute()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun toggleAreaActive(area: Area) {
        val toggledArea = area.withActive(!area.active)
        UpdateAsyncTask(areaDao, toggledArea).execute()
        if (toggledArea.active) {
            GeofencingManager.monitorGeofence(toggledArea)
        } else {
            GeofencingManager.unmonitor(toggledArea)
        }
    }

    fun deleteById(ids: Collection<Int>) {
        delete(areas.value?.filter { ids.contains(it.id) } ?: emptyList())
    }

    fun delete(areas: Collection<Area>) {
        _lastDeleted.value = areas.toList()
        val ids = areas.map { it.id }
        DeleteAsyncTask(areaDao, ids).execute()
        GeofencingManager.unmonitor(areas.filter { it.active })
    }

    fun undelete() {
        _lastDeleted.value?.let { deleted ->
            if (deleted.isNotEmpty()) {
                InsertAsyncTask(areaDao, deleted).execute()
                _lastDeleted.value = emptyList()
                GeofencingManager.unmonitor(deleted.filter { it.active })
            }
        }
    }

    private class ToggleAsyncTask(private val areaDao: AreaDao, private val id: Int) :
        AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            areaDao.toggleState(id)
            return null
        }
    }

    private class DeleteAsyncTask(private val areaDao: AreaDao, private val ids: List<Int>) :
            AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            areaDao.deleteAllById(ids)
            return null
        }
    }

    private class InsertAsyncTask(private val areaDao: AreaDao, private val areas: List<Area>) :
        AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            areaDao.insert(areas)
            return null
        }
    }

    private class UpdateAsyncTask(private val areaDao: AreaDao, private val area: Area) :
            AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            areaDao.update(area)
            return null
        }
    }
}