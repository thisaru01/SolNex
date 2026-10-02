package com.example.solnex.map

import com.example.solnex.*

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    stations: List<Station>,
    favoriteIds: Set<String>,
    showFavoritesOnly: Boolean,
    locationPermissionGranted: Boolean,
    mapViewRef: MutableState<MapView?>,
    locationOverlayRef: MutableState<MyLocationNewOverlay?>,
    onStationSelected: (Station) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
                it.status.equals("Active", ignoreCase = true) &&
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
                    onStationSelected(station)
                    true
                }
                mapView.overlays.add(marker)
            }

            mapView.invalidate()
        },
        modifier = modifier
    )
}
