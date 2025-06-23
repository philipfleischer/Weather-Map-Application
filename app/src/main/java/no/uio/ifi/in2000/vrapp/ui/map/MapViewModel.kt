package no.uio.ifi.in2000.vrapp.ui.map

import android.graphics.Color.HSVToColor
import android.graphics.Color.RGBToHSV
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.data.api.map.MapDataSource
import no.uio.ifi.in2000.vrapp.data.api.map.MapRepository
import no.uio.ifi.in2000.vrapp.data.location.GeoSearch
import no.uio.ifi.in2000.vrapp.domain.models.ApiLegend
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.MapWeatherApi
import no.uio.ifi.in2000.vrapp.domain.models.TimedLinks
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue
import java.time.ZoneId
import java.time.ZonedDateTime

//ViewModel for mapScreen (view)
@RequiresApi(Build.VERSION_CODES.O)
class MapViewModel(
    private val mapRepository: MapRepository = MapRepository(MapDataSource()),
) : ViewModel() {

    // Map style as a getter for map configuration
    val mapStyle: String
        get() = mapRepository.getMapStyle()

    //Search and location states from geoSearch
    val searchQuery: StateFlow<String> = GeoSearch.searchQuery
    val searchResults: StateFlow<List<Location>> = GeoSearch.searchResults
    val isSearching: StateFlow<Boolean> = GeoSearch.isSearching
    val showSearchResults: StateFlow<Boolean> = GeoSearch.showSearchResults
    val selectedLocation: StateFlow<Location?> = GeoSearch.selectedLocation

    //Camera positioin State for map, initialized to Blindern, Oslo at zoom 5
    var cameraPosition by mutableStateOf(
        CameraPosition.Builder()
            .target(LatLng(59.94063, 10.72307))
            .zoom(5.0)
            .build()
    )

    //Weather Api States and refresh job initialization
    var availableWeatherApis by mutableStateOf<List<MapWeatherApi>>(emptyList())
    var selectedWeatherApi by mutableStateOf<MapWeatherApi?>(null)
    var availableTimes by mutableStateOf<List<String>>(emptyList())
    var selectedTimeIndex by mutableIntStateOf(0)
    private var allTimedLinks = mutableMapOf<String, List<TimedLinks>>()
    var hueRotate by mutableFloatStateOf(0f)
    private var refreshJob: Job? = null

    //Animation state and job initialization
    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()
    private var animationJob: Job? = null

    //Defining weather api configuration using data class MapWeatherApi
    val weatherApiConfigs = listOf(
        MapWeatherApi(
            name = "Temperatur",
            baseUrl = "https://beta.yr-maps.met.no/api/air-temperature/",
            maxZoom = 5f,
            apiLayerProperties = arrayOf(
                //PropertyFactory.rasterOpacity(0.7f),
                //PropertyFactory.rasterContrast(0.7f),
                PropertyFactory.rasterHueRotate(hueRotate)
            )
        ),
        MapWeatherApi(
            name = "Skyer",
            baseUrl = "https://beta.yr-maps.met.no/api/cloud-area-fraction/",
            maxZoom = 5f,
            apiLayerProperties = arrayOf(
                //PropertyFactory.rasterOpacity(0.5f),
                PropertyFactory.rasterHueRotate(hueRotate)
            )
        ),
        MapWeatherApi(
            name = "Nedbør langtids",
            baseUrl = "https://beta.yr-maps.met.no/api/precipitation-amount/",
            maxZoom = 5f,
            apiLayerProperties = arrayOf(
                //PropertyFactory.rasterOpacity(0.5f),
                PropertyFactory.rasterHueRotate(hueRotate)
            )
        ),
        MapWeatherApi(
            name = "Nedbør radar", //Nowcast and Observation combined
            baseUrl = "https://beta.yr-maps.met.no/api/precipitation-nowcast/",
            maxZoom = 6f,
            apiLayerProperties = arrayOf(
                //PropertyFactory.rasterOpacity(0.5f),
                PropertyFactory.rasterHueRotate(hueRotate)
            )
        ),
        MapWeatherApi(
            name = "Vind",
            baseUrl = "https://beta.yr-maps.met.no/api/wind/",
            maxZoom = 6f,
            apiLayerProperties = arrayOf(
                //PropertyFactory.rasterOpacity(0.6f),
                //PropertyFactory.rasterContrast(0.6f),
                PropertyFactory.rasterHueRotate(hueRotate)
            )
        )
    )

    // Fetching available Weather apis and start refreshJob coroutine
    init {
        fetchAvailableWeatherApis()
        startApiRefresh()
    }

    //Starts/restarts a courentine on a separate thread that calls fetchAvailableWeatherApis() every 5 minutes
    @RequiresApi(Build.VERSION_CODES.O)
    fun startApiRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5 * 60 * 1000L)
                fetchAvailableWeatherApis()
            }
        }
    }

    //Starts a courentine on a seperate thread with Dispatchers.IO and fetches available weatherApis and caches their timed links
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAvailableWeatherApis() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newApis = mutableListOf<MapWeatherApi>()
                val cachedLinks = mutableMapOf<String, List<TimedLinks>>()

                for (config in weatherApiConfigs) {
                    when (config.name) {
                        //Seperate logic since nowcast and observation apis are combined into one and sorted
                        "Nedbør radar" -> {
                            val nowcastLinks =
                                mapRepository.getAvailableWeatherApis("https://beta.yr-maps.met.no/api/precipitation-nowcast/")
                            val observationsLinks =
                                mapRepository.getAvailableWeatherApis("https://beta.yr-maps.met.no/api/precipitation-observations/")

                            cachedLinks["precipitation-nowcast"] = nowcastLinks
                            cachedLinks["precipitation-observations"] = observationsLinks

                            val combinedLinks = (nowcastLinks + observationsLinks)
                                .filter { it.pngUrl != "null" }
                                .sortedBy { it.time }

                            //Add Api to NewApis/availableWeatherApi and maintaining hueRotation
                            if (combinedLinks.isNotEmpty()) {
                                newApis.add(
                                    config.copy(
                                        baseUrl = combinedLinks.first().pngUrl,
                                        apiLayerProperties = config.apiLayerProperties.map { prop ->
                                            when (prop.name) {
                                                "raster-hue-rotate" -> PropertyFactory.rasterHueRotate(
                                                    hueRotate
                                                )

                                                else -> prop
                                            }
                                        }.toTypedArray()
                                    )
                                )
                                cachedLinks[config.name] = combinedLinks
                            }
                        }

                        //Same logic, but without combining two apis
                        else -> {
                            val timedLinks = mapRepository.getAvailableWeatherApis(config.baseUrl)
                            cachedLinks[config.name] = timedLinks

                            val chosenLink = timedLinks.firstOrNull { it.pngUrl != "null" }
                            if (chosenLink != null) {
                                newApis.add(
                                    config.copy(
                                        baseUrl = chosenLink.pngUrl,
                                        apiLayerProperties = config.apiLayerProperties.map { prop ->
                                            when (prop.name) {
                                                "raster-hue-rotate" -> PropertyFactory.rasterHueRotate(
                                                    hueRotate
                                                )

                                                else -> prop
                                            }
                                        }.toTypedArray()
                                    )
                                )
                            }
                        }
                    }
                }

                allTimedLinks = cachedLinks
                availableWeatherApis = newApis

                //Maintain SelectedWeatherApi after fetchAvailableWeatherApis, if SelectedWeatherApi was null, then first one in newApis (temperature)
                val previousSelection = selectedWeatherApi?.name
                if (selectedWeatherApi == null && newApis.isNotEmpty()) {
                    selectedWeatherApi = newApis[0]
                    updateAvailableTimes()
                } else if (previousSelection != null) {
                    val refreshedApi = newApis.find { it.name == previousSelection }
                    if (refreshedApi != null) {
                        selectedWeatherApi = refreshedApi
                        updateAvailableTimes()
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching weather APIs: ${e.message}")
            }
        }
    }

    //Updates selectedWeatherApi, maintains huerotation and refreshes timedlinks by calling updateAvailableTimes()
    @RequiresApi(Build.VERSION_CODES.O)
    fun selectWeatherApi(apiName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedApi = availableWeatherApis.find { it.name == apiName }
                ?: availableWeatherApis.firstOrNull()
            selectedWeatherApi = selectedApi?.copy(
                apiLayerProperties = selectedApi.apiLayerProperties.map { prop ->
                    when (prop.name) {
                        "raster-hue-rotate" -> PropertyFactory.rasterHueRotate(hueRotate)
                        else -> prop
                    }
                }.toTypedArray()
            )
            updateAvailableTimes()
        }
    }

    //Searches through the cache of selectedWeatherApi availableTimes with a index
    fun updateSelectedTime(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTimeIndex =
                index.coerceIn(0, availableTimes.size - 1)  //Index is within a valid range
            if (availableTimes.isNotEmpty()) {
                val selectedTime = availableTimes[selectedTimeIndex]
                selectedWeatherApi?.let { api ->
                    //Find matching apiname in the hashmap, then the time in the related timedlinks, and updates selectedWeatherApi
                    val links = allTimedLinks[api.name] ?: emptyList()
                    val link = links.find { it.time == selectedTime }
                    if (link != null) {
                        selectedWeatherApi = api.copy(baseUrl = link.pngUrl)
                    }
                }
            }
        }
    }

    //Updates the list of available timedLinks only for selectedWeatherApi
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAvailableTimes() {
        viewModelScope.launch(Dispatchers.IO) {
            val api = selectedWeatherApi ?: return@launch

            when (api.name) {
                "Nedbør radar" -> {
                    //For nowcast & observations which use 5-minute intervals: combine and sort.
                    val links = allTimedLinks[api.name] ?: run {

                        val nowcastLinks = allTimedLinks["precipitation-nowcast"] ?: emptyList()
                        val observationsLinks = allTimedLinks["precipitation-observations"] ?: emptyList()

                        (nowcastLinks + observationsLinks).filter { it.pngUrl != "null" }
                            .sortedBy { it.time }
                    }

                    availableTimes = links.map { it.time }
                }

                else -> {
                    //For all other apis, hourly intervals
                    val links = allTimedLinks[api.name] ?: emptyList()
                    availableTimes = links.map { it.time }
                }
            }

            if (availableTimes.isNotEmpty()) {
                updateSelectedTime(0)
            }
        }
    }

    //Converts zulu time to CEST time
    fun zuluToCEST(zulu: String): ZonedDateTime {
        val zuluTime = ZonedDateTime.parse(zulu)
        return zuluTime.withZoneSameInstant(ZoneId.of("Europe/Oslo"))
    }

    //GeoSearch selectSearchResult, sets a search result as selectedLocation
    fun selectSearchResult(location: Location?) {
        GeoSearch.selectSearchResult(location)
    }

    //GeoSearch searchNearestLocation, for "din posisjon" button
    fun searchNearestLocation(currentLocation: LatLng) {
        GeoSearch.searchNearestLocation(viewModelScope, currentLocation)
    }

    //Copies and edits weatherApiConfigs for selectedWeatherApi with a new hueRotate value
    fun updateHueRotate(newValue: Float) {
        hueRotate = newValue
        selectedWeatherApi?.let { api ->
            selectedWeatherApi = api.copy(
                apiLayerProperties = arrayOf(
                    PropertyFactory.rasterHueRotate(hueRotate)
                )
            )
        }
    }

    //Toggles animation, the animation runs on a couroutine and iterates over available time with 1 second intervals
    fun toggleAnimation() {
        if (_isAnimating.value) {
            animationJob?.cancel()
            animationJob = null
            _isAnimating.value = false
        } else {
            animationJob = viewModelScope.launch {
                _isAnimating.value = true
                while (isActive && _isAnimating.value) {
                    if (selectedTimeIndex < availableTimes.size - 1) {
                        updateSelectedTime(selectedTimeIndex + 1)
                    }
                    delay(1000)
                }
                toggleAnimation()
            }
        }
    }
    //Sets Selectedtime to first avaiable time, and starts animation
    fun restartAnimation() {
        if (_isAnimating.value) {
            toggleAnimation()
        }
        updateSelectedTime(0)
        toggleAnimation()
    }

    //Converting given RGB Color from RGB to HSV, rotating with a angle of hueRotation, then back to RGB
    private fun rotateHue(color: Color, hueRotation: Float): Color {
        val hsv = floatArrayOf(0f, 0f, 0f)
        val rgb = floatArrayOf(
            color.red,
            color.green,
            color.blue
        )
        RGBToHSV(
            (rgb[0] * 255).toInt(),
            (rgb[1] * 255).toInt(),
            (rgb[2] * 255).toInt(),
            hsv
        )
        //hsv[0] is hue
        hsv[0] = (hsv[0] + hueRotation) % 360
        return Color(HSVToColor(hsv))
    }

    //Getters for setting up the map and api layer
    val selectedApiUrl: String
        get() = selectedWeatherApi?.baseUrl ?: ""

    val selectedApiMaxZoom: Float
        get() = selectedWeatherApi?.maxZoom ?: 5f

    val selectedApiLayerProperties: Array<PropertyValue<*>>
        get() = selectedWeatherApi?.apiLayerProperties ?: emptyArray()

    //Making ApiLegend Configuration using MapApiLegend data class
    fun mapLegend(): ApiLegend {
        val selectedApi = selectedWeatherApi?.name
        val hueRotate = hueRotate

        val legendConfig = when (selectedApi) {
            "Temperatur" -> ApiLegend(
                color = listOf(
                    Color(0xFF78003D) to ">50°",
                    Color(0xFFB21002) to "40°",
                    Color(0xFFFF5D46) to "30°",
                    Color(0xFFFFB37A) to "20°",
                    Color(0xFFFFF36E) to "10°",
                    Color(0xFFD0F5D9) to "0°",
                    Color(0xFF85E4ED) to "-10°",
                    Color(0xFF6AC6EE) to "-20°",
                    Color(0xFF649BE2) to "-30°",
                    Color(0xFF114F9D) to "-40°",
                    Color(0xFF481581) to "<-50°"
                ).map { (color, label) -> rotateHue(color, hueRotate) to label },
                text = " °C",
                icon = Icons.Filled.Thermostat
            )

            "Nedbør radar", "Nedbør langtids" -> ApiLegend(
                color = listOf(
                    Color(0xFF7A0087) to ">15",
                    Color(0xFF0055FF) to "5",
                    Color(0xFF0080FF) to "1",
                    Color(0xFF00AAFF) to "0.2",
                    Color(0xFF5ED7FF) to "0.05",
                    Color(0xFF91E4FF) to "0.03",
                ).map { (color, label) -> rotateHue(color, hueRotate) to label },
                text = "mm/t",
                icon = Icons.Filled.WaterDrop
            )

            "Skyer" -> ApiLegend(
                color = listOf(
                    //Using rgb 255,255,255, converting label:String to float, divided by 128 and using it as opacity
                    Color(0xFFffffff) to "100",
                    Color(0xFFffffff) to "90",
                    Color(0xFFffffff) to "80",
                    Color(0xFFffffff) to "70",
                    Color(0xFFffffff) to "60",
                    Color(0xFFffffff) to "50",
                    Color(0xFFffffff) to "40",
                    Color(0xFFffffff) to "30",
                    Color(0xFFffffff) to "20",
                    Color(0xFFffffff) to "10",
                    Color(0xFFffffff) to "0"
                ).map { (color, label) -> rotateHue(color, hueRotate) to label },
                text = "  %",
                icon = Icons.Filled.Cloud,
                background = Color(0xFFc1d6a8)    //Light green color (representing land)
//            background = Color(0xFF94c1e0)    //Light Blue color (representing water)
            )

            "Vind" -> ApiLegend(
                color = listOf(
                    Color(0xFF310047) to ">32.6",
                    Color(0xFF4d0a6c) to "28.5",
                    Color(0xFF5B278D) to "24.5",
                    Color(0xFF7043A8) to "20.8",
                    Color(0xFF7B57ED) to "17.2",
                    Color(0xFF4B87EA) to "13.9",
                    Color(0xFF13A8D6) to "10.8",
                    Color(0xFF3CBEBE) to "8",
                    Color(0xFF79CCAC) to "5.5",
                    Color(0xFFA7CEA1) to "<5.4"
                ).map { (color, label) -> rotateHue(color, hueRotate) to label },
                text = "m/s",
                icon = Icons.Filled.Air
            )

            else -> ApiLegend(
                color = emptyList(),
                text = "",
                icon = Icons.Default.Close
            )
        }

        return legendConfig
    }
}