package no.uio.ifi.in2000.vrapp.data.api.weather

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.WeatherInfo

//DataSource for Weather, with api call on locationforecast
class WeatherDataSource {
    private val customJson = Json { ignoreUnknownKeys = true }

    private val ktorHttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(customJson)
        }
    }

    private val baseUrl: String =
        "https://in2000.api.met.no/weatherapi/locationforecast/2.0/complete?"


    //locationforecast gives hourly data for 3 days, then 6hour chunks for remaining days.
    //all hours marks are on UTC format which is 2 hours earlier than Oslo time.
    //last element in timeseries lacks "symbol_code" since the given chunk goes beyond the reach of forecast
    //potential trimming of the last day if it is "very" incomplete
    //for symbol for the whole day consider using 12-hour symbol given at that days 6am(8am) prediction
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLocationForecast(location: Location): WeatherInfo? {
        try {
            val response = ktorHttpClient.get(
                baseUrl + "lat=${location.latitude}&lon=${location.longitude}"
            ) {
                header(HttpHeaders.UserAgent, "IN2000-Team28 davidhov@uio.no")
            }

            val data: String = response.bodyAsText()
            val jsonElement: JsonElement = customJson.parseToJsonElement(data)
            val weatherApiResponse: WeatherInfo = customJson.decodeFromJsonElement(jsonElement)

            // Map the API response to WeatherInfo
            return WeatherInfo(weatherApiResponse.properties)
        } catch (e: Exception) {
            Log.e("WeatherDataSource", "Failed to fetch data: ${e.localizedMessage}")
            return null
        }
    }
}
