package no.uio.ifi.in2000.vrapp.ui.map

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.vrapp.R
import no.uio.ifi.in2000.vrapp.WeatherApplication
import no.uio.ifi.in2000.vrapp.data.location.GPSManager
import no.uio.ifi.in2000.vrapp.domain.models.Location
import no.uio.ifi.in2000.vrapp.ui.components.BottomAppBarFun
import no.uio.ifi.in2000.vrapp.ui.components.LocalTopAppBar
import no.uio.ifi.in2000.vrapp.ui.home.HomeViewModel
import no.uio.ifi.in2000.vrapp.ui.theme.VærAppTheme
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel,
    onMapViewCreated: (MapView) -> Unit = {},
    homeViewModel: HomeViewModel
) {
    //Ui visibility states
    var showApis by rememberSaveable { mutableStateOf(false) }
    var showLegend by rememberSaveable { mutableStateOf(true) }
    var showInfoBox by rememberSaveable { mutableStateOf(false) }
    var showAnimationBar by rememberSaveable { mutableStateOf(true) }
    var showButtonColumn by rememberSaveable { mutableStateOf(true) }

    //Map and context states
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentMapApi by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    //Animation state from mapViewModel
    val isAnimating by viewModel.isAnimating.collectAsState()

    //Gps permissions and location states from gpsManager.
    val application = context.applicationContext as WeatherApplication
    val gpsManager = application.gpsManager
    val permissionGranted by gpsManager.permissionGranted.collectAsState()
    val currentLocation by gpsManager.currentLocation.collectAsState()
    val isLocationUpdatesActive by gpsManager.isLocationUpdatesActive.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        gpsManager.updatePermissionStatus()
        if (gpsManager.hasLocationPermission()) {
            gpsManager.startLocationUpdates()
        }
    }

    //Search and location related states from
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showSearchResults by viewModel.showSearchResults.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    //collecting the selectedApi state from viewmodel
    val selectedApi by remember { derivedStateOf { viewModel.selectedWeatherApi } }

    //Map markers and favorite states
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var selectedMarker by remember { mutableStateOf<Symbol?>(null) }
    val favorites by homeViewModel.favoritesWithWeather.collectAsState()
    val isFavorite by homeViewModel.isFavorite.collectAsState()
    val favoriteLocations = favorites.keys.toList()
    val favoriteMarkers = remember { mutableListOf<Symbol>() }
    val mapSymbolToLocation = remember { mutableMapOf<Long, Location>() }

    //UI state for searchbar
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // For Dynamic Colors
    val isDarkByTime = homeViewModel.isDarkMode.collectAsState().value
    val isDarkMode = remember { mutableStateOf(false) }
    LaunchedEffect(isDarkByTime) {
        isDarkMode.value = isDarkByTime
    }

    //Update map camera position on selectedLocation on selectedLocation change
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { cameraLocation ->
            mapLibreMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraLocation.latitude.toDouble(),
                        cameraLocation.longitude.toDouble()
                    ), 6.0
                )
            )
        }
    }

    //Activate locationComponent on map based on permissions and gps status
    LaunchedEffect(isLocationUpdatesActive, permissionGranted, mapLibreMap) {
        if (mapLibreMap != null && mapLibreMap?.style != null) {
            if (isLocationUpdatesActive && permissionGranted) {
                locationComponent(mapLibreMap!!, context, gpsManager, true)
            } else {
                locationComponent(mapLibreMap!!, context, gpsManager, false)
            }
        }
    }

    // Updates selected_map_marker on creation of symbolManager or selectedLocation change
    LaunchedEffect(selectedLocation, symbolManager) {
        symbolManager?.let { sm ->
            //Removing existing selectedMarker Symbol
            selectedMarker?.let { marker ->
                sm.delete(marker)
                selectedMarker = null
            }

            selectedLocation?.let { loc ->
                val latLng = LatLng(loc.latitude.toDouble(), loc.longitude.toDouble())
                val newMarker = sm.create(
                    SymbolOptions()
                        .withLatLng(latLng)

                        .withIconImage("selected_map_marker")
                        .withIconSize(0.1f)
                        .withIconAnchor("bottom")
                        .withIconOffset(arrayOf(0f, 5f))

                        .withTextField(loc.displayName)
                        .withTextSize(14f)
                        .withTextAnchor("bottom")
                        .withTextOffset(arrayOf(0f, -1.4f))
                )
                selectedMarker = newMarker
            }
        }
    }

    // Updates favorite map markers on creation of symbolManager, selectedLocation and favoriteLocations change
    LaunchedEffect(favoriteLocations, selectedLocation, symbolManager) {
        symbolManager?.let { sm ->
            //Removes all existing favorite map markers and its Location component
            favoriteMarkers.forEach { sm.delete(it) }
            favoriteMarkers.clear()
            mapSymbolToLocation.clear()

            //Show every favorite, except if favorite == selectedLocation
            val selected = selectedLocation
            val favoritesToDisplay = if (selected != null) {
                favoriteLocations.filter { fav ->
                    fav.latitude != selected.latitude || fav.longitude != selected.longitude
                }
            } else {
                favoriteLocations
            }

            favoritesToDisplay.forEach { location ->
                val symbol = sm.create(
                    SymbolOptions()
                        .withLatLng(
                            LatLng(
                                location.latitude.toDouble(),
                                location.longitude.toDouble()
                            )
                        )

                        .withIconImage("favorite_map_marker")
                        .withIconSize(0.08f)
                        .withIconAnchor("bottom")
                        .withIconOffset(arrayOf(0f, 5f))

                        .withTextField(location.displayName)
                        .withTextSize(14f)
                        .withTextAnchor("bottom")
                        .withTextOffset(arrayOf(0f, -1.3f))
                )
                //Storing location component of favorite marker in mapSymbolToLocation using the markers id
                mapSymbolToLocation[symbol.id] = location
                favoriteMarkers.add(symbol)
            }
        }
    }

    // Update map api layer on selectedWeatherApi change
    LaunchedEffect(selectedApi) {
        currentMapApi?.getMapAsync { map ->
            map.getStyle { style ->
                style.getLayer("api-layer")?.let { style.removeLayer(it) }
                style.getSource("api-source")?.let { style.removeSource(it) }

                val tileSet = TileSet("tiles", viewModel.selectedApiUrl).apply {
                    minZoom = 0f
                    maxZoom = viewModel.selectedApiMaxZoom
                }
                val apiSource = RasterSource("api-source", tileSet)
                style.addSource(apiSource)

                val rasterLayer = RasterLayer("api-layer", "api-source")
                    .withProperties(*viewModel.selectedApiLayerProperties)
                style.addLayer(rasterLayer)
            }
        }
    }

    //Handle MapView Life cyclce
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    //Clean up map markes on dispose
    DisposableEffect(Unit) {
        onDispose {
            symbolManager?.let { sm ->
                selectedMarker?.let { sm.delete(it) }
                favoriteMarkers.forEach { sm.delete(it) }
                favoriteMarkers.clear()
            }
        }
    }

    //Main UI composition
    VærAppTheme(
        isDarkByTime = isDarkByTime,
        dynamicColor = false
    ) {
        Scaffold(
            topBar = {
                Column {
                    LocalTopAppBar(
                        navController = navController,
                        isFavorite = isFavorite,
                        onFavoriteToggle = { homeViewModel.toggleFavorite() },
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { homeViewModel.searchLocation(it) },
                        // Map-specific parameters
                        hueRotate = viewModel.hueRotate,
                        onHueRotateChange = { viewModel.updateHueRotate(it) },
                        isLocationUpdatesActive = isLocationUpdatesActive,
                        permissionGranted = permissionGranted,
                        onRequestPermission = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onStartLocationUpdates = { gpsManager.startLocationUpdates() },
                        onStopLocationUpdates = { gpsManager.stopLocationUpdates() },
                    )
                }
            },
            bottomBar = {
                BottomAppBarFun(navController = navController)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                //Creating MapView and initialising first api layer, marker drawable, symbolManager, and both onclick listener
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            onMapViewCreated(this)
                            currentMapApi = this
                            getMapAsync { map ->
                                mapLibreMap = map
                                map.cameraPosition = viewModel.cameraPosition
                                map.addOnCameraIdleListener {
                                    viewModel.cameraPosition = map.cameraPosition
                                }

                                map.setStyle(viewModel.mapStyle) { style ->
                                    val tileSet = TileSet("tiles", viewModel.selectedApiUrl).apply {
                                        minZoom = 0f
                                        maxZoom = viewModel.selectedApiMaxZoom
                                    }
                                    val apiSource = RasterSource("api-source", tileSet)
                                    style.addSource(apiSource)

                                    val rasterLayer = RasterLayer("api-layer", "api-source")
                                        .withProperties(*viewModel.selectedApiLayerProperties)
                                    style.addLayer(rasterLayer)

                                    val favoriteDrawable = ContextCompat.getDrawable(
                                        ctx,
                                        R.drawable.favorite_map_marker
                                    )!!
                                    style.addImage("favorite_map_marker", favoriteDrawable)

                                    val selectedDrawable = ContextCompat.getDrawable(
                                        ctx,
                                        R.drawable.selected_map_marker
                                    )!!
                                    style.addImage("selected_map_marker", selectedDrawable)

                                    symbolManager = SymbolManager(this, map, style).apply {
                                        iconAllowOverlap = true
                                        textAllowOverlap = true
                                        addClickListener { symbol ->
                                            val location = mapSymbolToLocation[symbol.id]
                                            if (location != null) {
                                                viewModel.selectSearchResult(location)
                                            }
                                            true
                                        }
                                    }

                                    if (gpsManager.hasLocationPermission()) {
                                        locationComponent(map, ctx, gpsManager, true)
                                    }
                                }
                                map.addOnMapLongClickListener { latlng ->
                                    coroutineScope.launch {
                                        viewModel.searchNearestLocation(latlng)
                                    }
                                    true
                                }

                                map.uiSettings.isRotateGesturesEnabled = false
                                map.uiSettings.isLogoEnabled = false
                                map.uiSettings.isAttributionEnabled = false
                            }
                        }
                    },
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    //Searchbar search results and "din posisjon" button
                    if (showSearchResults) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .width(230.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .shadow(8.dp, RoundedCornerShape(8.dp))
                                .zIndex(1f)
                        ) {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .align(Alignment.TopStart)
                                            .clickable {
                                                //Checking permission, locationUpdates and if gps location has a value
                                                if (permissionGranted) {
                                                    if (isLocationUpdatesActive && currentLocation != null) {
                                                        viewModel.searchNearestLocation(
                                                            currentLocation!!
                                                        )
                                                    } else {
                                                        gpsManager.startLocationUpdates()
                                                    }
                                                } else {
                                                    permissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                }
                                            }.semantics {
                                                contentDescription = if (permissionGranted && isLocationUpdatesActive && currentLocation != null) {
                                                    "Søk etter nærmeste plassering"
                                                } else {
                                                    "Aktiver gps før du kan søke etter nærmeste plassering"
                                                }
                                            },

                                        ) {
                                        Text(
                                            text = "Din Posisjon",
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .padding(4.dp)
                                        )
                                    }
                                }
                                if (isSearching) {
                                    item { Text("Laster Lokasjoner") }
                                } else {
                                    items(searchResults) { location ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectSearchResult(location)
                                                    keyboardController?.hide()
                                                    focusManager.clearFocus()
                                                }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.LocationOn,
                                                contentDescription = "Lokasjons Ikon",
                                            )
                                            Text(
                                                text = location.name ?: "Laster Lokasjons navn",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //Show MapApiInfo Box
                    if (showInfoBox) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .then(
                                    //placement/size reacts to other ui visibility states (buttoncolumn and animationbar)
                                    if (showAnimationBar) {
                                        Modifier
                                            .offset(y = (-78).dp)
                                            .padding(4.dp, end = 58.dp, top = 162.dp)
                                    } else {
                                        if (showButtonColumn) {
                                            Modifier
                                                .offset(y = (-30).dp)
                                                .padding(4.dp, end = 58.dp, top = 114.dp)
                                        } else {
                                            Modifier
                                                .offset(y = (-30).dp)
                                                .padding(4.dp, end = 4.dp, top = 114.dp)
                                        }

                                    }
                                )
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                                .zIndex(0.5f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedApi?.name
                                            ?: "Laster Api navn",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { showInfoBox = false }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Lukk Informasjons Boksen",
                                            tint = Color.DarkGray
                                        )
                                    }
                                }
                                LazyColumn {
                                    item {
                                        mapApiInfo(
                                            selectedApi?.name,
                                            viewModel
                                        )
                                    }
                                }
                            }
                        }
                    }

                    //Api selection Box, expanding on click to show available apis
                    Column(
                        modifier = Modifier
                            .then(
                                if (showAnimationBar) {
                                    Modifier.padding(start = 4.dp, top = 4.dp, bottom = 78.dp)
                                } else {
                                    Modifier.padding(start = 4.dp, top = 4.dp, bottom = 30.dp)
                                }
                            )
                            .align(Alignment.TopStart)
                            .width(200.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(16.dp)
                            )
                            .zIndex(0.9f)
                    ) {
                        TextField(
                            value = selectedApi?.name ?: "Laster API...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth(),
                            //Textfield wont allow clickable without enable being false, this is a workaround.
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect {
                                            if (it is PressInteraction.Release) {
                                                showApis = !showApis
                                            }
                                        }
                                    }
                                },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (selectedApi?.name) {
                                        "Temperatur" -> Icons.Filled.Thermostat
                                        "Skyer" -> Icons.Filled.Cloud
                                        "Nedbør radar" -> Icons.Filled.WaterDrop
                                        "Nedbør langtids" -> Icons.Filled.WaterDrop
                                        "Vind" -> Icons.Filled.Air
                                        else -> Icons.Filled.ErrorOutline
                                    },
                                    contentDescription = "API-ikon for ${selectedApi?.name}"
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showApis) },
                            label = { Text("Velg API") },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 18.sp, textAlign = TextAlign.Start),
                            shape = RoundedCornerShape(16.dp)
                        )
                        //expanded on click
                        if (showApis) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                                verticalArrangement = Arrangement.Top
                            ) {
                                viewModel.availableWeatherApis.forEach { api ->
                                    if (api.name != selectedApi?.name) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.selectWeatherApi(api.name)
                                                        showApis = false
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = painterResource(
                                                        when (api.name) {
                                                            "Temperatur" -> R.drawable.temperature_img
                                                            "Skyer" -> R.drawable.cloud_img
                                                            "Nedbør radar" -> R.drawable.rain_img2
                                                            "Nedbør langtids" -> R.drawable.rain_img1
                                                            "Vind" -> R.drawable.wind_img
                                                            else -> R.drawable.wind_img
                                                        }
                                                    ),
                                                    contentDescription = "Bilde av ${api.name}",
                                                    modifier = Modifier
                                                        .height(40.dp)
                                                        .padding(bottom = 4.dp, top = 4.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = api.name,
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                            }
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                            }
                        }
                    }

                    //Button lazycolumn on the Bottom-End, collapsable.
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.BottomEnd)
                            .then(
                                //Placement reacts to Animation Bar
                                if (!showAnimationBar) {
                                    if (!showButtonColumn) {
                                        Modifier.padding(start = 4.dp, end = 4.dp)
                                    } else {
                                        Modifier.padding(4.dp)
                                    }
                                } else {
                                    Modifier
                                        .offset(y = (-73).dp)
                                        .padding(4.dp)
                                }
                            )
                            .background(Color.Transparent)
                    ) {
                        LazyColumn(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.End,
                        ) {
                            if (showButtonColumn) {
                                item {
                                    if (showAnimationBar) {
                                        Spacer(modifier = Modifier.height(77.dp))
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .then(
                                            if (!showAnimationBar && !showButtonColumn) {
                                                Modifier.background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp
                                                    )
                                                )
                                            } else {
                                                Modifier.background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(16.dp)
                                                )
                                            }
                                        )
                                        .clickable { showButtonColumn = !showButtonColumn }
                                ) {
                                    if (showButtonColumn) {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            contentDescription = "Kollaps Knapp kolonne",
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.ArrowDropUp,
                                            contentDescription = "Utvid Knapp kolonne",
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }

                                }
                            }
                            //expanded:
                            if (showButtonColumn) {
                                //focus camera on active gps location.
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            ),
                                        onClick = {
                                            //Checking permission, starting locationupdates and building new camera pos
                                            if (permissionGranted) {
                                                gpsManager.startLocationUpdates()
                                                currentLocation?.let { cameraLocation ->
                                                    mapLibreMap?.animateCamera(
                                                        CameraUpdateFactory.newLatLngZoom(
                                                            cameraLocation,
                                                            8.0
                                                        )
                                                    )
                                                }
                                            } else {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        }
                                    ) {
                                        if (isLocationUpdatesActive) {
                                            Icon(
                                                Icons.Filled.MyLocation,
                                                contentDescription = "Fokuser på din posisjon",
                                                Modifier.size(30.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.LocationSearching,
                                                contentDescription = "Aktiver gps og fokuser på din posisjon",
                                                Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }
                                //Zoom in button
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            ),
                                        onClick = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomIn()) }
                                    ) {
                                        Icon(
                                            Icons.Filled.ZoomIn,
                                            contentDescription = "Zoomer inn",
                                            Modifier.size(30.dp)
                                        )
                                    }
                                }
                                //Zoom out button
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            ),
                                        onClick = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomOut()) }
                                    ) {
                                        Icon(
                                            Icons.Filled.ZoomOut,
                                            contentDescription = "Zoomer ut",
                                            Modifier.size(30.dp)
                                        )
                                    }
                                }
                                //Show or hide MapApiInfo box
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            ),
                                        onClick = { showInfoBox = !showInfoBox }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Info,
                                            contentDescription = "Info om Apiet",
                                            Modifier.size(30.dp)
                                        )
                                    }
                                }
                                //Collapsable maplegend, showing api color representation
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clickable { showLegend = !showLegend }
                                            .semantics {
                                                contentDescription = if (showLegend) {
                                                    "Skjul legende"
                                                } else {
                                                    "Vis legende"
                                                }
                                            }
                                    ) {
                                        val (legendColors, legendUnit, legendIcon, legendBackground) = viewModel.mapLegend()

                                        Column(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .width(48.dp)
                                                .padding(
                                                    start = 4.dp,
                                                    end = 4.dp,
                                                    top = 4.dp,
                                                    bottom = 8.dp
                                                ),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            if (showLegend) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropDown,
                                                    contentDescription = "Kollaps farge beskrivelse",
                                                    modifier = Modifier
                                                        .height(20.dp)
                                                        .align(Alignment.CenterHorizontally)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDropUp,
                                                    contentDescription = "Utvid farge beskrivelse",
                                                    modifier = Modifier
                                                        .height(20.dp)
                                                        .align(Alignment.CenterHorizontally)
                                                )
                                            }

                                            Icon(
                                                imageVector = legendIcon,
                                                contentDescription = "Enhets ikon for apiet",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                text = legendUnit,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                            )

                                            if (showLegend) {
                                                legendColors.forEach { pair: Pair<Color, String> ->
                                                    val (color, unit) = pair
                                                    //"Skyer" is represented through opacity, so should maplegend
                                                    val opacity: Float =
                                                        if (selectedApi?.name == "Skyer") {
                                                            unit.toFloat() / 128f
                                                        } else {
                                                            1f
                                                        }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .then(
                                                                    if (legendBackground != null) {
                                                                        Modifier.background(legendBackground)
                                                                    } else {
                                                                        Modifier
                                                                    }
                                                                )
                                                                .background(color.copy(alpha = opacity))
                                                                .width(8.dp)
                                                                .height(24.dp)
                                                        )
                                                        Text(
                                                            unit,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //Animation bar at the bottom, collapsable
                    if (viewModel.availableTimes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .then(
                                    if (showAnimationBar) {
                                        Modifier
                                            .padding(4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(
                                                start = 4.dp,
                                                top = 2.dp,
                                                end = 4.dp,
                                                bottom = 2.dp
                                            )
                                            .height(66.dp)

                                    } else {
                                        Modifier
                                            .padding(start = 4.dp, top = 4.dp, end = 58.dp)//58
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                            )
                                            .padding(start = 4.dp, top = 2.dp, end = 4.dp)
                                    }
                                )
                        ) {
                            //expanded:
                            if (showAnimationBar) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .weight(1f),
                                    ) {
                                        IconButton(
                                            modifier = Modifier
                                                .align(Alignment.Bottom)
                                                .padding(top = 4.dp),
                                            onClick = {
                                                if (viewModel.selectedTimeIndex > 0)
                                                    viewModel.updateSelectedTime(viewModel.selectedTimeIndex - 1)
                                            },
                                            enabled = viewModel.selectedTimeIndex > 0
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Forrige gyldigde klokkeslett",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.9f
                                                )

                                            )
                                        }

                                        Slider(
                                            value = viewModel.selectedTimeIndex.toFloat(),
                                            onValueChange = { newValue ->
                                                viewModel.updateSelectedTime(newValue.toInt())
                                            },
                                            valueRange = 0f..(viewModel.availableTimes.size - 1).toFloat(),
                                            steps = viewModel.availableTimes.size - 2,
                                            modifier = Modifier
                                                .align(Alignment.Bottom)
                                                .padding(top = 4.dp)
                                                .weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.9f
                                                ),
                                                activeTrackColor = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.9f
                                                ),
                                                activeTickColor = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.6f
                                                )
                                            )
                                        )

                                        IconButton(
                                            modifier = Modifier
                                                .align(Alignment.Bottom)
                                                .padding(top = 4.dp),
                                            onClick = {
                                                if (viewModel.selectedTimeIndex < viewModel.availableTimes.size - 1) {
                                                    viewModel.updateSelectedTime(viewModel.selectedTimeIndex + 1)
                                                }
                                            },
                                            enabled = viewModel.selectedTimeIndex < viewModel.availableTimes.size - 1
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Neste gyldige klokkeslett",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.9f
                                                )

                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(
                                                top = 3.dp,
                                                end = 2.dp,
                                                start = 2.dp,
                                                bottom = 2.dp
                                            )
                                    ) {
                                        if (viewModel.selectedTimeIndex < viewModel.availableTimes.size - 1) {
                                            IconButton(
                                                onClick = { viewModel.toggleAnimation() },
                                                modifier = Modifier.align(Alignment.Center)
                                            ) {
                                                if (isAnimating) {
                                                    Icon(
                                                        Icons.Filled.Pause,
                                                        contentDescription = "Start animering",
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.9f
                                                        )
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Filled.PlayArrow,
                                                        contentDescription = "Stop animering",
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.9f
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { viewModel.restartAnimation() },
                                                modifier = Modifier.align(Alignment.Center)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Repeat,
                                                    contentDescription = "Restart animering fra første tid",
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.9f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            //Date, time and expand button row for animation bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                        then (
                                        if (!showAnimationBar) {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                            )
                                        } else {
                                            Modifier
                                                .padding(start = 0.dp, end = 56.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp
                                                    )
                                                )
                                        }
                                        ),
                            ) {
                                Text(
                                    text = try {
                                        viewModel.zuluToCEST(viewModel.availableTimes[viewModel.selectedTimeIndex])
                                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                    } catch (e: Exception) {
                                        "Ugyldig Dato"
                                    },
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold, // Overwrite
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)

                                )
                                Text(
                                    text = try {
                                        viewModel.zuluToCEST(viewModel.availableTimes[viewModel.selectedTimeIndex])
                                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                                    } catch (e: Exception) {
                                        "Ugyldig tid"
                                    },
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)

                                )
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .weight(1f)
                                        .clickable { showAnimationBar = !showAnimationBar }
                                ) {
                                    if (showAnimationBar) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Kollaps",
                                            Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropUp,
                                            contentDescription = "Expander",
                                            Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//Helper function to activate LocationComponent and start locationUppdates
private fun locationComponent(
    map: MapLibreMap,
    context: Context,
    gpsManager: GPSManager,
    status: Boolean
) {
    if (!gpsManager.hasLocationPermission()) {
        return
    }
    try {
        val locationComponent = map.locationComponent
        val style = map.style ?: return
        val activationOptions = LocationComponentActivationOptions.builder(context, style)
            .build()

        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = status
    } catch (e: SecurityException) {
        Log.e("locationComponent", "SecurityException: $e")
    }
}
