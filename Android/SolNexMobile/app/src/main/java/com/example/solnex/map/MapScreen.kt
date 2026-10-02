package com.example.solnex.map

import com.example.solnex.*

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    token: String,
    role: String = "",
    onNavigateToTab: (BottomNavItem) -> Unit = {}
) {
    val context = LocalContext.current

    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var showReservationForm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var closestStations by remember { mutableStateOf<List<StationWithDistance>?>(null) }
    var fetchingClosest by remember { mutableStateOf(false) }

    val repository = remember { StationRepository(context) }

    // Initialize osmdroid config ONCE before anything renders
    remember {
        val osmConfig = Configuration.getInstance()
        val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        osmConfig.load(context, prefs)
        osmConfig.userAgentValue = "SolNexMobile/1.0 (Android)"

        osmConfig.additionalHttpRequestProperties["Referer"] = "https://solnex.app"
        osmConfig.additionalHttpRequestProperties["Accept"] = "image/png,image/*;q=0.9,*/*;q=0.8"

        val baseDir = java.io.File(context.cacheDir, "osmdroid")
        baseDir.mkdirs()
        osmConfig.osmdroidBasePath = baseDir
        val tileDir = java.io.File(baseDir, "tiles")
        tileDir.mkdirs()
        osmConfig.osmdroidTileCache = tileDir

        osmConfig.tileFileSystemThreads = 4
        osmConfig.tileDownloadThreads = 4
        osmConfig.tileFileSystemCacheMaxBytes = 100L * 1024 * 1024 // 100 MB cache
        
        true
    }

    var locationPermissionGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(token) {
        repository.syncFavoritesFromServer(token) {
            favoriteIds = repository.getFavoriteStationIds()
            repository.getStations(token) { fetchedStations, errMsg ->
                stations = fetchedStations ?: emptyList()
                error = errMsg
                loading = false
            }
        }
    }

    LaunchedEffect(error, stations.size) {
        if (error != null && stations.isNotEmpty()) {
            while (true) {
                delay(15_000L)
                repository.getStations(token) { fetchedStations, errMsg ->
                    if (errMsg == null && fetchedStations != null) {
                        stations = fetchedStations
                        error = null   // clears the banner
                    }
                }
                if (error == null) break
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val mapViewRef = remember { mutableStateOf<MapView?>(null) }
        val locationOverlayRef = remember { mutableStateOf<MyLocationNewOverlay?>(null) }

        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            stations = stations,
            favoriteIds = favoriteIds,
            showFavoritesOnly = showFavoritesOnly,
            locationPermissionGranted = locationPermissionGranted,
            mapViewRef = mapViewRef,
            locationOverlayRef = locationOverlayRef,
            onStationSelected = { station -> selectedStation = station }
        )

        MapSearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showSearchResults = showSearchResults,
            onShowSearchResultsChange = { showSearchResults = it },
            showFavoritesOnly = showFavoritesOnly,
            onShowFavoritesOnlyChange = { showFavoritesOnly = it },
            fetchingClosest = fetchingClosest,
            onFetchClosest = {
                val loc = locationOverlayRef.value?.lastFix
                if (loc != null) {
                    fetchingClosest = true
                    repository.getClosestStations(token, loc.latitude, loc.longitude, 3) { result, err ->
                        fetchingClosest = false
                        if (result != null) {
                            closestStations = result
                            selectedStation = null
                        } else {
                            error = err
                        }
                    }
                } else {
                    error = "Current location not available"
                }
            },
            error = error,
            stations = stations,
            onStationSelected = { station ->
                selectedStation = station
                closestStations = null
            },
            mapView = mapViewRef.value
        )

        MapControls(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedStation != null || closestStations != null) 250.dp else 16.dp),
            mapView = mapViewRef.value,
            locationOverlay = locationOverlayRef.value
        )

        AnimatedVisibility(
            visible = closestStations != null && selectedStation == null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            closestStations?.let { closestList ->
                ClosestStationsCard(
                    closestList = closestList,
                    onClose = { closestStations = null },
                    onStationSelected = { station -> 
                        selectedStation = station 
                        closestStations = null
                    },
                    mapView = mapViewRef.value
                )
            }
        }

        AnimatedVisibility(
            visible = selectedStation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedStation?.let { station ->
                SelectedStationCard(
                    station = station,
                    role = role,
                    repository = repository,
                    token = token,
                    onClose = { selectedStation = null },
                    onFavoritesChanged = { favoriteIds = it },
                    onReserve = { showReservationForm = true }
                )
            }
        }

        if (loading && stations.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null && stations.isEmpty()) {
            Text(
                text = error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        }

        if (showReservationForm && selectedStation != null) {
            ReservationFormDialog(
                stationId = selectedStation!!.stationId,
                stationName = selectedStation!!.stationName,
                onDismiss = { showReservationForm = false },
                onSuccess = { navTarget ->
                    showReservationForm = false
                    selectedStation = null
                    if (navTarget != null) {
                        onNavigateToTab(navTarget)
                    }
                }
            )
        }
    }
}
