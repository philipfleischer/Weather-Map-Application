package no.uio.ifi.in2000.vrapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.vrapp.domain.models.WeatherInfo

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BigWeatherHeader(
    weatherInfo: WeatherInfo,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        // Location name
        Text(
            text = weatherInfo.location?.displayName ?: "Laster",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Light,
            color = textColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Current temperature
        Text(
            text = "${weatherInfo.currentTemp}°C",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.W700,
            color = textColor
        )

        // Feels-like temperature
        Text(
            text = "Føles som: ${weatherInfo.feelsLikeTemp}°C",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp),
            color = textColor
        )
        Spacer(modifier = Modifier.weight(0.7f))
    }
}

@Composable
fun CompactWeatherHeader(
    weatherInfo: WeatherInfo,
    modifier: Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location name
            Text(
                text = weatherInfo.location?.displayName ?: "Laster",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(8f))
                // Current temperature
                Text(
                    text = "${weatherInfo.currentTemp}°C",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.weight(1f))

                // Separating icon
                Text(text = "|", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.weight(1f))

                // Feels-like temperature
                Text(
                    text = "Føles som: ${weatherInfo.feelsLikeTemp}°C",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.weight(8f))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherHeaderTransition(
    weatherInfo: WeatherInfo,
    textColor: Color,
    showCompactHeader: Boolean,
    modifier: Modifier = Modifier
) {
    // Get screen configuration
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Calculate header heights based on screen size
    val bigHeaderHeight: Dp = remember(configuration, density) {
        with(density) {
            (configuration.screenHeightDp.dp * 0.3f) // Big header takes 30% of screen height
        }
    }
    val compactHeaderHeight = 110.dp // Fixed height for compact header

    // Animate the container height
    val animatedHeight by animateDpAsState(
        targetValue = if (showCompactHeader) compactHeaderHeight else bigHeaderHeight,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 20f
        )
    )

    // Container for animated headers
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
    ) {
        // Big header - fades and scales when hiding
        AnimatedVisibility(
            visible = !showCompactHeader,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier.fillMaxWidth()
        ) {
            BigWeatherHeader(
                weatherInfo = weatherInfo,
                textColor = textColor,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Compact header - slides down from top
        AnimatedVisibility(
            visible = showCompactHeader,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.fillMaxWidth()
        ) {
            CompactWeatherHeader(
                weatherInfo = weatherInfo,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



