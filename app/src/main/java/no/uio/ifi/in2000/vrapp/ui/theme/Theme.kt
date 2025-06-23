package no.uio.ifi.in2000.vrapp.ui.theme

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.data.local.PreferencesRepository

//Available theme modes with their display names
enum class ColorMode(val displayName: String) {
    LIGHT("Lys"),
    DARK("Mørk"),
    DYNAMIC("Dynamisk")
}
//Apps theme state and preferences
object AppTheme {
    private lateinit var preferencesRepository: PreferencesRepository
    private var _currentTheme by mutableStateOf(ColorMode.DYNAMIC)

    val currentTheme: ColorMode
        get() = _currentTheme

    suspend fun initialize(repository: PreferencesRepository) {
        preferencesRepository = repository
        _currentTheme = preferencesRepository.getThemeMode()
    }

    fun setThemeMode(value: ColorMode) {
        _currentTheme = value
        // Launch a coroutine to save the preference
        CoroutineScope(Dispatchers.IO).launch {
            preferencesRepository.saveThemeMode(value)
        }
    }

    fun getIconForCurrentMode() = when (currentTheme) {
        ColorMode.LIGHT -> Icons.Filled.WbSunny
        ColorMode.DARK -> Icons.Filled.NightsStay
        ColorMode.DYNAMIC -> Icons.Filled.AutoAwesome
    }
}

//Applies the theme based on the current mode and dynamic color settings
@Composable
fun VærAppTheme(
    isDarkByTime: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (AppTheme.currentTheme) {
        ColorMode.LIGHT -> false
        ColorMode.DARK -> true
        ColorMode.DYNAMIC -> isDarkByTime
    }

    val useDynamicColors = AppTheme.currentTheme == ColorMode.DYNAMIC &&
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColors -> {
            val dynamicScheme = if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
            dynamicScheme
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}