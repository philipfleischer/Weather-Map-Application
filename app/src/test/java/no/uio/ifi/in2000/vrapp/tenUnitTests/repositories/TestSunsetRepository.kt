package no.uio.ifi.in2000.vrapp.tenUnitTests.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.vrapp.data.api.sunset.SunriseSunsetDataSource
import no.uio.ifi.in2000.vrapp.data.api.sunset.SunriseSunsetRepository
import no.uio.ifi.in2000.vrapp.domain.models.Location
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SunriseSunsetRepositoryTest {

    private lateinit var dataSource: SunriseSunsetDataSource
    private lateinit var repository: SunriseSunsetRepository

    private val testLocation = Location("Oslo", 59.91f, 10.75f)
    private val expectedSunrise = "2025-05-16T04:30:00+02:00"
    private val expectedSunset = "2025-05-16T21:30:00+02:00"

    @Before
    fun setup() {
        dataSource = mockk()
        repository = SunriseSunsetRepository(dataSource)
    }

    @Test
    fun `getSunTimes fetches from dataSource and caches result`() = runBlocking {
        // Mock dataSource's getSunTimes to return expected values
        coEvery { dataSource.getSunTimes(testLocation) } returns Pair(expectedSunrise, expectedSunset)

        val (sunrise1, sunset1) = repository.getSunTimes(testLocation)

        // Verify returned values and that dataSource was called once
        assertEquals(expectedSunrise, sunrise1)
        assertEquals(expectedSunset, sunset1)
        coVerify(exactly = 1) { dataSource.getSunTimes(testLocation) }

        // Second call should use cached values (no new calls)
        val (sunrise2, sunset2) = repository.getSunTimes(testLocation)

        // Cached results returned
        assertEquals(expectedSunrise, sunrise2)
        assertEquals(expectedSunset, sunset2)

        // Verify dataSource was NOT called again
        coVerify(exactly = 1) { dataSource.getSunTimes(testLocation) }
    }
}
