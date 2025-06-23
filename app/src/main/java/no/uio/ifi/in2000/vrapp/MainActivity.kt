package no.uio.ifi.in2000.vrapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.data.local.PreferencesRepository
import no.uio.ifi.in2000.vrapp.ui.theme.AppTheme
import no.uio.ifi.in2000.vrapp.ui.theme.VærAppTheme
import org.maplibre.android.MapLibre

// MainActivity for the app, initializing the UI and theme
class MainActivity : ComponentActivity() {

    //Initializes maplibre, UI and theme
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()
        val preferencesRepository = PreferencesRepository(applicationContext)
        lifecycleScope.launch {
            AppTheme.initialize(preferencesRepository)
        }
        setContent {
            VærAppTheme {
                val navController = rememberNavController()
                WeatherApp(navController = navController)
            }
        }
    }
}