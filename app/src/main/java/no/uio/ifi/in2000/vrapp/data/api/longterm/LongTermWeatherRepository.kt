package no.uio.ifi.in2000.vrapp.data.api.longterm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.LongTermWeatherInfo
import java.time.ZoneId
import java.time.ZonedDateTime

//Repository for fetching Weather data from LongTermWeatherDataSource
class LongTermWeatherRepository(private val dataSource: LongTermWeatherDataSource) {
    private val cache = mutableMapOf<Location, LongTermWeatherInfo>()

    //Checks if location is cached, if not fetches data from DataSource
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLongtermWeatherData(location: Location): LongTermWeatherInfo? {
        return try {
            cache[location] ?: fetchLongtermWeatherData(location)
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

    //Fetches data from dataSource
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchLongtermWeatherData(location: Location): LongTermWeatherInfo? {
        val data = dataSource.getLongtermForecast(location)
        if (data != null) {
            //converts timeseries.time fra zulu to CEST
            data.properties.timeseries.forEach { time ->
                time.time = zuluToCEST(time.time).toString()
            }
            cache[location] = data
            Log.e("LongtermDATA", "${data.properties.timeseries}" )
        }
        return data
    }
}
