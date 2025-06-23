package no.uio.ifi.in2000.vrapp.domain.models

import kotlinx.serialization.Serializable

//location object data class
@Serializable
data class Location(
    val name: String? = null,
    val latitude: Float,
    val longitude: Float,
    val isFavorite: Boolean = false
) {
    //splits name(fullName) to only "skrivemåte"
    val displayName: String?
        get() = name?.split(",")?.get(0)

}
