package no.uio.ifi.in2000.vrapp.data.favorite


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.vrapp.domain.models.Location

// Entity representing a favorite location in the Room database
@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Unique ID (auto-generated)
    val name: String,
    val latitude: Float,
    val longitude: Float,
    val timestamp: Long = System.currentTimeMillis() // Used for ordering
)

// Converts a database entity to a domain model (Location)
fun FavoriteLocationEntity.toDomain() = Location(
    name = name,
    latitude = latitude,
    longitude = longitude
)

// Converts a domain model (Location) to a database entity
fun Location.toEntity() = FavoriteLocationEntity(
    name = name.orEmpty(),
    latitude = latitude,
    longitude = longitude
)

@Dao
interface FavoriteLocationDao {
    // Inserts a new favorite location into the database
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: FavoriteLocationEntity)

    // Deletes a favorite location from the database
    @Delete
    suspend fun delete(location: FavoriteLocationEntity)

    // Gets all favorite locations sorted by most recent first
    @Query("SELECT * FROM favorite_locations ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteLocationEntity>>

    @Query("SELECT * FROM favorite_locations WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    suspend fun getByCoordinates(lat: Float, lon: Float): FavoriteLocationEntity?

    @Query("DELETE FROM favorite_locations WHERE latitude = :lat AND longitude = :lon")
    suspend fun deleteByCoordinates(lat: Float, lon: Float)


}