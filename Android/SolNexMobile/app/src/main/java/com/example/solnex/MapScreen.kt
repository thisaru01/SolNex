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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    token: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }

    val repository = remember { StationRepository() }

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

        true
    }

    LaunchedEffect(token) {
        repository.getStations(token) { fetchedStations, errMsg ->
            stations = fetchedStations ?: emptyList()
            error = errMsg
            loading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null) {
            Text(
                text = error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else {
            // Remember the MapView so we can manage its lifecycle
            val mapViewRef = remember { mutableStateOf<MapView?>(null) }

            // Handle lifecycle
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> mapViewRef.value?.onResume()
                        Lifecycle.Event.ON_PAUSE -> mapViewRef.value?.onPause()
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
                            return baseUrl + "$zoom/$x/$y.png"
                        }
                    }
                    mapView.setTileSource(tileSource)

                    mapView.setMultiTouchControls(true)
                    mapView.setUseDataConnection(true)

                    // Center on Sri Lanka
                    mapView.controller.setZoom(7.5)
                    mapView.controller.setCenter(GeoPoint(7.8731, 80.7718))

                    // Add station markers with click listeners
                    stations.forEach { station ->
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(station.latitude, station.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = station.stationName
                        marker.snippet = "Capacity: ${station.capacityKw} kW • Status: ${station.status}"
                        marker.setOnMarkerClickListener { _, _ ->
                            selectedStation = station
                            true
                        }
                        mapView.overlays.add(marker)
                    }

                    mapViewRef.value = mapView
                    mapView.onResume()
                    
                    mapView
                },
                modifier = Modifier.fillMaxSize()
            )

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
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { selectedStation = null }) {
                                    Text("✕")
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
                            Button(
                                onClick = { /* TODO: Implement reservation submission */ },
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
    }
}
