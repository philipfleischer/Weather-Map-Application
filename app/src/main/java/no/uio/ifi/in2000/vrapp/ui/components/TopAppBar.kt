package no.uio.ifi.in2000.vrapp.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.uio.ifi.in2000.vrapp.ui.map.MapSettingsContent
import no.uio.ifi.in2000.vrapp.ui.theme.AppTheme
import no.uio.ifi.in2000.vrapp.ui.theme.ColorMode

//Universal Top app bar with diffrent Menu depending on screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTopAppBar(
    navController: NavController? = null,
    onFavoriteToggle: () -> Unit = {},
    onBackFromFavorites: () -> Unit = {},
    isFavorite: Boolean = false,
    showSearchBar: Boolean = true,
    showFavorites: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchBarClicked: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    // Make map-specific parameters optional
    hueRotate: Float = 0f,
    onHueRotateChange: (Float) -> Unit = {},
    isLocationUpdatesActive: Boolean = false,
    permissionGranted: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onStartLocationUpdates: () -> Unit = {},
    onStopLocationUpdates: () -> Unit = {},
) {


    Column {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet()) 60.dp else 0.dp),
                    horizontalArrangement = if (isTablet()) Arrangement.Center else Arrangement.Start
                ) {

                    if (showSearchBar) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChanged = onSearchQueryChanged,
                            onSearchBarClicked = onSearchBarClicked
                        )
                    }

                }
            },
            navigationIcon = {
                Row(
                    modifier = Modifier.padding(start = if (isTablet()) 40.dp else 0.dp),
                ) {
                    if (showFavorites) {
                        Box(
                            modifier = Modifier.semantics {
                                contentDescription = "Gå til forrige skjerm"
                            }
                        ) {
                            BackButton(onClick = onBackFromFavorites)
                        }
                    } else {
                        Box(
                            modifier = Modifier.semantics {
                                contentDescription = if (isFavorite) {
                                    "Fjern fra favoritter"
                                } else {
                                    "Legg til favoritt"
                                }
                            }
                        ) {
                            FavoriteButton(
                                isFavorite = isFavorite,
                                onClick = onFavoriteToggle
                            )
                        }
                    }

                }
            },
            actions = {
                Row(
                    modifier = Modifier.padding(end = if (isTablet()) 40.dp else 0.dp),
                ) {
                    SettingsMenu(
                        navController = navController,
                        hueRotate = hueRotate,
                        onHueRotateChange = onHueRotateChange,
                        isLocationUpdatesActive = isLocationUpdatesActive,
                        permissionGranted = permissionGranted,
                        onRequestPermission = onRequestPermission,
                        onStartLocationUpdates = onStartLocationUpdates,
                        onStopLocationUpdates = onStopLocationUpdates
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface.copy(alpha = 0.5f),
                scrolledContainerColor = colorScheme.surfaceColorAtElevation(2.dp)
            ),
            scrollBehavior = scrollBehavior
        )
    }
}
//MapScreen Menu
@Composable
fun SettingsMenu(
    navController: NavController?,
    // Make map-specific parameters optional
    hueRotate: Float = 0f,
    onHueRotateChange: (Float) -> Unit = {},
    isLocationUpdatesActive: Boolean = false,
    permissionGranted: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onStartLocationUpdates: () -> Unit = {},
    onStopLocationUpdates: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var securityExpanded by remember { mutableStateOf(false) }
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Innstillinger"
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = {
                menuExpanded = false
                themeExpanded = false
            },
            modifier = Modifier.padding(end = 4.dp)
        ) {
            // Always show these standard menu items
            StandardSettingsMenuItems(
                themeExpanded = themeExpanded,
                onThemeExpandedChange = { themeExpanded = it },
                onThemeSelected = { menuExpanded = false },
                securityExpanded = securityExpanded,
                onSecurityExpandedChange = { securityExpanded = it }

            )

            // Conditionally show map settings
            if (currentRoute == "map") {
                MapSettingsMenuItems(
                    hueRotate = hueRotate,
                    onHueRotateChange = onHueRotateChange,
                    isLocationUpdatesActive = isLocationUpdatesActive,
                    permissionGranted = permissionGranted,
                    onRequestPermission = onRequestPermission,
                    onStartLocationUpdates = onStartLocationUpdates,
                    onStopLocationUpdates = onStopLocationUpdates,
                    onDismiss = { menuExpanded = false }
                )
            }
        }
    }
}
//Homescreen and LongTermForecastScreen Menu
@Composable
private fun StandardSettingsMenuItems(
    themeExpanded: Boolean,
    onThemeExpandedChange: (Boolean) -> Unit,
    onThemeSelected: () -> Unit,
    securityExpanded: Boolean,
    onSecurityExpandedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Theme section
    DropdownMenuItem(
        text = {
            Text(
                "Tema: ${AppTheme.currentTheme.displayName}",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        onClick = { onThemeExpandedChange(!themeExpanded) },
        leadingIcon = {
            Icon(AppTheme.getIconForCurrentMode(), contentDescription = "Tema ikon")
        }
    )

    if (themeExpanded) {
        HorizontalDivider()

        ThemeMenuItem("Lys", Icons.Filled.WbSunny) {
            onThemeSelected()
            AppTheme.setThemeMode(ColorMode.LIGHT)
        }

        ThemeMenuItem("Mørk", Icons.Filled.NightsStay) {
            onThemeSelected()
            AppTheme.setThemeMode(ColorMode.DARK)
        }

        ThemeMenuItem("Dynamisk", Icons.Filled.AutoAwesome) {
            onThemeSelected()
            AppTheme.setThemeMode(ColorMode.DYNAMIC)
        }

        HorizontalDivider()
    }

    HorizontalDivider()

    // Security "Sikkerhet" section
    DropdownMenuItem(
        text = { Text("Sikkerhet", style = MaterialTheme.typography.bodyMedium) },
        onClick = { onSecurityExpandedChange(!securityExpanded) },
        leadingIcon = {
            Icon(Icons.Filled.Shield, contentDescription = "Skjold ikon")
        }
    )

    if (securityExpanded) {
        HorizontalDivider()

        ThemeMenuItem("Tillatelser", Icons.Filled.ManageAccounts) {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            } else {
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                }
            }.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Ultimate fallback
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }

        ThemeMenuItem("Databehandling", Icons.Filled.Info) {
            showDataDialog = true
        }

        if (showDataDialog) {
            AlertDialog(
                onDismissRequest = { showDataDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        showDataDialog = false
                    }) {
                        Text("Åpne innstillinger", style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDataDialog = false }) {
                        Text("Lukk", style = MaterialTheme.typography.titleMedium)
                    }
                },
                title = { Text("Databehandling") },
                text = {
                    Text(
                        "VAFF lagrer dine innstillinger (som favoritter og tema) lokalt på enheten. " +
                                "Ingen data deles med tredjepart fra appen direkte.\n\n" +
                                "Du kan nullstille lokale data ved å åpne appens innstillinger og slette lagring eller mellomlagring.\n\n" +
                                "*Merk*: Appes værdata, hentet fra MET, kan logge IP og posisjon. " +
                                "Dette skjer selv om appen ikke lagrer disse dataene. " +
                                "Se METs personvernerklæring for mer detaljer."
                    )
                }
            )
        }
    }
    HorizontalDivider()

    DropdownMenuItem(
        text = { Text("Om Oss", style = MaterialTheme.typography.bodyMedium) },
        onClick = { showAboutDialog = true },
        leadingIcon = {
            Icon(Icons.Filled.Code, contentDescription = "Kode ikon")
        }
    )

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Lukk", style = MaterialTheme.typography.titleMedium)
                }
            },
            title = { Text("Om VAFF") },
            text = {
                Text(
                    "VAFF står for - Vær-app for folket -, og er utviklet som et skoleprosjekt i emnet IN2000 ved Universitetet i Oslo (UiO).\n\n" +
                            "Appen henter værdata fra Meteorologisk institutt via IN2000-API-et. \n\n" +
                            "Versjon: 1.0.0"
                )
            }
        )
    }
}

//MapScreen settings, including gps and hue rotate
@Composable
private fun MapSettingsMenuItems(
    hueRotate: Float,
    onHueRotateChange: (Float) -> Unit,
    isLocationUpdatesActive: Boolean,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onStartLocationUpdates: () -> Unit,
    onStopLocationUpdates: () -> Unit,
    onDismiss: () -> Unit
) {
    HorizontalDivider()

    MapSettingsContent(
        hueRotate = hueRotate,
        onHueRotateChange = onHueRotateChange,
        isLocationUpdatesActive = isLocationUpdatesActive,
        permissionGranted = permissionGranted,
        onRequestPermission = onRequestPermission,
        onStartLocationUpdates = onStartLocationUpdates,
        onStopLocationUpdates = onStopLocationUpdates,
        onDismiss = onDismiss
    )
}

//Back button for navigation
@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Tilbakeknapp"
        )
    }
}
//Favorite toggle button
@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Favoritterknapp",
            tint = if (isFavorite) colorScheme.primary
            else colorScheme.onSurface
        )
    }
}

//Displays a single theme menu item with an icon and action
@Composable
private fun ThemeMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .padding(start = 20.dp)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            onClick = onClick,
            leadingIcon = { Icon(icon, contentDescription = null) }
        )
        HorizontalDivider()

    }

}
//Top app bar SearchBar
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchBarClicked: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var lastQuery by remember { mutableStateOf("") }




    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        TextField(
            value = query,
            placeholder = {
                if (query.isEmpty() && lastQuery.isNotEmpty()) {
                    Text(
                        text = lastQuery,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            onValueChange = { text -> onQueryChanged(text) },
            label = { Text("Søk etter sted") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(colorScheme.onSurfaceVariant, CircleShape)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        // Save the current query before clearing
                        if (query.isNotEmpty()) {
                            lastQuery = query
                        }
                        onQueryChanged("") // Clear on focus
                        onSearchBarClicked()
                    } else if (query.isEmpty()) {
                        focusManager.clearFocus()
                    }
                },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Søk")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onQueryChanged("")
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Tøm søk")
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            )
        )
    }
}


