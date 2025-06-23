package no.uio.ifi.in2000.vrapp.ui.favorites

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.uio.ifi.in2000.vrapp.data.api.weather.WeatherRepository
import no.uio.ifi.in2000.vrapp.data.favorite.FavoriteLocationRepository
import no.uio.ifi.in2000.vrapp.domain.models.FavoriteWeather
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.Properties
import no.uio.ifi.in2000.vrapp.domain.models.WeatherInfo
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class FavoritesStateHolder @Inject constructor(
    private val favoritesRepository: FavoriteLocationRepository,
    private val weatherRepository: WeatherRepository
) {
    private val _favoritesWithWeather = MutableStateFlow<Map<Location, FavoriteWeather>>(emptyMap())
    val favoritesWithWeather: StateFlow<Map<Location, FavoriteWeather>> = _favoritesWithWeather

    init {
        observeFavorites()
    }

    // Observes changes in favorite locations + updates weather data
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeFavorites() {
        favoritesRepository.favorites
            .onEach { locations ->
                _favoritesWithWeather.value = buildFavoriteWeatherMap(locations)
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }

    // Toggles a location's favorite status
    suspend fun toggleFavorite(location: Location) {
        favoritesRepository.toggleFavorite(location)
    }

    // Builds a map of locations to their weather data
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun buildFavoriteWeatherMap(locations: List<Location>): Map<Location, FavoriteWeather> {
        return locations.associateWith { location ->
            fetchWeather(location)
        }
    }

    // Fetches weather for specific location
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchWeather(location: Location): FavoriteWeather {
        return try {
            weatherRepository.getWeatherData(location)?.let { weather ->
                val todayTemps = weather.properties.timeSeries
                    .filter { it.time.startsWith(weather.getCurrentDate()) }
                    .map { it.data.instant.details.temperature }

                FavoriteWeather(
                    weatherInfo = weather,
                    maxTemp = todayTemps.maxOrNull()?.toInt()?.toString() ?: "--",
                    minTemp = todayTemps.minOrNull()?.toInt()?.toString() ?: "--"
                )
            } ?: createDefaultFavoriteWeather()
        } catch (_: Exception) {
            createDefaultFavoriteWeather()
        }
    }

    // Creates default weather data when real data isn't available
    private fun createDefaultFavoriteWeather(): FavoriteWeather {
        return FavoriteWeather(
            weatherInfo = WeatherInfo(Properties(emptyList())),
            maxTemp = "--",
            minTemp = "--"
        )
    }
}