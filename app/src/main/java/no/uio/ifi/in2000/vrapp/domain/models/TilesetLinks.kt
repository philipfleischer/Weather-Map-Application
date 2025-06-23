package no.uio.ifi.in2000.vrapp.domain.models

import kotlinx.serialization.Serializable

//Timed links data classes
@Serializable
data class IntermediaryTimedLinks(
    val time: String,
    val tiles: HashMap<String, String>
)

data class TimedLinks(
    val time: String,
    val pngUrl: String
)