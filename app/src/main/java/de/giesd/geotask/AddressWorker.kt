package de.giesd.geotask

import android.content.Context
import android.location.Geocoder
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.IOException

class AddressWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        const val PARAM_ID = "param:id"
    }

    override fun doWork(): Result {
        val dao = AreaDatabase.getInstance().areaDao()
        val id = inputData.getInt(PARAM_ID, 0)
        if (id == 0) {
            return Result.failure()
        }
        val area = dao.getById(id)
        if (area != null) {
            val geocoder = Geocoder(applicationContext)
            return try {
                val addresses = geocoder.getFromLocation(area.latitude, area.longitude, 1)
                if (addresses.isEmpty()) {
                    Result.failure()
                } else {
                    dao.updateDescription(id, addresses[0].getAddressLine(0))
                    Result.success()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Result.retry()
            }
        }
        return Result.failure()
    }
}