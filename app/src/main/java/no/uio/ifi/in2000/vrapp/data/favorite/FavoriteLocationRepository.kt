package no.uio.ifi.in2000.vrapp.data.favorite

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.vrapp.domain.models.Location

class FavoriteLocationRepository(private val dao: FavoriteLocationDao) {

    // Flow of all favorite locations, converted from entities to domain models
    val favorites: Flow<List<Location>> =
        dao.getAllFavorites().map { entities -> entities.map { it.toDomain() } }


    // Adds a location to favorites
    suspend fun addFavorite(location: Location): Boolean = try {
        dao.insert(location.toEntity())
        true
    } catch (_: Exception) {
        false
    }

    // Removes a location from favorites
    suspend fun removeFavorite(location: Location): Boolean = try {
        dao.deleteByCoordinates(location.latitude, location.longitude)
        true
    } catch (_: Exception) {
        false
    }

    // Toggles favorite status (adds if not favorite, removes if favorite)
    suspend fun toggleFavorite(location: Location): Boolean {
        return if (isFavorite(location)) {
            removeFavorite(location)
        } else {
            addFavorite(location)
        }
    }

    // Checks if a location is marked as favorite
    suspend fun isFavorite(location: Location): Boolean {
        return dao.getByCoordinates(location.latitude, location.longitude) != null
    }
}

