package no.uio.ifi.in2000.vrapp.data.api.sunrise

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

//Repository for MoonPhase, fetches moonPhase from DataSource and caches it
class MoonPhaseRepository(private val dataSource: MoonPhaseDataSource) {
    private var cachedPercent: Int? = null
    private var lastFetchDate: String? = null

    //Fetches moon phase from DataSource, and calculates the fraction of the moon that is illuminated
    suspend fun getMoonPhasePercent(): Int? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastFetchDate != today || cachedPercent == null) {
            Log.d("MoonRepo", "Fetching new moon phase for $today")
            cachedPercent = dataSource.getMoonPhase()?.let { phase ->
                val radians = Math.toRadians(phase)
                val illumination = (1 - cos(radians)) / 2
                (illumination * 100).roundToInt()
            }
            lastFetchDate = today
        }
        return cachedPercent
    }
}
