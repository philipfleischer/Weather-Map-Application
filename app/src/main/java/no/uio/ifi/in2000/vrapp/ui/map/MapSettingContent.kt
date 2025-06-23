package no.uio.ifi.in2000.vrapp.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//Function to display adjustable content in settings for each screen, in this case mapScreen
@Composable
fun MapSettingsContent(
    hueRotate: Float,
    onHueRotateChange: (Float) -> Unit,
    isLocationUpdatesActive: Boolean,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onStartLocationUpdates: () -> Unit,
    onStopLocationUpdates: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Icon(
                imageVector = Icons.Default.Palette, // Built-in palette icon
                contentDescription = "Fargehjul ikon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant  // Auto-adjusts for light/dark mode
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Fargehjul ${hueRotate.toInt()}°",
                style = MaterialTheme.typography.bodyMedium
            )

        }

        Slider(
            value = hueRotate,
            onValueChange = onHueRotateChange,
            valueRange = 0f..360f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Gray,
                activeTrackColor = Color.Gray,
                inactiveTrackColor = Color.LightGray
            )
        )

        HorizontalDivider()
        Spacer(modifier = Modifier.padding(6.dp))


        val locationToggleText = if (isLocationUpdatesActive) "GPS: Aktiv" else "GPS: Inaktiv"
        val icon = if (isLocationUpdatesActive) Icons.Filled.MyLocation else Icons.Filled.LocationDisabled


        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isLocationUpdatesActive) {
                        onStopLocationUpdates()
                    } else {
                        if (permissionGranted) {
                            onStartLocationUpdates()
                        } else {
                            onRequestPermission()
                        }
                    }
                    onDismiss()
                }
        ) {
            Icon(
                icon,
                contentDescription = locationToggleText,
                tint = MaterialTheme.colorScheme.onSurfaceVariant

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                locationToggleText,
                style = MaterialTheme.typography.bodyMedium ,
                modifier = Modifier.weight(1f)
            )

        }
    }
}
