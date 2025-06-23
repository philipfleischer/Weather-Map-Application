package no.uio.ifi.in2000.vrapp.ui.longterm

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.WeatherApplication
import no.uio.ifi.in2000.vrapp.data.location.GeoSearch
import no.uio.ifi.in2000.vrapp.data.api.longterm.LongTermWeatherRepository
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.LongTermWeatherInfo

sealed class LongTermWeatherUiState {
    object Loading : LongTermWeatherUiState()
    object Error : LongTermWeatherUiState()
    data class Success(val weatherInfo: LongTermWeatherInfo) : LongTermWeatherUiState()
}

@RequiresApi(Build.VERSION_CODES.O)
class LongTermForecastViewModel(
    private val longTermWeatherRepository: LongTermWeatherRepository
) : ViewModel() {

    private val blindernLocation = Location("Blindern, Oslo", 59.94063f, 10.72307f)
    private val _weatherUiState = MutableStateFlow<LongTermWeatherUiState>(LongTermWeatherUiState.Loading)
    val weatherUiState: StateFlow<LongTermWeatherUiState> = _weatherUiState

    // Search State (from GeoSearch)
    val searchQuery: StateFlow<String> = GeoSearch.searchQuery
    val searchResults: StateFlow<List<Location>> = GeoSearch.searchResults
    val isSearching: StateFlow<Boolean> = GeoSearch.isSearching
    val showSearchResults: StateFlow<Boolean> = GeoSearch.showSearchResults
    val selectedLocation: StateFlow<Location?> = GeoSearch.selectedLocation

    init {
        fetchWeatherData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherData() {
        viewModelScope.launch(Dispatchers.IO) {
            _weatherUiState.value = LongTermWeatherUiState.Loading
            try {
                val location = selectedLocation.value ?:  blindernLocation
                val weatherData = longTermWeatherRepository.getLongtermWeatherData(location)
                    ?.copy(location = location)

                _weatherUiState.value = if (weatherData != null) {
                    LongTermWeatherUiState.Success(weatherInfo = weatherData)
                } else {
                    LongTermWeatherUiState.Error
                }
            } catch (e: Exception) {
                Log.e("LongTermForecastViewModel", "Error: ${e.message}")
                _weatherUiState.value = LongTermWeatherUiState.Error
            }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WeatherApplication
                LongTermForecastViewModel(
                    longTermWeatherRepository = application.container.longTermWeatherRepository
                )
            }
        }
    }
}