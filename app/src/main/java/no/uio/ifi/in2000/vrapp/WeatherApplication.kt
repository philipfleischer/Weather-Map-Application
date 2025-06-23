package no.uio.ifi.in2000.vrapp

import android.app.Application
import no.uio.ifi.in2000.vrapp.data.location.GPSManager
import no.uio.ifi.in2000.vrapp.dependencyinjection.AppContainer
import no.uio.ifi.in2000.vrapp.dependencyinjection.DefaultAppContainer

//Sets up the app container and GPS manager on app creation
class WeatherApplication : Application() {
    val gpsManager: GPSManager by lazy { GPSManager(this) }
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}