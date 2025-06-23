package no.uio.ifi.in2000.vrapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.vrapp.ui.home.HomeViewModel

//Sets up the apps navigation host with the home view model
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherApp(navController: NavHostController) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

    WeatherNavHost(
        navController = navController,
        modifier = Modifier,
        homeViewModel = homeViewModel,
    )
}

//Loading Screen
@Composable
fun SplashScreen(loadingProgress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.logotittel),
                contentDescription = "App Logo",
                modifier = Modifier.size(300.dp)
            )


            LinearProgressIndicator(
                progress = { loadingProgress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

//Manages loadingscreen and navigating to homeScreen
@Composable
fun AppEntry(navController: NavHostController) {
    var progress by remember { mutableFloatStateOf(0f) }

    //Dosent preload homeScreen, would be included in further development
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(30)
            progress += 0.1f
        }


        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    SplashScreen(loadingProgress = progress)
}
