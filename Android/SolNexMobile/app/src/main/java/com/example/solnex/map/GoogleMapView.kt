package com.example.solnex.map

import com.example.solnex.*

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    stations: List<Station>,
    favoriteIds: Set<String>,
    showFavoritesOnly: Boolean,
    locationPermissionGranted: Boolean,
    cameraPositionState: CameraPositionState,
    onStationSelected: (Station) -> Unit
) {
    val mapProperties = MapProperties(
        isMyLocationEnabled = locationPermissionGranted
    )
    val mapUiSettings = MapUiSettings(
        myLocationButtonEnabled = false,
        zoomControlsEnabled = false,
        mapToolbarEnabled = false
    )

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        val filteredStations = stations.filter { 
            it.status.equals("Active", ignoreCase = true) &&
            (!showFavoritesOnly || favoriteIds.contains(it.stationId))
        }
        
        filteredStations.forEach { station ->
            val isFavorite = favoriteIds.contains(station.stationId)
            val hue = if (isFavorite) BitmapDescriptorFactory.HUE_ORANGE else BitmapDescriptorFactory.HUE_RED
            
            Marker(
                state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                title = station.stationName,
                snippet = "Capacity: ${station.capacityKw} kW • Status: ${station.status}",
                icon = BitmapDescriptorFactory.defaultMarker(hue),
                onClick = {
                    onStationSelected(station)
                    true
                }
            )
        }
    }
}
