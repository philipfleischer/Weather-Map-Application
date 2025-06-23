package no.uio.ifi.in2000.vrapp.data.api.geoSearch

//Repository for handling location search operations
class GeoSearchRepository(private val geoSource: GeoSearchDataSource) {
    // Fetches searchLocation from data source
    suspend fun searchLocations(query: String, lat: Double?, lng: Double?) =
        geoSource.searchLocations(query, lat, lng)
}