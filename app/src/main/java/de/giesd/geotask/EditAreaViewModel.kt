package de.giesd.geotask

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import androidx.work.*
import com.google.android.gms.maps.model.LatLng

class EditAreaViewModel(application: Application, id: Int) : AndroidViewModel(application) {

    companion object {
        private fun startAddressWorker(id: Int) {
            val data = Data.Builder().putInt(AddressWorker.PARAM_ID, id).build()
            val netConstraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val addressWorker = OneTimeWorkRequest.Builder(AddressWorker::class.java)
                .setInputData(data)
                .setConstraints(netConstraint)
                .build()
            WorkManager.getInstance().enqueue(addressWorker)
        }
    }

    private val areaDao = AreaDatabase.getInstance(application).areaDao()
    private val _area = MediatorLiveData<Area>()
    val area: LiveData<Area>
        get() = _area
    var isCenterSet = false
        private set

    init {
        if (id != 0) {
            _area.addSource(areaDao.getByIdAsync(id)) { _area.value = it }
            isCenterSet = true
        } else {
            _area.value = Area.default()
        }
    }

    fun setName(name: String) {
        if (name != _area.value?.name) {
            _area.value = _area.value?.withName(name)
        }
    }

    fun setRadius(radius: Float) {
        if (radius != _area.value?.radius) {
            _area.value = _area.value?.withRadius(radius)
        }
    }

    fun setCenter(center: LatLng) {
        if (_area.value?.latitude != center.latitude || _area.value?.longitude != center.longitude) {
            _area.value =_area.value?.withLatitude(center.latitude)?.withLongitude(center.longitude)
            isCenterSet = true
        }
    }

    fun saveArea() {
        val area = _area.value
        if (area != null) {
            InsertOrUpdateAsyncTask(areaDao).execute(area)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorGeofence(context: Context) {
        val area = _area.value
        if (area != null) {
            GeofencingManager.monitorGeofence(context, area)
        }
    }

    private class InsertOrUpdateAsyncTask(private val dao: AreaDao) : AsyncTask<Area, Void, Void>() {
        override fun doInBackground(vararg params: Area): Void? {
            params.forEach { area ->
                if (area.id == 0) {
                    val newId = dao.insert(area.withActive(true))
                    startAddressWorker(newId.toInt())
                } else {
                    dao.update(area)
                    startAddressWorker(area.id)
                }
            }
            return null
        }
    }

    class Factory(private val application: Application, private val id: Int) :
            ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EditAreaViewModel(application, id) as T
        }
    }
}