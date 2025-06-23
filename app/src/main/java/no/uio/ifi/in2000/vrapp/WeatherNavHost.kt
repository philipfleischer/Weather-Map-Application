package no.uio.ifi.in2000.vrapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.uio.ifi.in2000.vrapp.ui.home.HomeScreen
import no.uio.ifi.in2000.vrapp.ui.home.HomeViewModel
import no.uio.ifi.in2000.vrapp.ui.longterm.LongTermForecastScreen
import no.uio.ifi.in2000.vrapp.ui.longterm.LongTermForecastViewModel
import no.uio.ifi.in2000.vrapp.ui.map.MapScreen
import no.uio.ifi.in2000.vrapp.ui.map.MapViewModel

//Sets up the navigation host for the apps screens
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val weatherUiState by homeViewModel.weatherUiState.collectAsState()

    val mapViewModel: MapViewModel = viewModel()
    val longTermForecastViewModel: LongTermForecastViewModel =
        viewModel(factory = LongTermForecastViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            AppEntry(navController)
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                weatherUiState = weatherUiState,
                viewModel = homeViewModel
            )
        }
        composable("map") {
            MapScreen(
                navController = navController,
                viewModel = mapViewModel,
                homeViewModel = homeViewModel,
            )
        }
        composable("longtermforecast") {
            LongTermForecastScreen(
                navController = navController,
                viewModel = longTermForecastViewModel,
                homeViewModel = homeViewModel,
            )
        }
    }
}
