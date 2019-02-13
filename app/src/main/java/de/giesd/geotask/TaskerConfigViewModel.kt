package de.giesd.geotask

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

class TaskerConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val areaDao = AreaDatabase.getInstance().areaDao()
    val areas = areaDao.getAllAsync()
}