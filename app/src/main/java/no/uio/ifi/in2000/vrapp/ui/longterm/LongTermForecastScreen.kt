package no.uio.ifi.in2000.vrapp.ui.longterm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.uio.ifi.in2000.vrapp.domain.models.DailyForecast
import no.uio.ifi.in2000.vrapp.domain.models.getDailyForecast
import no.uio.ifi.in2000.vrapp.ui.components.BottomAppBarFun
import no.uio.ifi.in2000.vrapp.ui.components.LocalTopAppBar
import no.uio.ifi.in2000.vrapp.ui.components.SearchResultsOverlay
import no.uio.ifi.in2000.vrapp.ui.components.isTablet
import no.uio.ifi.in2000.vrapp.ui.favorites.FavoritesOverlay
import no.uio.ifi.in2000.vrapp.ui.home.HomeViewModel
import no.uio.ifi.in2000.vrapp.ui.home.SkyBackground
import no.uio.ifi.in2000.vrapp.ui.theme.VærAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Function that displays longtermforecast when success in getting data from API
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LongTermForecastScreen(
    navController: NavController,
    viewModel: LongTermForecastViewModel = viewModel(factory = LongTermForecastViewModel.Factory),
    homeViewModel: HomeViewModel
) {
    val weatherUiState by viewModel.weatherUiState.collectAsState()
    val favoritesWithWeather by homeViewModel.favoritesWithWeather.collectAsState()

    // Variables for LocalTopAppBar
    var showFavorites by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isFavorite by homeViewModel.isFavorite.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    // State collection
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showSearchResults by viewModel.showSearchResults.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current


    // For Dynamic Colors
    val isDarkByTime = homeViewModel.isDarkMode.collectAsState().value
    val isDarkMode = remember { mutableStateOf(false) }
    val textColor =
        if (isDarkMode.value) Color.White else Color.Black // Add text color based on theme

    LaunchedEffect(isDarkByTime) {
        isDarkMode.value = isDarkByTime
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation.let {
            viewModel.fetchWeatherData()
        }
    }

    VærAppTheme(
        isDarkByTime = isDarkByTime,
        dynamicColor = false
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                LocalTopAppBar(
                    showFavorites = showFavorites,
                    onBackFromFavorites = {
                        showFavorites = false
                        focusManager.clearFocus()
                    },
                    isFavorite = isFavorite,
                    onFavoriteToggle = { homeViewModel.toggleFavorite() },
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { homeViewModel.searchLocation(it) },
                    onSearchBarClicked = { showFavorites = true }
                )
            },
            bottomBar = { BottomAppBarFun(navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                SkyBackground(
                    viewModel = homeViewModel
                )

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    WeatherContent(
                        weatherUiState = weatherUiState,
                        textColor = textColor
                    ) // Pass textColor here
                }

                // Favorites overlay - shown when search bar is clicked
                if (showFavorites) {
                    FavoritesOverlay(
                        favorites = favoritesWithWeather.keys.toList(),
                        faveWeatherData = favoritesWithWeather,
                        onFavoriteClick = { location ->
                            homeViewModel.selectSearchResult(location)
                            showFavorites = false
                            focusManager.clearFocus()
                        },
                        onDismiss = {
                            showFavorites = false
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.zIndex(1f),
                        contentDescription = "Favoritterlag som gir rask tilgang til favorittværsteder."

                    )
                }

                SearchResultsOverlay(
                    showSearchResults = showSearchResults,
                    isSearching = isSearching,
                    searchResults = searchResults,
                    onLocationSelected = { location ->
                        homeViewModel.selectSearchResult(location)
                        showFavorites = false
                    }
                )


            }
        }
    }
}

@Composable
fun WeatherContent(
    weatherUiState: LongTermWeatherUiState,
    textColor: Color
) {

    val sidePadding = if (isTablet()) 64.dp else 0.dp


    when (weatherUiState) {
        is LongTermWeatherUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is LongTermWeatherUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error loading weather data", color = textColor)
            }
        }

        is LongTermWeatherUiState.Success -> {
            val weatherInfo = (weatherUiState as LongTermWeatherUiState.Success).weatherInfo
            val dailyForecasts = weatherInfo.getDailyForecast()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding), // Apply only here
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = weatherInfo.location?.displayName ?: "-",
                    style = MaterialTheme.typography.displayMedium,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                if (dailyForecasts.isEmpty()) {
                    Text(
                        text = "Ingen værdata tilgjengelig",
                        color = textColor
                    )
                } else {
                    val weeks = groupForecastsByWeek(dailyForecasts)

                    LazyColumn {
                        weeks.forEach { week ->
                            item {
                                WeekSectionHeader(
                                    week.first,
                                    textColor = textColor
                                )
                            }
                            week.second.forEach { forecast ->
                                item {
                                    ForecastDayLongterm(
                                        day = SimpleDateFormat("yyyy-MM-dd", Locale("no")).parse(
                                            forecast.date
                                        )!!,
                                        maxTemp = forecast.maxTemp.toInt(),
                                        minTemp = forecast.minTemp.toInt(),
                                        precipitationProbability = forecast.precipitationProbability,
                                        condition = getLongTermWeatherCondition(forecast),
                                    )
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// Function matches emojis to weathercondition from DailyForecast
fun getLongTermWeatherCondition(forecast: DailyForecast): String {
    return when {
        forecast.precipitationProbability > 70 && forecast.precipitationAmount > 10 -> "🌧️"
        forecast.precipitationProbability > 50 && forecast.precipitationAmount > 5 -> "🌦️"
        forecast.precipitationProbability > 30 -> "🌧️"
        forecast.maxTemp < 0 && forecast.minTemp < 0 -> "❄️"
        forecast.maxTemp < 5 && forecast.minTemp < 0 -> "🌨️"
        forecast.maxTemp > 25 -> "☀️"
        forecast.maxTemp > 20 -> "🌤️"
        else -> "⛅"
    }
}

// Function holds logic to group the API data in 7 day weeks starting monday and ending sunday,
// and removes past days.
fun groupForecastsByWeek(dailyForecasts: List<DailyForecast>): List<Pair<String, List<DailyForecast>>> {
    val weeks = mutableMapOf<String, MutableList<DailyForecast>>()
    val calendar = Calendar.getInstance()

    for (forecast in dailyForecasts) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale("no")).parse(forecast.date) ?: continue
            calendar.time = date
            calendar.firstDayOfWeek = Calendar.MONDAY

            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            val weekKey = "Uke $weekOfYear"

            if (!weeks.containsKey(weekKey)) {
                weeks[weekKey] = mutableListOf()
            }
            weeks[weekKey]?.add(forecast)
        } catch (e: Exception) {
            continue
        }
    }

    return weeks.toList().sortedBy { (key, _) ->
        val parts = key.split(" ")
        if (parts.size >= 3) {
            val week = parts[1].removeSuffix(",").toIntOrNull() ?: 0
            val year = parts[2].toIntOrNull() ?: 0
            year * 100 + week
        } else {
            0 // fallback
        }
    }
        .take(4) // keeps the amount of weeks to 4 because in the fifth, you only get one day

}

// Function creates header of which week it is.
@Composable
fun WeekSectionHeader(weekKey: String, textColor: Color) {
    Text(
        text = weekKey,
        color = textColor,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Function creates a string of the weatherforecast for each day
@Composable
fun ForecastDayLongterm(
    day: Date,
    maxTemp: Int,
    minTemp: Int,
    precipitationProbability: Double,
    condition: String
) {
    val (dayName, date) = getDayAndDate(day)
    val forecastText = "$minTemp°C | $maxTemp°C"


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {

                if (precipitationProbability > 20) {
                    Text(
                        text = "${precipitationProbability.toInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = condition,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = forecastText,
                    style = MaterialTheme.typography.headlineSmall,
                )

            }
        }
    }
}

//Currently unused
// Helperfunction to check if a given date is today
fun isToday(date: Date): Boolean {
    val today = Calendar.getInstance()
    val otherDate = Calendar.getInstance().apply { time = date }
    return today.get(Calendar.YEAR) == otherDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == otherDate.get(Calendar.DAY_OF_YEAR)
}

// Helperfunction that formats the date into a readable date, pairing actual date and weekday
fun getDayAndDate(date: Date): Pair<String, String> {
    val calendar = Calendar.getInstance().apply {
        time = date
    }

    val dayName = SimpleDateFormat("EEEE", Locale("no")).format(calendar.time)
        .replaceFirstChar { it.uppercase() }
    val dateFormatted = SimpleDateFormat("d. MMMM", Locale("no")).format(calendar.time)
    return Pair(dayName, dateFormatted)
}
