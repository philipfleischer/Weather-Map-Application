package no.uio.ifi.in2000.vrapp.ui.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.vrapp.domain.models.FavoriteWeather
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.ui.components.isTablet


@Composable
fun FavoritePlaces(
    locations: List<Location>,
    faveWeatherData: Map<Location, FavoriteWeather>,
    onPlaceClick: (Location) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dynamicPadding = if (isTablet()) 74.dp else 16.dp

    // Main surface container
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(dynamicPadding)
                .padding(top = 100.dp) // Extra top padding for header space
        ) {
            // Header
            Text(
                text = "Favorittsteder",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Shows empty state message or list of favorites
            if (locations.isEmpty()) {
                EmptyFavoritesMessage()
            } else {
                LazyColumn {
                    items(locations.size) { index ->
                        val location = locations[index]
                        val favoriteWeather = faveWeatherData[location]
                        FavoritePlaceItem(
                            location = location,
                            weatherEmoji = favoriteWeather?.weatherInfo?.hourlyForecast?.firstOrNull()
                                ?.split(" ")?.get(1) ?: "☁️",
                            temperature = favoriteWeather?.weatherInfo?.currentTemp ?: "--",
                            maxTemp = favoriteWeather?.maxTemp ?: "--",
                            minTemp = favoriteWeather?.minTemp ?: "--",
                            onClick = { onPlaceClick(location) }
                        )
                    }
                }

            }
        }
    }
}


// Message when no favorites are saved
@Composable
private fun EmptyFavoritesMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Ingen favoritter enda;",
                style = MaterialTheme.typography.titleLarge

            )
            Spacer(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Legg til noen med hjerte ikonet ♡",
                style = MaterialTheme.typography.titleLarge
            )

        }


    }
}

// Individual favorite location card item
@Composable
fun FavoritePlaceItem(
    location: Location,
    weatherEmoji: String,
    temperature: String,
    maxTemp: String,
    minTemp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick) // Makes entire card clickable
            .semantics { contentDescription = "Trykk for å se været for ${location.name ?: "N/A"}" },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {

            // Left column: Location name and weather emoji
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = location.displayName ?: "N/A",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = weatherEmoji,
                    style = MaterialTheme.typography.headlineMedium

                )
            }

            // Right column: Temperature info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$temperature °C",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = "L: $minTemp H: $maxTemp°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,


                    )
            }

        }
    }
}

// Full-screen overlay for showing favorites
@Composable
fun FavoritesOverlay(
    favorites: List<Location>,
    faveWeatherData: Map<Location, FavoriteWeather>,
    onFavoriteClick: (Location) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier.fillMaxSize(),

        ) {
        FavoritePlaces(
            locations = favorites,
            onPlaceClick = onFavoriteClick,
            modifier = Modifier.padding(16.dp),
            faveWeatherData = faveWeatherData
        )
    }
}


