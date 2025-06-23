package no.uio.ifi.in2000.vrapp.data.api.map

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import no.uio.ifi.in2000.vrapp.domain.models.IntermediaryTimedLinks
import no.uio.ifi.in2000.vrapp.domain.models.TimedLinks

//DataSource for fetching map related weather api metadata
class MapDataSource {
    private val serverIp = "10.0.2.2" //If you want to test with a physical device or an emulator ran on a device
    //that isn't the device running the server code you have to change this to the ip-address of the server-device.
    
    private val customJson = Json { ignoreUnknownKeys = true }

    //ktor client
    private val ktorHttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(customJson)
        }
    }
    
    //map style from maptiler (basic-v2)
    fun getMapStyle(): String {
        return "https://api.maptiler.com/maps/basic-v2/style.json?key=k8vCjkZ0eGgIOUk0hVRU"
    }

    suspend fun getAvailableAPI(currentApi: String): List<TimedLinks>{
        try {
            val response = ktorHttpClient.get("${currentApi}available.json") {
                header(HttpHeaders.UserAgent, "IN2000-Team28 davidhov@uio.no")
            }
            val data: String = response.bodyAsText()
            val jsonElement: JsonElement = customJson.parseToJsonElement(data)
            val jsonArray: JsonArray = jsonElement.jsonObject["times"]!!.jsonArray
            val preTimesList: List<IntermediaryTimedLinks> = customJson.decodeFromJsonElement(jsonArray)
            val timesList: List<TimedLinks> = preTimesList.map{
                TimedLinks(it.time, "http://${serverIp}:8080/api-point/${it.tiles["png"]!!}")
            }
            return timesList
        } catch (e: Exception) {
            return listOf(TimedLinks("null", "null"))
        }
    }
}
