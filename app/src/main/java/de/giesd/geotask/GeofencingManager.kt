package de.giesd.geotask

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.annotation.RequiresPermission
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

object GeofencingManager {

    private const val requestIdPrefix = "GeoTask-"
    private val geofencingClient by lazy {
        LocationServices.getGeofencingClient(GeoTaskApplication.appContext)
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorGeofences(areas: Iterable<Area>) {
        areas.forEach { monitorGeofence(it) }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorGeofence(area: Area) {
        val geofence = getGeofence(area)
        val request = getRequest(geofence)
        geofencingClient.addGeofences(request, getGeofencePendingIntent(area.id))
        Log.i("GeofencingManager", "monitoring " + area.name)
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorAllActive(context: Context) {
        val dao = AreaDatabase.getInstance().areaDao()
        val activeAreas = dao.getAllActive()
        activeAreas.forEach { area ->
            monitorGeofence(area)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun updateAll(areas: Iterable<Area>) {
        val activeAreas = areas.filter { it.active }
        unmonitorAll()?.addOnCompleteListener { monitorGeofences(activeAreas) }
    }

    fun unmonitor(area: Area): Task<Void> {
        Log.i("GeofencingManager", "unmonitoring " + area.name)
        return geofencingClient.removeGeofences(listOf(getRequestId(area)))
    }

    fun unmonitor(areas: Iterable<Area>): Task<Void> {
        return geofencingClient.removeGeofences(areas.map { getRequestId(it) })
    }

    private fun unmonitorAll(): Task<Void>? {
        return geofencingClient.removeGeofences(getGeofencePendingIntent(0))
    }

    private fun getGeofence(area: Area): Geofence {
        return Geofence.Builder()
            .setRequestId(requestIdPrefix + area.id)
            .setCircularRegion(area.latitude, area.longitude, area.radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setRequestId(getRequestId(area))
            .build()
    }

    private fun getGeofences(areas: List<Area>): Iterable<Geofence> =
        areas.map { getGeofence(it) }

    private fun getRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    private fun getRequest(geofences: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
    }

    private fun getGeofencePendingIntent(id: Int): PendingIntent {
        val intent = Intent(GeoTaskApplication.appContext, GeofenceTransitionIntentService::class.java)
        intent.putExtra(EXTRA_AREA_ID, id)
        return PendingIntent.getService(GeoTaskApplication.appContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getRequestId(area: Area): String {
        return requestIdPrefix + area.id
    }

}