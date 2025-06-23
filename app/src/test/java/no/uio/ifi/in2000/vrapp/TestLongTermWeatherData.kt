package no.uio.ifi.in2000.vrapp

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.vrapp.data.api.longterm.LongTermWeatherDataSource
import no.uio.ifi.in2000.vrapp.data.api.longterm.LongTermWeatherRepository
import no.uio.ifi.in2000.vrapp.domain.models.Geometry
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.domain.models.LongTermProperties
import no.uio.ifi.in2000.vrapp.domain.models.LongTermTimeSeries
import no.uio.ifi.in2000.vrapp.domain.models.LongTermTimeSeriesData
import no.uio.ifi.in2000.vrapp.domain.models.LongTermWeatherInfo
import no.uio.ifi.in2000.vrapp.domain.models.Meta
import no.uio.ifi.in2000.vrapp.domain.models.Next24Hours
import no.uio.ifi.in2000.vrapp.domain.models.Next24HoursDetails
import no.uio.ifi.in2000.vrapp.domain.models.Units
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
class LongTermWeatherRepositoryTest {

    @MockK
    lateinit var dataSource: LongTermWeatherDataSource

    private lateinit var repository: LongTermWeatherRepository

    private val testLocation = Location("Oslo", 59.91f, 10.75f)

    private lateinit var testForecast: LongTermWeatherInfo

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val dummyDetails = Next24HoursDetails(
            airTemperatureMax = 20.0,
            airTemperatureMaxPercentile10 = 19.0,
            airTemperatureMaxPercentile90 = 21.0,
            airTemperatureMean = 18.0,
            airTemperatureMeanPercentile10 = 17.0,
            airTemperatureMeanPercentile90 = 19.5,
            airTemperatureMin = 10.0,
            airTemperatureMinPercentile10 = 9.0,
            airTemperatureMinPercentile90 = 11.0,
            precipitationAmount = 5.0,
            precipitationAmountPercentile10 = 4.0,
            precipitationAmountPercentile90 = 6.0,
            probabilityOfFrost = 10.0,
            probabilityOfHeavyPrecipitation = 20.0,
            probabilityOfPrecipitation = 50.0
        )

        testForecast = LongTermWeatherInfo(
            type = "Feature",
            geometry = Geometry(
                type = "Point",
                coordinates = listOf(10.75, 59.91)
            ),
            properties = LongTermProperties(
                meta = Meta(
                    updatedAt = "2025-05-20T00:00:00Z",
                    units = Units(
                        airTemperatureMax = "celsius",
                        airTemperatureMaxPercentile10 = "celsius",
                        airTemperatureMaxPercentile90 = "celsius",
                        airTemperatureMean = "celsius",
                        airTemperatureMeanPercentile10 = "celsius",
                        airTemperatureMeanPercentile90 = "celsius",
                        airTemperatureMin = "celsius",
                        airTemperatureMinPercentile10 = "celsius",
                        airTemperatureMinPercentile90 = "celsius",
                        precipitationAmount = "mm",
                        precipitationAmountPercentile10 = "mm",
                        precipitationAmountPercentile90 = "mm",
                        probabilityOfFrost = "%",
                        probabilityOfHeavyPrecipitation = "%",
                        probabilityOfPrecipitation = "%"
                    )
                ),
                timeseries = List(21) { index ->
                    LongTermTimeSeries(
                        time = "2025-05-${20 + index}T00:00:00Z",
                        data = LongTermTimeSeriesData(
                            next24Hours = Next24Hours(
                                details = dummyDetails
                            ),
                            next7Days = null
                        )
                    )
                }
            ),
            location = testLocation
        )

        coEvery { dataSource.getLongtermForecast(testLocation) } returns testForecast
        repository = LongTermWeatherRepository(dataSource)
    }


    @Test
    fun `contains valid weather details for each day`() = runTest {
        val result = repository.getLongtermWeatherData(testLocation)
        val timeseries = result?.properties?.timeseries ?: emptyList()

        timeseries.forEach { ts ->
            val details = ts.data.next24Hours?.details
            assertNotNull(details?.airTemperatureMax)
            assertNotNull(details?.airTemperatureMin)
            assertNotNull(details?.precipitationAmount)
            assertNotNull(details?.probabilityOfPrecipitation)
        }
    }

    @Test
    fun `forecast data should be in chronological order`() = runTest {
        val result = repository.getLongtermWeatherData(testLocation)
        val times = result?.properties?.timeseries?.map { it.time } ?: emptyList()
        assertEquals(times.sorted(), times)
    }

    @Test
    fun `precipitation probability should be in range 0 to 100`() = runTest {
        val result = repository.getLongtermWeatherData(testLocation)
        result?.properties?.timeseries?.forEach {
            val prob = it.data.next24Hours?.details?.probabilityOfPrecipitation
            if (prob != null) {
                assertTrue(prob in 0.0..100.0)
            }
        }
    }
}