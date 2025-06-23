package no.uio.ifi.in2000.vrapp.data.api.map

import no.uio.ifi.in2000.vrapp.domain.models.TimedLinks

//Repository for handling map operations
class MapRepository (
    private val mapDataSource: MapDataSource
) {
    //Fetches available weather api timed links from dataSource
    suspend fun getAvailableWeatherApis(apiUrl: String): List<TimedLinks> {
        return try {
            mapDataSource.getAvailableAPI(apiUrl)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Retrieves map style (map url)
    fun getMapStyle(): String {
        return mapDataSource.getMapStyle()
    }
}