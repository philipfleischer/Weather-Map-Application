package no.uio.ifi.in2000.vrapp.domain.models

import org.maplibre.android.style.layers.PropertyValue

//Data class with map weather api configurations
data class MapWeatherApi (
    val name:String,
    val baseUrl: String,
    val maxZoom: Float,
    val apiLayerProperties: Array<PropertyValue<*>>
)