package no.uio.ifi.in2000.vrapp.data.location

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.data.api.geoSearch.GeoSearchDataSource
import no.uio.ifi.in2000.vrapp.data.api.geoSearch.GeoSearchRepository
import no.uio.ifi.in2000.vrapp.domain.models.Location
import org.maplibre.android.geometry.LatLng

//Manages Location and location search
object GeoSearch {
    // Repository for location searches
    private val repository = GeoSearchRepository(GeoSearchDataSource())

    //Location and location search related states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Location>>(emptyList())
    val searchResults: StateFlow<List<Location>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _showSearchResults = MutableStateFlow(false)
    val showSearchResults: StateFlow<Boolean> = _showSearchResults

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    //Searches location based on query
    fun searchLocation(query: String, scope: CoroutineScope, currentLocation: LatLng? = null) {
        _searchQuery.value = query

        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _showSearchResults.value = false
            return
        }

        // Api calls are done in a Dispatchers.IO thread
        scope.launch(Dispatchers.IO) {
            _isSearching.value = true
            //updates result and showresult state based on result
            try {
                val lat = currentLocation?.latitude
                val lng = currentLocation?.longitude
                _searchResults.value = repository.searchLocations(query, lat, lng)
                _showSearchResults.value = _searchResults.value.isNotEmpty()
            } catch (e: Exception) {
                Log.e("SearchService", "Error searching locations: ${e.message}")
                _searchResults.value = emptyList()
                _showSearchResults.value = false
            } finally {
                _isSearching.value = false
            }
        }
    }

    //Searches posistion based on LatLng location (used for "din posisjon" button)
    fun searchNearestLocation(scope: CoroutineScope, currentLocation: LatLng) {
        // Api calls are done in a Dispatchers.IO thread
        scope.launch(Dispatchers.IO) {
            _isSearching.value = true
            //updates result and showresult state based on result
            try {
                //query is set to empty, and gps LatLng are passed on
                _searchResults.value = repository.searchLocations("", currentLocation.latitude, currentLocation.longitude)
                _showSearchResults.value = _searchResults.value.isNotEmpty()
                if (_searchResults.value.isNotEmpty()) {
                    selectSearchResult(_searchResults.value.first())
                }
            } catch (e: Exception) {
                Log.e("SearchService", "Error searching nearest location: ${e.message}")
                _searchResults.value = emptyList()
                _showSearchResults.value = false
            } finally {
                _isSearching.value = false
            }
        }
    }

    //Updates selectedLocation and search states
    fun selectSearchResult(location: Location?) {
        _selectedLocation.value = location
        _searchQuery.value = location?.name ?: ""
        _showSearchResults.value = false
    }
}