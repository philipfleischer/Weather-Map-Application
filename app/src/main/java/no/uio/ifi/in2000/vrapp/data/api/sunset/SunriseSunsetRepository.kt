package no.uio.ifi.in2000.vrapp.data.api.sunset

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.vrapp.domain.models.Location
import java.text.SimpleDateFormat
import java.util.*

//Repositroy for SunRiseSunSet
class SunriseSunsetRepository(private val dataSource: SunriseSunsetDataSource) {
    private var sunrise: String? = null
    private var sunset: String? = null
    private var lastFetchDate: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSunTimes(location: Location): Pair<String?, String?> {
        val currentDate = getCurrentDate()

        // Fetch data if not already fetched for today
        if (lastFetchDate != currentDate || sunrise == null || sunset == null) {
            Log.d("SunriseRepo", "Fetching new sun times for date: $currentDate")
            val (newSunrise, newSunset) = dataSource.getSunTimes(location)
            sunrise = newSunrise
            sunset = newSunset
            Log.d("SunriseRepo", "sunriseCEST:$sunrise,   SunsetCEST:$sunset")
            lastFetchDate = currentDate
        }

        return Pair(sunrise, sunset)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}