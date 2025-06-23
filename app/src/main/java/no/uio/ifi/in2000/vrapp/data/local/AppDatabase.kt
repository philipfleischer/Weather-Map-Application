package no.uio.ifi.in2000.vrapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import no.uio.ifi.in2000.vrapp.data.favorite.FavoriteLocationDao
import no.uio.ifi.in2000.vrapp.data.favorite.FavoriteLocationEntity

// Room database class for storing favorite locations
@Database(
    entities = [FavoriteLocationEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteLocationDao(): FavoriteLocationDao
}
