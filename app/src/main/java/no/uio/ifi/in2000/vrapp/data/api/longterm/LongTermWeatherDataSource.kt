package no.uio.ifi.in2000.vrapp.data.api.longterm

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.LongTermWeatherInfo

//DataSource for longTermWeatherForecast calling Met subseasonal api using ktor
class LongTermWeatherDataSource {
    private val customJson = Json { ignoreUnknownKeys = true }

    private val ktorHttpClient = HttpClient (CIO){
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val baseUrl: String =
        "https://in2000.api.met.no/weatherapi/subseasonal/1.0/complete?"

    //subseasonal gives a timeseries array with weatherdata for the next 21 days.
    //the data is structured in objects containting the forecast details for the next 24 hours, and a summary data for the next 7 days.
    //all hours are on UTC format which is 2 hours earlier than Oslo.
    //last element in timeseries lacks "symbol_code" since the given chunk goes beyond the reach of forecast
    //potential trimming of the last day if it is "very" incomplete
    //for symbol for the whole day consider using 12-hour symbol given at that days 6am(8am) prediction
    suspend fun getLongtermForecast(location: Location): LongTermWeatherInfo? {
        try {
            val response = ktorHttpClient.get(
                baseUrl + "lat=${location.latitude}&lon=${location.longitude}"
            ) {
                header(HttpHeaders.UserAgent, "IN2000-Team28 miaap@uio.no")
            }
            val data: String = response.bodyAsText()

            // Log the raw data received
            Log.d("LongTermWeatherDataSource", "Raw API Response: $data")

            return customJson.decodeFromString<LongTermWeatherInfo>(data)

        } catch (e: Exception) {
            Log.e("LongTermWeatherDataSource", "Failed to fetch data: ${e.localizedMessage}")
            return null
        }
    }
}


