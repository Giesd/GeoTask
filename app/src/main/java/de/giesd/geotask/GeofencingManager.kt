package de.giesd.geotask

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.annotation.RequiresPermission
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

object GeofencingManager {

    private const val requestIdPrefix = "GeoTask-"

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorGeofences(context: Context, areas: Iterable<Area>) {
        val client = LocationServices.getGeofencingClient(context.applicationContext)
        areas.forEach { monitorGeofence(context, it, client) }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorGeofence(context: Context, area: Area, geofencingClient: GeofencingClient? = null) {
        val geofence = getGeofence(area)
        val request = getRequest(geofence)
        val client = geofencingClient ?: LocationServices.getGeofencingClient(context.applicationContext)
        client.addGeofences(request, getGeofencePendingIntent(context, area.id))
        Log.i("GeofencingManager", "monitoring " + area.name)
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun monitorAllActive(context: Context) {
        val dao = AreaDatabase.getInstance(context).areaDao()
        val activeAreas = dao.getAllActive()
        val client = LocationServices.getGeofencingClient(context)
        activeAreas.forEach { area ->
            monitorGeofence(context, area, client)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun updateAll(context: Context, areas: Iterable<Area>) {
        val activeAreas = areas.filter { it.active }
        unmonitorAll(context)?.addOnCompleteListener { monitorGeofences(context, activeAreas) }
    }

    fun unmonitor(context: Context, area: Area): Task<Void> {
        Log.i("GeofencingManager", "unmonitoring " + area.name)
        val client = LocationServices.getGeofencingClient(context)
        return client.removeGeofences(listOf(getRequestId(area)))
    }

    fun unmonitor(context: Context, areas: Iterable<Area>): Task<Void> {
        val client =  LocationServices.getGeofencingClient(context)
        return client.removeGeofences(areas.map { getRequestId(it) })
    }

    private fun unmonitorAll(context: Context): Task<Void>? {
        val client = LocationServices.getGeofencingClient(context)
        return client.removeGeofences(getGeofencePendingIntent(context, 0))
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

    private fun getGeofencePendingIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, GeofenceTransitionIntentService::class.java)
        intent.putExtra(EXTRA_AREA_ID, id)
        return PendingIntent.getService(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getRequestId(area: Area): String {
        return requestIdPrefix + area.id
    }

}