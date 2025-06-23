package no.uio.ifi.in2000.vrapp

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.vrapp.data.api.map.MapDataSource
import no.uio.ifi.in2000.vrapp.data.api.map.MapRepository
import no.uio.ifi.in2000.vrapp.domain.models.TimedLinks
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapRepositoryTest {
    private lateinit var mapDataSource: MapDataSource
    private lateinit var mapRepository: MapRepository

    @Before
    fun setup() {
        mapDataSource = mockk()
        mapRepository = MapRepository(mapDataSource)
    }

    @Test
    fun `getAvailableWeatherApis returns data from dataSource`() = runBlocking {
        val dummyUrl = "https://example.com/api"
        val expected = listOf(
            TimedLinks(time = "2024-01-01T12:00:00Z", pngUrl = "url1.png"),
            TimedLinks(time = "2024-01-01T13:00:00Z", pngUrl = "url2.png")
        )
        coEvery { mapDataSource.getAvailableAPI(dummyUrl) } returns expected
        val result = mapRepository.getAvailableWeatherApis(dummyUrl)
        assertEquals(expected, result)
    }

    @Test
    fun `getAvailableWeatherApis returns emptyList on exception`() = runBlocking {
        val dummyUrl = "https://example.com/api"
        coEvery { mapDataSource.getAvailableAPI(dummyUrl) } throws Exception("Network error")
        val result = mapRepository.getAvailableWeatherApis(dummyUrl)
        assertEquals(emptyList<TimedLinks>(), result)
    }

    @Test
    fun `getMapStyle returns value from dataSource`() {
        val expectedStyle = "custom-map-style"
        every { mapDataSource.getMapStyle() } returns expectedStyle
        val result = mapRepository.getMapStyle()
        assertEquals(expectedStyle, result)
    }
}