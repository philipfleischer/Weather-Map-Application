package no.uio.ifi.in2000.vrapp.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import no.uio.ifi.in2000.vrapp.R
import no.uio.ifi.in2000.vrapp.WeatherApplication
import no.uio.ifi.in2000.vrapp.domain.models.HourlyData
import no.uio.ifi.in2000.vrapp.ui.components.BottomAppBarFun
import no.uio.ifi.in2000.vrapp.ui.components.LocalTopAppBar
import no.uio.ifi.in2000.vrapp.ui.components.SearchResultsOverlay
import no.uio.ifi.in2000.vrapp.ui.components.WeatherHeaderTransition
import no.uio.ifi.in2000.vrapp.ui.components.getShortWeekday
import no.uio.ifi.in2000.vrapp.ui.components.getWindDirectionArrow
import no.uio.ifi.in2000.vrapp.ui.components.isFrameDark
import no.uio.ifi.in2000.vrapp.ui.components.isTablet
import no.uio.ifi.in2000.vrapp.ui.components.parseAndFormatTime
import no.uio.ifi.in2000.vrapp.ui.favorites.FavoritesOverlay
import no.uio.ifi.in2000.vrapp.ui.theme.VærAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    weatherUiState: WeatherUiState,
    viewModel: HomeViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    // State collection
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showSearchResults by viewModel.showSearchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val favoritesWithWeather by viewModel.favoritesWithWeather.collectAsState()

    // UI state variables
    var expandedDay by remember { mutableStateOf<Int?>(null) }
    var showFavorites by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


    // Scroll state  -> For header animation
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val showCompactHeader by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                true
            } else {
                val thresholdPx = with(density) { 150.dp.roundToPx() }
                listState.firstVisibleItemScrollOffset > thresholdPx
            }
        }
    }

    // Fetch weather when location changes
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            viewModel.fetchWeatherData()
        }
    }

    // Dark mode handling
    val isDarkByTime = viewModel.isDarkMode.collectAsState().value
    val isDarkMode = remember { mutableStateOf(false) }
    val textColor = if (isDarkMode.value) Color.White else Color.Black
    LaunchedEffect(isDarkByTime) {
        isDarkMode.value = isDarkByTime
    }

    // Location permission handling
    val context = LocalContext.current
    val application = context.applicationContext as WeatherApplication
    val gpsManager = application.gpsManager
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        gpsManager.updatePermissionStatus()
        if (gpsManager.hasLocationPermission()) {
            gpsManager.startLocationUpdates()
        }
    }

    // Request location permissions on launch
    LaunchedEffect(Unit) {
        if (!gpsManager.hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            gpsManager.startLocationUpdates()
        } else {
            gpsManager.startLocationUpdates()
        }
    }

    // Main theme and layout
    VærAppTheme(isDarkByTime = isDarkByTime, dynamicColor = false) {
        Scaffold(
            topBar = {
                Column {
                    LocalTopAppBar(
                        showFavorites = showFavorites,
                        onBackFromFavorites = {
                            showFavorites = false
                            focusManager.clearFocus()
                        },
                        isFavorite = isFavorite,
                        onFavoriteToggle = { viewModel.toggleFavorite() },
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { viewModel.searchLocation(it) },
                        onSearchBarClicked = { showFavorites = true },
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = { BottomAppBarFun(navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Background layer
                SkyBackground(
                    viewModel = viewModel
                )

                // Main content column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Animated weather header
                    if (weatherUiState is WeatherUiState.Success && !showSearchResults) {
                        WeatherHeaderTransition(
                            weatherInfo = weatherUiState.weatherInfo,
                            textColor = textColor,
                            showCompactHeader = showCompactHeader,
                            modifier = Modifier.zIndex(1f) // Ensure it stays above other content
                        )
                    }

                    // Main content
                    WeatherContent(
                        weatherUiState = weatherUiState,
                        listState = listState,
                        expandedDay = expandedDay,
                        onDayClick = { index ->
                            expandedDay = if (expandedDay == index) null else index
                        },
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)

                    )
                }

                // Favorites overlay -> Shown when search bar is clicked
                if (showFavorites) {
                    FavoritesOverlay(
                        favorites = favoritesWithWeather.keys.toList(),
                        faveWeatherData = favoritesWithWeather,
                        onFavoriteClick = { location ->
                            viewModel.selectSearchResult(location)
                            showFavorites = false
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.zIndex(1f),
                        onDismiss = {
                            showFavorites = false
                            focusManager.clearFocus()
                        }
                    )
                }

                // Search results overlay
                SearchResultsOverlay(
                    showSearchResults = showSearchResults,
                    isSearching = isSearching,
                    searchResults = searchResults,
                    onLocationSelected = { location ->
                        viewModel.selectSearchResult(location)
                        showFavorites = false
                    }
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherContent(
    weatherUiState: WeatherUiState,
    listState: LazyListState,
    expandedDay: Int?,
    onDayClick: (Int) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {

    val dynamicPadding = if (isTablet()) 80.dp else 16.dp

    // Collect celestial data
    val sunrise = viewModel.sunrise.collectAsState(initial = null).value
    val sunset = viewModel.sunset.collectAsState(initial = null).value
    val moonPhasePercent by viewModel.moonPhasePercent.collectAsState()
    val isDarkMode = remember { mutableStateOf(false) }

    // Update dark mode based on sunrise/sunset
    LaunchedEffect(sunrise, sunset) {
        if (sunrise != null && sunset != null) {
            isDarkMode.value = isFrameDark(sunrise, sunset)
        }
    }

    // Main weather content list
    LazyColumn(
        state = listState,
        modifier = modifier.padding(dynamicPadding)
    ) {
        when (weatherUiState) {
            is WeatherUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is WeatherUiState.Error -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error loading weather data")
                    }
                }
            }

            is WeatherUiState.Success -> {
                val weatherInfo = weatherUiState.weatherInfo


                // Hourly forecast
                item { HourlyForecast(weatherInfo.hourlyForecast) }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Daily forecast items
                items(weatherInfo.weeklyForecast.indices.toList()) { index ->
                    ForecastDay(
                        day = index,
                        expandedDay = expandedDay,
                        forecast = weatherInfo.weeklyForecast[index],
                        onClick = { onDayClick(index) },
                        structuredHourlyForecast = weatherInfo.structuredHourlyForecasts[index],
                        precipitationProbability = weatherInfo.getDayPrecipitationProbability(index)
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Weather widgets (wind, precipitation, etc)
                item {
                    WeatherWidgets(
                        currentWeather = weatherInfo.structuredHourlyForecasts.firstOrNull()
                            ?.firstOrNull(),
                        precipitationProbability = weatherInfo.getDayPrecipitationProbability(0),
                        sunriseTime = sunrise,
                        sunsetTime = sunset,
                        moonPhasePercent = moonPhasePercent
                    )
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }

        }
    }
}


@Composable
fun HourlyForecast(hourlyForecast: List<String>) {
    // Horizontal scrollable hourly forecast
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(25.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        items(hourlyForecast) { forecast ->
            val parts = forecast.split(" ")
            val hour = parts.getOrNull(0) ?: "--:--"
            val condition = parts.getOrNull(1) ?: "☁️"
            val temperature = parts.getOrNull(2) ?: "--°C"

            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = hour, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = condition,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Text(text = temperature, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}


@Composable
fun ForecastDay(
    day: Int,
    expandedDay: Int?,
    forecast: String,
    onClick: () -> Unit,
    structuredHourlyForecast: List<HourlyData>,
    precipitationProbability: Double,
) {
    // Format date for display
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"))
    calendar.add(Calendar.DAY_OF_YEAR, day)
    val date = calendar.time
    val dateText = remember(date) {
        SimpleDateFormat("d. MMMM", Locale("no")).format(date)
    }

    // Daily forecast card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 26.dp)
            .semantics { this.contentDescription = "Trykk for å utvide værvarsel" }

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (day == 0) "I dag $dateText" else getShortWeekday(day) + " $dateText",
                style = MaterialTheme.typography.headlineSmall

            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Precipitation indicator if probability > 20%
                if (precipitationProbability > 20) {
                    Text(
                        text = "${precipitationProbability.toInt()}%",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Forecast
                Text(
                    text = forecast,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        // Expanded hourly forecast when clicked
        if (expandedDay == day) {
            ExpandedDayForecast(
                structuredHourlyForecast,
            )
        }
    }
}


@Composable
fun ExpandedDayForecast(dailyForecast: List<HourlyData>) {
    // Detailed forecast on dayClick
    Column(modifier = Modifier.padding(top = 8.dp)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .defaultMinSize(minHeight = 50.dp)
                .heightIn(max = 200.dp)
        ) {
            items(dailyForecast) { hourlyData ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time display
                    if (hourlyData.hourRange.isNotEmpty()) {
                        Text(
                            text = hourlyData.hourRange,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    } else {
                        Text(
                            text = String.format(Locale.ROOT, "%02d", hourlyData.hour),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Weather icon
                    Text(
                        text = hourlyData.condition,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Rain chance
                    if (hourlyData.precipitation > 0) {
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${hourlyData.precipitation} mm",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))

                    } else Spacer(modifier = Modifier.weight(4f))


                    // Wind
                    Text(
                        text = "${hourlyData.windSpeed.roundToInt()} m/s ${
                            getWindDirectionArrow(hourlyData.windDirection)
                        }",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(2f))

                    // Temperature
                    Text(
                        text = "${hourlyData.temperature}°C",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }


    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherWidgets(
    currentWeather: HourlyData?,
    precipitationProbability: Double?,
    sunriseTime: String?,
    sunsetTime: String?,
    moonPhasePercent: Int?
) {
    // Format sunrise/sunset times
    val formattedSunrise =
        remember(sunriseTime) { sunriseTime?.let { parseAndFormatTime(it) } ?: "--:--" }
    val formattedSunset =
        remember(sunsetTime) { sunsetTime?.let { parseAndFormatTime(it) } ?: "--:--" }

    // Container for weather info cards
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Wind
                WeatherInfoCard(
                    title = "Vind",
                    value = "${currentWeather?.windSpeed?.roundToInt() ?: "--"} m/s",
                    icon = {
                        Text(
                            text = getWindDirectionArrow(currentWeather?.windDirection),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                )
                // Current rain
                WeatherInfoCard(
                    title = "Nedbør",
                    value = "${currentWeather?.precipitation?.roundToInt() ?: "--"} mm"
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rain chance
                WeatherInfoCard(
                    title = "Sjanse for regn",
                    value = "${precipitationProbability?.toInt() ?: "--"}%"
                )

                // Moon phase in %
                WeatherInfoCard(
                    title = "Månefase",
                    value = "${moonPhasePercent ?: "--"}%",
                    icon = { Icon(Icons.Outlined.NightsStay, contentDescription = "Måne fase") }
                )

            }
            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                // Sunrise
                WeatherInfoCard(
                    title = "Soloppgang",
                    value = formattedSunrise,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.sunrise),
                            contentDescription = "Soloppgang",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                        )
                    }
                )

                // Sunset
                WeatherInfoCard(
                    title = "Solnedgang",
                    value = formattedSunset,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.sunrise),
                            contentDescription = "Solnedgang",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)

                        )
                    }
                )
            }

        }
    }

    Spacer(modifier = Modifier.padding(8.dp))
}

// Reusable weather info card
@Composable
fun WeatherInfoCard(
    title: String,
    value: String,
    icon: @Composable (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            icon?.invoke()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}







