package no.uio.ifi.in2000.vrapp.data.api.sunrise

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
import java.text.SimpleDateFormat
import java.util.*

//Dataclass for MoonApiResponse
@Serializable
data class MoonApiResponse(
    val copyright: String? = null,
    val license: String? = null,
    val properties: Properties
)

//Dataclass for properties i MoonApiResponse
@Serializable
data class Properties(
    val moonphase: Double
)

//DataSource for calling moon api and getting the moonphase using ktor
class MoonPhaseDataSource {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    // Calls moon api and catches and decodes response, offset is set to +02:00 to make zulu time CEST
    suspend fun getMoonPhase(): Double? {
        val blindernLat = 59.94063
        val blindernLon = 10.72307
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val url = "https://in2000.api.met.no/weatherapi/sunrise/3.0/moon?lat=$blindernLat&lon=$blindernLon&date=$date&offset=+02:00"

        return try {
            val response = client.get(url) {
                header(HttpHeaders.UserAgent, "IN2000-Team28 linnthor@uio.no")
            }
            val moonApiResponse = json.decodeFromString<MoonApiResponse>(response.bodyAsText())
            moonApiResponse.properties.moonphase
        } catch (e: Exception) {
            Log.e("MoonPhase", "API error: ${e.message}", e)
            null
        }
    }
}

