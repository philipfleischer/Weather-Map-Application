package no.uio.ifi.in2000.vrapp

import no.uio.ifi.in2000.vrapp.domain.models.MapWeatherApi
import no.uio.ifi.in2000.vrapp.ui.map.MapViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class MapViewModelTest {
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        viewModel = MapViewModel()
    }

    @Test
    fun `viewModel should be initialized correctly`() {
        assertNotNull(viewModel)
        assertTrue(viewModel.mapStyle.isNotEmpty())
        assertTrue(viewModel.weatherApiConfigs.isNotEmpty())
    }

    @Test
    fun `weatherApiConfigs should contain correct URLs`() {
        val expectedUrls = listOf(
            "https://beta.yr-maps.met.no/api/air-temperature/",
            "https://beta.yr-maps.met.no/api/cloud-area-fraction/",
            "https://beta.yr-maps.met.no/api/precipitation-amount/",
            "https://beta.yr-maps.met.no/api/precipitation-nowcast/",
            "https://beta.yr-maps.met.no/api/precipitation-observations/",
            "https://beta.yr-maps.met.no/api/wind/"
        )

        for ((i, config) in viewModel.weatherApiConfigs.withIndex()) {
            assertEquals(expectedUrls[i], config.baseUrl)
        }
    }

    @Test
    fun `camera position should be set to default if not restored`() {
        val cam = viewModel.cameraPosition
        cam.target?.let { assertEquals(59.91, it.latitude, 0.01) }
        cam.target?.let { assertEquals(10.75, it.longitude, 0.01) }
        assertEquals(5.0, cam.zoom, 0.01)
        assertEquals(0.0, cam.bearing, 0.01)
    }

    @Test
    fun `selectWeatherApi should update selectedWeatherApi when name exists`() {
        val mockApi = viewModel.weatherApiConfigs.first()
        val testApi = MapWeatherApi(
            name = mockApi.name,
            baseUrl = mockApi.baseUrl + "test.webp",
            maxZoom = mockApi.maxZoom,
            apiLayerProperties = mockApi.apiLayerProperties
        )

        viewModel.apply {
            availableWeatherApis = listOf(testApi)
        }

        viewModel.selectWeatherApi(mockApi.name)

        assertNotNull(viewModel.selectedWeatherApi)
        assertEquals(mockApi.name, viewModel.selectedWeatherApi?.name)
        assertEquals(testApi.baseUrl, viewModel.selectedWeatherApi?.baseUrl)
    }
}