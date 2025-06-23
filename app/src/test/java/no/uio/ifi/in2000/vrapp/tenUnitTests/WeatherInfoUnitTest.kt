package no.uio.ifi.in2000.vrapp.tenUnitTests
import no.uio.ifi.in2000.vrapp.domain.models.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class WeatherInfoTest {

    // Prepare dummy TimeSeries data for WeatherInfo instance
    private val dummyTimeSeries = listOf(
        TimeSeries(
            time = "2025-05-16T12:00:00Z",
            data = ForecastData(
                instant = InstantData(
                    details = WeatherDetails(
                        pressure = 1013.0,
                        temperature = 20.0,
                        cloudCover = 0.5,
                        humidity = 50.0,
                        windDirection = 0.0,
                        windSpeed = 3.0,
                        precipitation = 0.0
                    )
                )
            )
        )
    )

    private val dummyProperties = Properties(dummyTimeSeries)
    private val weatherInfo = WeatherInfo(dummyProperties)

    @Test
    fun `getWeatherCondition returns correct emoji for night rain`() {
        val details = WeatherDetails(
            pressure = 1013.0,
            temperature = 5.0,
            cloudCover = 0.9,
            humidity = 90.0,
            windDirection = 180.0,
            windSpeed = 5.0,
            precipitation = 1.0
        )
        val symbolCode = "rain_night"

        val result = weatherInfo.getWeatherCondition(details, symbolCode)

        assertEquals("🌧️", result)
    }

    @Test
    fun `getWeatherCondition returns sun emoji for day clear`() {
        val details = WeatherDetails(
            pressure = 1013.0,
            temperature = 25.0,
            cloudCover = 0.1,
            humidity = 40.0,
            windDirection = 90.0,
            windSpeed = 3.0,
            precipitation = 0.0
        )
        val symbolCode = "clear_day"

        val result = weatherInfo.getWeatherCondition(details, symbolCode)

        assertEquals("☀️", result)
    }

    @Test
    fun `getWeatherCondition returns cloud emoji fallback if no symbolCode`() {
        val details = WeatherDetails(
            pressure = 1013.0,
            temperature = 15.0,
            cloudCover = 0.7,
            humidity = 50.0,
            windDirection = 270.0,
            windSpeed = 4.0,
            precipitation = 0.0
        )
        val result = weatherInfo.getWeatherCondition(details, null)

        // Because cloudCover 0.7 is in 0.5..0.8 range => "⛅"
        assertEquals("⛅", result)
    }
}
