package no.uio.ifi.in2000.vrapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import no.uio.ifi.in2000.vrapp.ui.theme.ColorMode

//Manages storage and retrieval of user preferences like theme mode
class PreferencesRepository(
    context: Context
) {
    private val dataStore = context.dataStore

    //Saves the selected theme mode to the data store
    suspend fun saveThemeMode(themeMode: ColorMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = themeMode.name
        }
    }
    //Retrieves the current theme mode from the data store
    suspend fun getThemeMode(): ColorMode {
        val preferences = dataStore.data.first()
        val themeName = preferences[THEME_MODE_KEY] ?: ColorMode.DYNAMIC.name
        return ColorMode.valueOf(themeName)
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val Context.dataStore by preferencesDataStore("user_preferences")
    }
}

