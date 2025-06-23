package no.uio.ifi.in2000.vrapp.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.maplibre.android.geometry.LatLng
import java.util.concurrent.TimeUnit

//Manages Gps location updates and permissions
class GPSManager (private val context: Context) {
    //FusedLocationProviderClient
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    //Location and permission states
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _permissionGranted = MutableStateFlow(hasLocationPermission())
    val permissionGranted: StateFlow<Boolean> = _permissionGranted

    private val _isLocationUpdatesActive = MutableStateFlow(false)
    val isLocationUpdatesActive: StateFlow<Boolean> = _isLocationUpdatesActive

    private var locationCallback: LocationCallback? = null

    //Checks if gps permissions are granted
    fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return finePermission || coarsePermission
    }

    //Updates permission status
    fun updatePermissionStatus() {
        _permissionGranted.value = hasLocationPermission()
    }

    //Starts locationupdates with priority based on permission
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            _isLocationUpdatesActive.value = false
            return
        }

        stopLocationUpdates()

        //prioritize Fine location if permission is granted, else balanced
        val priority = if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // requset location between min 5 and max 10 seconds
        val locationRequest = LocationRequest.Builder(priority, TimeUnit.SECONDS.toMillis(10))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()

        //Update current location state based on last known gps location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _currentLocation.value = LatLng(location.latitude, location.longitude)
                }
            }
        }

        //Location request looper and updates islocationupdatesactive state
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isLocationUpdatesActive.value = true
        } catch (e: SecurityException) {
            _isLocationUpdatesActive.value = false
        }

    }

    //Stops location updates
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        _isLocationUpdatesActive.value = false
    }
}