package no.uio.ifi.in2000.vrapp.tenUnitTests.repositories

import io.mockk.Runs
import no.uio.ifi.in2000.vrapp.data.favorite.FavoriteLocationDao
import no.uio.ifi.in2000.vrapp.data.favorite.FavoriteLocationRepository
import no.uio.ifi.in2000.vrapp.data.favorite.toEntity
import no.uio.ifi.in2000.vrapp.domain.models.Location
import org.junit.Test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class FavoriteLocationRepositoryTest {

    private lateinit var repository: FavoriteLocationRepository
    private lateinit var mockDao: FavoriteLocationDao

    private val testLocation = Location("Oslo", 59.91f, 10.75f)
    private val testEntity = testLocation.toEntity()

    @Before
    fun setup() {
        // Create a relaxed mock that returns empty flow by default
        mockDao = mockk(relaxed = true) {
            coEvery { getAllFavorites() } returns flowOf(emptyList())
        }
        repository = FavoriteLocationRepository(mockDao)
    }

    @Test
    fun `addFavorite returns false on failure`() = runBlocking {
        coEvery { mockDao.insert(any()) } throws RuntimeException()

        assertFalse(repository.addFavorite(testLocation))
    }


    @Test
    fun `isFavorite returns correct status`() = runBlocking {
        // Test when favorite exists
        coEvery { mockDao.getByCoordinates(any(), any()) } returns testEntity
        assertTrue(repository.isFavorite(testLocation))

        // Test when favorite doesn't exist
        coEvery { mockDao.getByCoordinates(any(), any()) } returns null
        assertFalse(repository.isFavorite(testLocation))
    }

    @Test
    fun `removeFavorite returns true on success`() = runBlocking {
        coEvery { mockDao.deleteByCoordinates(any(), any()) } just Runs

        assertTrue(repository.removeFavorite(testLocation))
        coVerify { mockDao.deleteByCoordinates(testLocation.latitude, testLocation.longitude) }
    }

    @Test
    fun `removeFavorite returns false on failure`() = runBlocking {
        coEvery { mockDao.deleteByCoordinates(any(), any()) } throws RuntimeException()

        assertFalse(repository.removeFavorite(testLocation))
    }

    @Test
    fun `toggleFavorite removes when already favorite`() = runBlocking {
        coEvery { mockDao.getByCoordinates(any(), any()) } returns testEntity
        coEvery { mockDao.deleteByCoordinates(any(), any()) } just Runs

        assertTrue(repository.toggleFavorite(testLocation))
        coVerify { mockDao.deleteByCoordinates(testLocation.latitude, testLocation.longitude) }
    }

    @Test
    fun `empty favorites flow emits empty list`() = runBlocking {
        // This is already the default behavior from setup()
        val result = repository.favorites.first()
        assertTrue(result.isEmpty())
    }
}