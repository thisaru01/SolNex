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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

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
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(7.8731, 80.7718), 7.5f)
        }

        GoogleMapView(
            modifier = Modifier.fillMaxSize(),
            stations = stations,
            favoriteIds = favoriteIds,
            showFavoritesOnly = showFavoritesOnly,
            locationPermissionGranted = locationPermissionGranted,
            cameraPositionState = cameraPositionState,
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
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val loc = try { 
                    locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) ?: 
                    locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                } catch(e: SecurityException) { null }

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
            cameraPositionState = cameraPositionState
        )

        MapControls(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedStation != null || closestStations != null) 250.dp else 16.dp),
            cameraPositionState = cameraPositionState,
            context = context
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
                    cameraPositionState = cameraPositionState
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
