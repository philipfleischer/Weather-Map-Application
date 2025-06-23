package no.uio.ifi.in2000.vrapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.ui.home.HomeViewModel
import no.uio.ifi.in2000.vrapp.ui.home.calculateCurrentFrame
import no.uio.ifi.in2000.vrapp.ui.theme.AppTheme
import no.uio.ifi.in2000.vrapp.ui.theme.ColorMode
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

// Checks if current time is during dark hours (night or early dawn)
@RequiresApi(Build.VERSION_CODES.O)
fun isFrameDark(sunrise: String?, sunset: String?): Boolean {
    if (sunrise == null || sunset == null) {
        return false // Default LightMode
    }
    val frame = calculateCurrentFrame(sunrise, sunset)
    return frame in 195..300 || frame in 0..4
}

// Norwegian timezone
@RequiresApi(Build.VERSION_CODES.O)
fun parseAndFormatTime(isoTime: String): String {
    return try {
        val osloZone = ZoneId.of("Europe/Oslo")
        val time = OffsetDateTime.parse(isoTime)
            .atZoneSameInstant(osloZone)

        DateTimeFormatter.ofPattern("HH:mm")
            .withLocale(Locale("no", "NO"))
            .format(time)
    } catch (e: Exception) {
        "--:--"
    }
}

//Checks if emulator/device is tablet
@Composable
fun isTablet(): Boolean {
    return LocalConfiguration.current.screenWidthDp >= 600
}


fun getShortWeekday(dayOffset: Int): String {
    return Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, dayOffset)
    }.let { calendar ->
        // Get the full weekday name and take first 3 letters
        SimpleDateFormat("EEEE", Locale("no")).format(calendar.time)
            .take(3)
            .replaceFirstChar { it.uppercase() }
    }
}

// Converts wind degrees to directional arrow symbol
fun getWindDirectionArrow(degrees: Double?): String {
    if (degrees == null) return ""
    return when {
        degrees >= 337.5 || degrees < 22.5 -> "↓"  // N (wind FROM north, so arrow points down)
        degrees < 67.5 -> "↙"  // NE
        degrees < 112.5 -> "←"  // E
        degrees < 157.5 -> "↖"  // SE
        degrees < 202.5 -> "↑"  // S
        degrees < 247.5 -> "↗"  // SW
        degrees < 292.5 -> "→"  // W
        degrees < 337.5 -> "↘"  // NW
        else -> ""
    }
}

//Box to show SearchResults from geoSearch
@Composable
fun SearchResultsOverlay(
    showSearchResults: Boolean,
    isSearching: Boolean,
    searchResults: List<Location>,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    if (showSearchResults) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, end = 16.dp, top = 100.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(8.dp)
                )
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .zIndex(1f)
        ) {
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp)
                ) {
                    items(searchResults) { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(location)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Lokasjon på",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = location.name ?: "...",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Optional divider between items
                        if (location != searchResults.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
//Currently unused
// Determines current theme state (dark/light) based on settings and time
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun getCurrentThemeState(
    viewModel: HomeViewModel,
    isDarkByTime: Boolean = false
): ThemeState {
    val sunrise = viewModel.sunrise.collectAsState().value
    val sunset = viewModel.sunset.collectAsState().value

    val isDark = when (AppTheme.currentTheme) {
        ColorMode.LIGHT -> false
        ColorMode.DARK -> true
        ColorMode.DYNAMIC -> isDarkByTime || isFrameDark(sunrise, sunset)
    }

    return ThemeState(
        isDark = isDark,
        textColor = if (isDark) Color.White else Color.Black
    )
}

data class ThemeState(
    val isDark: Boolean,
    val textColor: Color
)