package com.example.solnex

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Displays the interactive Map Screen using osmdroid
// Allows users to view stations, search, and center on their current location.
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    token: String,
    role: String = ""
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var showReservationForm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val repository = remember { StationRepository(context) }

    // Initialize osmdroid config ONCE before anything renders
    remember {
        val osmConfig = Configuration.getInstance()
        val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        osmConfig.load(context, prefs)
        osmConfig.userAgentValue = "SolNexMobile/1.0 (Android)"

        // Add HTTP headers to satisfy tile server requirements
        osmConfig.additionalHttpRequestProperties["Referer"] = "https://solnex.app"
        osmConfig.additionalHttpRequestProperties["Accept"] = "image/png,image/*;q=0.9,*/*;q=0.8"

        // Use app-internal cache directory
        val baseDir = java.io.File(context.cacheDir, "osmdroid")
        baseDir.mkdirs()
        osmConfig.osmdroidBasePath = baseDir
        val tileDir = java.io.File(baseDir, "tiles")
        tileDir.mkdirs()
        osmConfig.osmdroidTileCache = tileDir

        // Performance tuning
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
        favoriteIds = repository.getFavoriteStationIds()
        repository.getStations(token) { fetchedStations, errMsg ->
            stations = fetchedStations ?: emptyList()
            error = errMsg
            loading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Remember the MapView so we can manage its lifecycle
            val mapViewRef = remember { mutableStateOf<MapView?>(null) }
            val locationOverlayRef = remember { mutableStateOf<MyLocationNewOverlay?>(null) }

            // Handle lifecycle
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            mapViewRef.value?.onResume()
                            locationOverlayRef.value?.enableMyLocation()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            mapViewRef.value?.onPause()
                            locationOverlayRef.value?.disableMyLocation()
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    mapViewRef.value?.onDetach()
                }
            }

            AndroidView(
                factory = { ctx ->
                    val mapView = MapView(ctx)
                    
                    // Use German OSM mirror - more permissive than main server
                    val tileSource = object : XYTileSource(
                        "OSMGermany",
                        0, 19, 256, ".png",
                        arrayOf(
                            "https://tile.openstreetmap.de/"
                        )
                    ) {
                        override fun getTileURLString(pMapTileIndex: Long): String {
                            val zoom = MapTileIndex.getZoom(pMapTileIndex)
                            val x = MapTileIndex.getX(pMapTileIndex)
                            val y = MapTileIndex.getY(pMapTileIndex)
                            return "${baseUrl}$zoom/$x/$y.png"
                        }
                    }
                    mapView.setTileSource(tileSource)

                    mapView.setMultiTouchControls(true)
                    mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    mapView.setUseDataConnection(true)

                    // Initial Center on Sri Lanka
                    mapView.controller.setZoom(7.5)
                    mapView.controller.setCenter(GeoPoint(7.8731, 80.7718))

                    mapViewRef.value = mapView
                    mapView.onResume()
                    
                    mapView
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // Handle Location Overlay
                    if (locationPermissionGranted) {
                        var overlay = locationOverlayRef.value
                        if (overlay == null) {
                            val provider = GpsMyLocationProvider(mapView.context)
                            provider.addLocationSource(android.location.LocationManager.GPS_PROVIDER)
                            provider.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER)
                            
                            overlay = MyLocationNewOverlay(provider, mapView)
                            overlay.enableMyLocation()
                            
                            // Custom Blue Dot Icon
                            val iconSize = (16 * mapView.context.resources.displayMetrics.density).toInt()
                            val bitmap = androidx.core.graphics.createBitmap(iconSize, iconSize, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.parseColor("#4285F4") // Google Maps Blue
                                style = android.graphics.Paint.Style.FILL
                            }
                            val borderPaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.WHITE
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 2 * mapView.context.resources.displayMetrics.density
                            }
                            val radius = iconSize / 2f
                            canvas.drawCircle(radius, radius, radius - borderPaint.strokeWidth, paint)
                            canvas.drawCircle(radius, radius, radius - borderPaint.strokeWidth, borderPaint)
                            
                            overlay.setPersonIcon(bitmap)
                            overlay.setPersonAnchor(0.5f, 0.5f)
                            @Suppress("DEPRECATION")
                            overlay.setDirectionArrow(bitmap, bitmap)
                            overlay.setDirectionAnchor(0.5f, 0.5f)

                            // First time setup: zoom to user's location when fixed
                            overlay.runOnFirstFix {
                                mapView.post {
                                    mapView.controller.setZoom(14.0)
                                    mapView.controller.animateTo(overlay.myLocation)
                                }
                            }
                            locationOverlayRef.value = overlay
                        }
                        mapView.overlays.add(overlay)
                    }

                    // Add station markers with click listeners
                    val filteredStations = stations.filter { 
                        !it.status.equals("Deactivated", ignoreCase = true) &&
                        (!showFavoritesOnly || favoriteIds.contains(it.stationId))
                    }
                    filteredStations.forEach { station ->
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(station.latitude, station.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = station.stationName
                        marker.snippet = "Capacity: ${station.capacityKw} kW • Status: ${station.status}"
                        
                        if (favoriteIds.contains(station.stationId)) {
                            val defaultIcon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                            // Use MULTIPLY to preserve the pin's 3D shading, and make it Gold/Amber
                            defaultIcon?.setColorFilter(android.graphics.Color.parseColor("#FFB300"), android.graphics.PorterDuff.Mode.MULTIPLY)
                            marker.icon = defaultIcon
                        }

                        marker.setOnMarkerClickListener { _, _ ->
                            selectedStation = station
                            true
                        }
                        mapView.overlays.add(marker)
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Search Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it 
                        showSearchResults = it.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search stations...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Button(
                    onClick = { showFavoritesOnly = !showFavoritesOnly },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (showFavoritesOnly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorites",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Favorites Only")
                }

                AnimatedVisibility(
                    visible = showSearchResults && searchQuery.isNotBlank()
                ) {
                    val filtered = stations.filter { 
                        !it.status.equals("Deactivated", ignoreCase = true) && 
                        it.stationName.contains(searchQuery, ignoreCase = true) 
                    }
                    if (filtered.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(filtered) { station ->
                                    Text(
                                        text = station.stationName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchQuery = station.stationName
                                                showSearchResults = false
                                                selectedStation = station
                                                mapViewRef.value?.controller?.animateTo(
                                                    GeoPoint(station.latitude, station.longitude)
                                                )
                                                mapViewRef.value?.controller?.setZoom(16.0)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Controls Column (Zoom & FAB)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (selectedStation != null) 220.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Custom Zoom Controls
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.width(48.dp)) {
                        IconButton(
                            onClick = { mapViewRef.value?.controller?.zoomIn() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider()
                        IconButton(
                            onClick = { mapViewRef.value?.controller?.zoomOut() }
                        ) {
                            Text(
                                text = "−",
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
                
                // FAB to re-center on location
                FloatingActionButton(
                    onClick = {
                        locationOverlayRef.value?.myLocation?.let { location ->
                            mapViewRef.value?.controller?.animateTo(location)
                            mapViewRef.value?.controller?.setZoom(14.0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "My Location"
                    )
                }
            }

            // Bottom card for selected station
            AnimatedVisibility(
                visible = selectedStation != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                selectedStation?.let { station ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = station.stationName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    var isFav by remember(station.stationId) { mutableStateOf(repository.isFavorite(station.stationId)) }
                                    IconButton(onClick = {
                                        isFav = !isFav
                                        repository.toggleFavorite(station.stationId, isFav)
                                        favoriteIds = repository.getFavoriteStationIds()
                                    }) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { selectedStation = null }) {
                                        Text("✕")
                                    }
                                }
                            }
                            Text(
                                text = "ID: ${station.stationId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Capacity: ${station.capacityKw} kW  •  Status: ${station.status}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!role.equals("GridOperator", ignoreCase = true)) {
                                Button(
                                    onClick = { showReservationForm = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = station.status == "Active"
                                ) {
                                    Text("Submit Reservation")
                                }
                            }
                        }
                    }
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
            } else if (error != null && stations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Offline Mode: Showing cached data",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        if (showReservationForm && selectedStation != null) {
            ReservationFormDialog(
                stationId = selectedStation!!.stationId,
                stationName = selectedStation!!.stationName,
                onDismiss = { showReservationForm = false },
                onSuccess = {
                    showReservationForm = false
                    selectedStation = null
                    // Optional: show a success message or snackbar
                }
            )
        }
    }
}
