package no.uio.ifi.in2000.vrapp.data.api.geoSearch

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.vrapp.domain.models.Location
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

//Datasource for location searches calling two different apis from Kartverket using ktor
class GeoSearchDataSource (
    //ktor client
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) {
    // Searches location based on query length
    suspend fun searchLocations(
        query: String,
        lat: Double? = null,
        lng: Double? = null
    ): List<Location> = withContext(Dispatchers.IO) {
        // if query is empty, return empty list, only if lat && lng isnt null, then call searchClosestLocations
        if (query.isEmpty() && lat != null && lng != null) {
            return@withContext searchClosestLocations(lat, lng)
        } else if (query.isEmpty()) {
            return@withContext emptyList()
        }
        val encodedQuery = URLEncoder.encode(query, "UTF-8")    //encoding string so it can be used in a Url
        val url = URL("https://api.kartverket.no/stedsnavn/v1/navn?sok=$encodedQuery&fuzzy=true&utkoordsys=4258&treffPerSide=10")

        //Passes response on to parseKartverketResponse
        try {
            val response = client.get(url) {
                header(HttpHeaders.Accept, "application/json")
            }
            parseKartverketResponse(response.bodyAsText())
        } catch (e: Exception) {
            emptyList()
        }

    }

    // Searches location based on location (Lat && Lng). Response dont have kommunenavn or fylkenavn
    private suspend fun searchClosestLocations(lat: Double, lng: Double): List<Location> {
        val url = URL("https://api.kartverket.no/stedsnavn/v1/punkt?nord=$lat&ost=$lng&koordsys=4258&radius=1000&utkoordsys=4258&treffPerSide=10&side=1")

        //Passes response on to parseKartverketResponse
        try {
            val response = client.get(url) {
                header(HttpHeaders.Accept, "application/json")
            }
            return parseKartverketResponse(response.bodyAsText())
        } catch (e: Exception) {
            return emptyList()
        }
    }

    //Parses kartverket response from both apis(slightly diffrent responses),
    //then makes a Location object of each response and adding it to locations list
    private fun parseKartverketResponse(jsonResponse: String): List<Location> {
        val locations = mutableListOf<Location>()

        try {
            val jsonObject = JSONObject(jsonResponse)
            val navnArray = jsonObject.optJSONArray("navn") ?: return emptyList()

            for (i in 0 until navnArray.length()) {
                val stedsnavnObj = navnArray.optJSONObject(i) ?: continue

                var name = ""
                val stedsnavnArray = stedsnavnObj.optJSONArray("stedsnavn")
                if (stedsnavnArray != null) {
                    for (j in 0 until stedsnavnArray.length()) {
                        val navnObj = stedsnavnArray.optJSONObject(j)
                        if (navnObj?.optString("navnestatus") == "hovednavn") {
                            name = navnObj.optString("skrivemåte", "")
                            break
                        }
                    }
                } else {
                    name = stedsnavnObj.optString("stedsnavn", "").takeIf { it.isNotEmpty() }
                        ?: stedsnavnObj.optString("skrivemåte", "")
                }

                if (name.isEmpty()) continue

                //collects Lat Lng from response
                val pos = stedsnavnObj.optJSONObject("representasjonspunkt") ?: continue
                val latitude = pos.optDouble("nord", Double.NaN)
                val longitude = pos.optDouble("øst", Double.NaN)
                if (latitude.isNaN() || longitude.isNaN()) continue

                //Defines kommune and fylke if not null
                val kommune = stedsnavnObj.optJSONArray("kommuner")
                    ?.optJSONObject(0)
                    ?.optString("kommunenavn", "")
                    .orEmpty()

                val fylke = stedsnavnObj.optJSONArray("fylker")
                    ?.optJSONObject(0)
                    ?.optString("fylkesnavn", "")
                    .orEmpty()

                //Combines name(stedsnavn or skrivemåte), kommunenavn and fylkenavn to fullName
                val fullName = buildString {
                    append(name)
                    if (kommune.isNotEmpty()) append(", $kommune")
                    if (fylke.isNotEmpty() && fylke != kommune) append(", $fylke")
                }

                locations.add(Location(fullName, latitude.toFloat(), longitude.toFloat()))
            }
        } catch (e: Exception) {
            Log.e("GeocodingService", "Error parsing response: ${e.message}")
        }

        return locations
    }
}