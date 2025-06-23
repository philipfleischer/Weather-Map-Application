package no.uio.ifi.in2000.vrapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.uio.ifi.in2000.vrapp.R

//Universal Bottom app bar
@Composable
fun BottomAppBarFun(navController: NavController) {
    // Get the current route from the NavController on the stack
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    // State to track the selected icon, so that it does not change to home (a slight problem i had where it changed automatically)
    var selectedIcon by remember { mutableStateOf(currentRoute ?: "home") }

    // Update the selectedIcon when the route changes, using coroutines
    LaunchedEffect(currentRoute) {
        selectedIcon = currentRoute ?: "home"
    }



    BottomAppBar(
        modifier = Modifier
            .height(60.dp)
            .fillMaxSize()
            .fillMaxWidth(),
        containerColor = colorScheme.primaryContainer.copy(alpha = 0.9f),
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
        IconButton(
            onClick = {
                navController.navigate("map") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Naviger til kartskjerm" }

        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = "Kart",
                modifier = Modifier.size(70.dp),
                tint = if (currentRoute == "map")
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = {
                navController.navigate("home") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Naviger til hjemmeskjerm" }

        ) {
            Icon(
                painter = painterResource(id = R.drawable.sunny),
                contentDescription = "Værtabell",
                modifier = Modifier.size(70.dp),
                tint = if (currentRoute == "home")
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

            )
        }

        IconButton(
            onClick = {
                navController.navigate("longtermforecast") {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Naviger til Langtidsværvarselskjerm" }

        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Langtidsværvarsel",
                modifier = Modifier.size(70.dp),
                tint = if (currentRoute == "longtermforecast")
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(0.2f))
    }
}
