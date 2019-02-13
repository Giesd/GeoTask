package de.giesd.geotask

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionIntentService : IntentService("GeofenceTransitionIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.i("GeofenceIntentService", "onHandleIntent")
        if (geofencingEvent != null && !geofencingEvent.hasError()) {
            val id = intent?.getIntExtra(EXTRA_AREA_ID, 0) ?: 0
            if (id != 0) {
                val insideArea =
                    geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
                Log.i("GeofenceIntentService", "onHandleIntent - id = " + id)
                Log.i("GeofenceIntentService", "onHandleIntent - inside = " + insideArea)
                setAreaInside(id, insideArea)
                notifyTasker()
            }
        }
    }

    private fun setAreaInside(id: Int, inside: Boolean) {
        val dao = AreaDatabase.getInstance().areaDao()
        dao.setInside(id, inside)
        // TODO: run in background
        Log.i("GeofenceIntentService", "setAreaInside")
    }

    // TODO: auch beim Deaktivieren einer Area Tasker benachrichtigen (?)
    private fun notifyTasker() {
        sendBroadcast(taskerRequestQueryIntent)
    }

}