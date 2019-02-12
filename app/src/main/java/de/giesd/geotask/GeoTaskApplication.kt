package de.giesd.geotask

import android.app.Application
import android.content.Context

class GeoTaskApplication : Application() {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

}