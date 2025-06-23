
package no.uio.ifi.in2000.vrapp.ui.home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

/*
This code uses the sunrise API to get the sunrise and sunset time, and converts frames from an
animation to fit the hours of the day. sunrise and sunset is aligned with the time in the API,
then the rest of the frames are set in between.
Checks if there is rain at current location this hour,The night sky is the same, but through
the day it is either rainy og sunny.
This creates a dynamic background used on the HomeScreen, that changes every minute
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SkyBackground(
    viewModel: HomeViewModel
) {

    val context = LocalContext.current

    // Get sunrise and sunset from Viewmodel
    val sunrise = viewModel.sunrise.collectAsState(initial = null).value
    val sunset = viewModel.sunset.collectAsState(initial = null).value

    // Raining status
    val isRaining = viewModel.isRaining.collectAsState(initial = false).value

    // State for current animation frame
    var currentFrame by remember { mutableStateOf<Int?>(null) }

    // Fetch sun times if not already available
    LaunchedEffect(Unit) {
        if (sunrise == null || sunset == null) {
            withContext(Dispatchers.IO) {
                viewModel.fetchSunTimes()
            }
        }
    }

    // Update frame every minute based on current time and sun position
    LaunchedEffect(sunrise, sunset) {
        if (sunrise != null && sunset != null) {
            while (true) {
                currentFrame = try {
                    calculateCurrentFrame(sunrise, sunset)
                } catch (_: Exception) {
                    100 // Default frame if calculation fails
                }
                delay(60_000) // Update every minute
            }
        }
    }

    // Display the current sky frame
    currentFrame?.let { frame ->
        val backgroundResource = remember(frame, isRaining) {
            getFrameResourceId(context, frame, isRaining)
        }

        Image(
            painter = painterResource(id = backgroundResource),
            contentDescription = "Bakgrunn",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


// Calculates appropriate animation frame based on current time relative to sunrise/sunset
@RequiresApi(Build.VERSION_CODES.O)
fun calculateCurrentFrame(sunrise: String?, sunset: String?): Int {
    if (sunrise == null || sunset == null) {
        throw IllegalArgumentException("Sunrise or sunset time is missing")
    }

    val sunriseMin = parseTimeToMinutes(sunrise)
        ?: throw IllegalArgumentException("Invalid sunrise time format")

    val sunsetMin = parseTimeToMinutes(sunset)
        ?: throw IllegalArgumentException("Invalid sunset time format")

    // CEST time
    val now = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
    val totalMinutes = now.hour * 60 + now.minute

    return when {
        // Sunrise frame 50, midday frame 100, sunset frame 150, 200-300 night

        totalMinutes < sunriseMin - 60 -> 250


        totalMinutes < sunriseMin -> {
            val progress = totalMinutes - (sunriseMin - 60)
            0 + (progress * (50 - 0) / 60)
        }

        totalMinutes < sunriseMin + (sunsetMin - sunriseMin) / 2 -> {
            val halfDayDuration = (sunsetMin - sunriseMin) / 2
            val progress = totalMinutes - sunriseMin
            50 + (progress * (100 - 50) / halfDayDuration)
        }

        totalMinutes < sunsetMin -> {
            val halfDayDuration = (sunsetMin - sunriseMin) / 2
            val progress = totalMinutes - (sunriseMin + halfDayDuration)
            100 + (progress * (150 - 100) / halfDayDuration)
        }

        totalMinutes < sunsetMin + 60 -> {
            val progress = totalMinutes - sunsetMin
            150 + (progress * (200 - 150) / 60)
        }

        else -> {
            val nightDuration = 24 * 60 - (sunsetMin + 60) + (sunriseMin - 60)
            val progress = (totalMinutes - (sunsetMin + 60)) % nightDuration
            200 + (progress * (300 - 200) / nightDuration)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseTimeToMinutes(timeString: String?): Int? {
    return try {
        if (timeString == null) return null
        val dateTime = OffsetDateTime.parse(timeString)
            .atZoneSameInstant(ZoneId.of("Europe/Oslo"))
            .toLocalDateTime()

        dateTime.hour * 60 + dateTime.minute
    } catch (e: DateTimeParseException) {
        null
    }
}


// Get accurate timeframe
private fun getFrameResourceId(context: Context, frame: Int, isRaining: Boolean): Int {

    val baseName = if (isRaining && frame in 50..200) {
        "rain"
    } else {
        "sky"
    }
    val frameNumber = frame.toString().padStart(4, '0')
    val resourceName = "${baseName}_$frameNumber"

    return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
}

