package no.uio.ifi.in2000.vrapp.tenUnitTests.repositories

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.vrapp.data.api.weather.WeatherDataSource
import no.uio.ifi.in2000.vrapp.data.api.weather.WeatherRepository
import no.uio.ifi.in2000.vrapp.domain.models.ForecastData
import no.uio.ifi.in2000.vrapp.domain.models.InstantData
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.Properties
import no.uio.ifi.in2000.vrapp.domain.models.TimeSeries
import no.uio.ifi.in2000.vrapp.domain.models.WeatherDetails
import no.uio.ifi.in2000.vrapp.domain.models.WeatherInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WeatherRepositoryUnitTest {

    @MockK
    lateinit var mockDataSource: WeatherDataSource

    private lateinit var repository: WeatherRepository

    private val dummyDetailsForWeather = WeatherDetails(
        pressure = 1013.0,
        temperature = 20.0,
        cloudCover = 0.0,
        humidity = 50.0,
        windDirection = 180.0,
        windSpeed = 3.0
    )

    private val dummyTimeSeries = listOf(
        TimeSeries(
            time = "2025-05-16T12:00:00Z",
            data = ForecastData(
                instant = InstantData(details = dummyDetailsForWeather)
            )
        )
    )

    private val testWeatherInfo = WeatherInfo(
        properties = Properties(dummyTimeSeries)
    )

    private val testLocation = Location("Oslo", 59.9f, 10.7f)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = WeatherRepository(mockDataSource)
    }

    @Test
    fun `WeatherRepository caches weather data`() = runTest {
        coEvery { mockDataSource.getLocationForecast(testLocation) } returns testWeatherInfo
        val firstCall = repository.getWeatherData(testLocation)
        val secondCall = repository.getWeatherData(testLocation)
        coVerify(exactly = 1) { mockDataSource.getLocationForecast(testLocation) }
        assertEquals(testWeatherInfo, firstCall)
        assertEquals(testWeatherInfo, secondCall)
    }

    @Test
    fun `WeatherRepository returns null if API fails`() = runTest {
        coEvery { mockDataSource.getLocationForecast(testLocation) } throws Exception("Network error")
        val result = repository.getWeatherData(testLocation)
        assertNull(result)
    }

    @Test
    fun `WeatherRepository fetches new data for a different location`() = runTest {
        val otherLocation = Location("Bergen", 60.4f, 5.3f)
        val dummyDetailsForBergen = WeatherDetails(
            pressure = 1005.0,
            temperature = 15.0,
            cloudCover = 80.0,
            humidity = 70.0,
            windDirection = 200.0,
            windSpeed = 5.0
        )

        val dummyTimeSeriesBergen = listOf(
            TimeSeries(
                time = "2025-05-16T12:00:00Z",
                data = ForecastData(
                    instant = InstantData(details = dummyDetailsForBergen)
                )
            )
        )

        val testWeatherInfoBergen = WeatherInfo(
            properties = Properties(dummyTimeSeriesBergen)
        )
        coEvery { mockDataSource.getLocationForecast(testLocation) } returns testWeatherInfo
        coEvery { mockDataSource.getLocationForecast(otherLocation) } returns testWeatherInfoBergen
        val osloData = repository.getWeatherData(testLocation)
        val bergenData = repository.getWeatherData(otherLocation)
        coVerify(exactly = 1) { mockDataSource.getLocationForecast(testLocation) }
        coVerify(exactly = 1) { mockDataSource.getLocationForecast(otherLocation) }
        assertEquals(testWeatherInfo, osloData)
        assertEquals(testWeatherInfoBergen, bergenData)
    }
}