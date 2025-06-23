package no.uio.ifi.in2000.vrapp.domain.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

//Data class for map legend
data class ApiLegend(
    val color: List<Pair<Color, String>>,//pairing color and the unit value string representing the color
    val text: String,   //unit
    val icon: ImageVector,
    val background: Color? = null
)