package no.uio.ifi.in2000.vrapp.ui.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.WeatherApplication
import no.uio.ifi.in2000.vrapp.data.api.sunrise.MoonPhaseDataSource
import no.uio.ifi.in2000.vrapp.data.api.sunrise.MoonPhaseRepository
import no.uio.ifi.in2000.vrapp.data.api.sunset.SunriseSunsetDataSource
import no.uio.ifi.in2000.vrapp.data.api.sunset.SunriseSunsetRepository
import no.uio.ifi.in2000.vrapp.data.api.weather.WeatherRepository
import no.uio.ifi.in2000.vrapp.data.location.GeoSearch
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.WeatherInfo
import no.uio.ifi.in2000.vrapp.ui.components.isFrameDark
import no.uio.ifi.in2000.vrapp.ui.favorites.FavoritesStateHolder

// States of weather data loading
sealed interface WeatherUiState {
    data class Success(val weatherInfo: WeatherInfo) : WeatherUiState
    data object Error : WeatherUiState
    data object Loading : WeatherUiState
}

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(
    private val weatherRepository: WeatherRepository,
    private val favoritesStateHolder: FavoritesStateHolder,
    private val sunriseSunsetRepository: SunriseSunsetRepository,
    private val moonRepository: MoonPhaseRepository
) : ViewModel() {

    // Default location (Blindern, Oslo) as fallback
    private val blindernLocation = Location("Blindern, Oslo", 59.94063f, 10.72307f)

    // Weather UI state management
    private val _weatherUiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherUiState: StateFlow<WeatherUiState> = _weatherUiState

    // Search-related state flows (delegated to GeoSearch)
    val searchQuery: StateFlow<String> = GeoSearch.searchQuery
    val searchResults: StateFlow<List<Location>> = GeoSearch.searchResults
    val isSearching: StateFlow<Boolean> = GeoSearch.isSearching
    val showSearchResults: StateFlow<Boolean> = GeoSearch.showSearchResults
    val selectedLocation: StateFlow<Location?> = GeoSearch.selectedLocation

    // Sunrise/sunset state
    private val _sunrise = MutableStateFlow<String?>(null)
    val sunrise: StateFlow<String?> = _sunrise
    private val _sunset = MutableStateFlow<String?>(null)
    val sunset: StateFlow<String?> = _sunset

    // Moon phase state
    private val _moonPhasePercent = MutableStateFlow<Int?>(null)
    val moonPhasePercent: StateFlow<Int?> = _moonPhasePercent

    // Dark mode state (based on time of day)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    // Favorites data & state
    val favoritesWithWeather = favoritesStateHolder.favoritesWithWeather
    val isFavorite: StateFlow<Boolean> = selectedLocation
        .combine(favoritesWithWeather) { location, favoritesMap ->
            location != null && favoritesMap.keys.any { it.name == location.name }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)


    // Rain status state
    private val _isRaining = MutableStateFlow(false)
    val isRaining: StateFlow<Boolean> = _isRaining


    init {
        // Try to initialize last favorite else fallback to Blindern
        viewModelScope.launch(Dispatchers.IO) {
            favoritesStateHolder.favoritesWithWeather.firstOrNull()?.let { favoritesMap ->
                val selected = GeoSearch.selectedLocation.value
                val query = GeoSearch.searchQuery.value
                val results = GeoSearch.searchResults.value

                if (selected == null && query.isBlank() && results.isEmpty()) {
                    if (favoritesMap.isNotEmpty()) {
                        GeoSearch.selectSearchResult(favoritesMap.keys.last()) // Last favorite
                    } else {
                        GeoSearch.selectSearchResult(blindernLocation) // Fallback
                    }
                }
            }
        }
        fetchWeatherData() // Initial weather data
        fetchMoonPhase() // Initial moon phase data

    }

    // Fetches weather data for current location
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherData() {
        viewModelScope.launch(Dispatchers.IO) {
            _weatherUiState.value = WeatherUiState.Loading
            try {
                val location = selectedLocation.value ?: blindernLocation
                val weatherData =
                    weatherRepository.getWeatherData(location)?.copy(location = location)
                Log.d("WeatherDebug", "Selected location: ${selectedLocation.value?.displayName}")

                weatherData?.let {
                    updateRainStatus(it) // Update rain status
                }
                _weatherUiState.value = if (weatherData != null) {
                    WeatherUiState.Success(weatherInfo = weatherData)
                } else {
                    WeatherUiState.Error
                }
            } catch (_: Exception) {
                _weatherUiState.value = WeatherUiState.Error
            }
        }
    }

    // Searches for locations based on query
    fun searchLocation(query: String) {
        GeoSearch.searchLocation(query, viewModelScope)
    }

    // Sets the selected location from search results
    fun selectSearchResult(location: Location?) {
        GeoSearch.selectSearchResult(location)
    }

    // Toggles favorite status of current location
    fun toggleFavorite() {
        selectedLocation.value?.let { location ->
            viewModelScope.launch {
                favoritesStateHolder.toggleFavorite(location)
            }
        }
    }

    // Updates rain status based on current weather data
    private fun updateRainStatus(weatherInfo: WeatherInfo) {
        val isCurrentlyRaining =
            weatherInfo.properties.timeSeries.firstOrNull()?.let { timeSeries ->
                val precipitationAmount = timeSeries.data.next1Hour?.details?.precipitation ?: 0.0
                val precipitationProb = timeSeries.data.next1Hour?.details?.precipProbability ?: 0.0


                precipitationAmount > 0.1 || precipitationProb > 30.0
            } == true

        _isRaining.value = isCurrentlyRaining
    }

    // Updates dark mode based on sunrise/sunset times
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateDarkMode(sunrise: String?, sunset: String?) {
        _isDarkMode.value = isFrameDark(sunrise, sunset)
    }

    // Fetches sunrise/sunset times for current location
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchSunTimes() {
        viewModelScope.launch {
            try {
                val location = selectedLocation.value ?: blindernLocation
                val times = sunriseSunsetRepository.getSunTimes(location)
                _sunrise.value = times.first
                _sunset.value = times.second
                updateDarkMode(times.first, times.second)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Feil ved henting av soltider", e)
            }
        }
    }

    // Fetches current moon phase percentage
    private fun fetchMoonPhase() {
        viewModelScope.launch {
            try {
                val moonPhase = moonRepository.getMoonPhasePercent()
                Log.d("MoonPhaseDebug", "Fetched moon phase: $moonPhase")
                _moonPhasePercent.value = moonPhase
            } catch (e: Exception) {
                Log.e("MoonPhaseError", "Error fetching moon phase", e)
            }
        }
    }


    companion object {
        // Factory for ViewModel instantiation with dependencies
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? WeatherApplication
                        ?: throw IllegalStateException("Application must be an instance of WeatherApplication")
                HomeViewModel(
                    weatherRepository = application.container.weatherRepository,
                    favoritesStateHolder = application.container.favoritesStateHolder,
                    sunriseSunsetRepository = SunriseSunsetRepository(SunriseSunsetDataSource()),
                    moonRepository = MoonPhaseRepository(MoonPhaseDataSource())
                )
            }
        }
    }
}
