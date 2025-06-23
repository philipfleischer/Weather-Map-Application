package no.uio.ifi.in2000.vrapp.dependencyinjection

import android.content.Context
import androidx.room.Room
import no.uio.ifi.in2000.vrapp.data.api.longterm.LongTermWeatherDataSource
import no.uio.ifi.in2000.vrapp.data.api.longterm.LongTermWeatherRepository
import no.uio.ifi.in2000.vrapp.data.api.map.MapDataSource
import no.uio.ifi.in2000.vrapp.data.api.map.MapRepository
import no.uio.ifi.in2000.vrapp.data.api.weather.WeatherDataSource
import no.uio.ifi.in2000.vrapp.data.api.weather.WeatherRepository
import no.uio.ifi.in2000.vrapp.data.favorite.FavoriteLocationRepository
import no.uio.ifi.in2000.vrapp.data.local.AppDatabase
import no.uio.ifi.in2000.vrapp.ui.favorites.FavoritesStateHolder

//Defines the interface for accessing app repositories and state holders
interface AppContainer {
    val weatherRepository: WeatherRepository
    val longTermWeatherRepository: LongTermWeatherRepository
    val favoriteRepository: FavoriteLocationRepository
    val mapRepository: MapRepository
    val favoritesStateHolder: FavoritesStateHolder
}

//Implements dependency injection for app repositories and database
class DefaultAppContainer(context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "weather-db"
        ).build()
    }

    private val weatherDataSource = WeatherDataSource()
    private val longTermWeatherDataSource = LongTermWeatherDataSource()
    private val mapDataSource = MapDataSource()

    override val weatherRepository by lazy { WeatherRepository(weatherDataSource) }
    override val longTermWeatherRepository by lazy {
        LongTermWeatherRepository(longTermWeatherDataSource)
    }
    override val favoriteRepository by lazy {
        FavoriteLocationRepository(database.favoriteLocationDao())
    }
    override val mapRepository by lazy { MapRepository(mapDataSource) }

    override val favoritesStateHolder by lazy {
        FavoritesStateHolder(favoriteRepository, weatherRepository)
    }
}