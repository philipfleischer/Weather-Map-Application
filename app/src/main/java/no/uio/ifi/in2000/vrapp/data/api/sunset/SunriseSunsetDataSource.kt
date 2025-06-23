package no.uio.ifi.in2000.vrapp.data.api.sunset

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.vrapp.domain.models.Location
import java.text.SimpleDateFormat
import java.util.*

//Data class for decoding api responce
@Serializable
data class SunriseSunsetResponse(
    val properties: Properties
) {
    @Serializable
    data class Properties(
        val sunrise: SunEvent?,
        val sunset: SunEvent?
    ) {
        @Serializable
        data class SunEvent(
            val time: String
        )
    }
}

//DataSource for getting sunrise and sunset from sunrise api using ktor
class SunriseSunsetDataSource {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
    }

    //Calls sun api and formats responce. offset is set to +02:00 to get CEST time
    suspend fun getSunTimes(location: Location): Pair<String?, String?> {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val url = "https://in2000.api.met.no/weatherapi/sunrise/3.0/sun?" +
                "lat=${location.latitude}&lon=${location.longitude}&date=$date&offset=+02:00"

        return try {
            val response = client.get(url) {
                header(HttpHeaders.UserAgent, "IN2000-Team28 linnthor@uio.no")
            }
            val data = json.decodeFromString<SunriseSunsetResponse>(response.bodyAsText())
            Log.d("SunriseSunset", "API Response - Sunrise: ${data.properties.sunrise?.time}, Sunset: ${data.properties.sunset?.time}")
            Pair(data.properties.sunrise?.time, data.properties.sunset?.time)
        } catch (e: Exception) {
            Log.d("SunriseSunset", "API Error: ${e.message}")
            Pair(null, null)
        }
    }
}