package no.uio.ifi.in2000.vrapp.data.api.weather

import android.os.Build
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.WeatherInfo
import java.time.ZoneId
import java.time.ZonedDateTime

//Repository for WeatherDataSource, fetches and caches api response
class WeatherRepository(private val dataSource: WeatherDataSource) {
    private val cache = mutableMapOf<Location, WeatherInfo>()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getWeatherData(location: Location): WeatherInfo? {
        return try {
            return cache[location] ?: fetchWeatherData(location)
        } catch (e: Exception) {
            null
        }
    }

    //Converts zulu time to CEST time
    @RequiresApi(Build.VERSION_CODES.O)
    fun zuluToCEST(zulu: String): ZonedDateTime {
        val zuluTime = ZonedDateTime.parse(zulu)
        return zuluTime.withZoneSameInstant(ZoneId.of("Europe/Oslo"))
    }

    //Fetches and caches Weather data response from LocationForecast
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchWeatherData(location: Location): WeatherInfo? {
        val data = dataSource.getLocationForecast(location)
        if (data != null) {
            //converts timeseries.time fra zulu to CEST
            data.properties.timeSeries.forEach { time ->
                time.time = zuluToCEST(time.time).toString()
            }
            cache[location] = data
        }
        return data
    }

}