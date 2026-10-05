package com.example.solnex.map

import com.example.solnex.*

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun MapControls(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.End
    ) {
        // Custom Zoom Controls
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.width(48.dp)) {
                IconButton(
                    onClick = { 
                        coroutineScope.launch {
                            val currentZoom = cameraPositionState.position.zoom
                            cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.zoomTo(currentZoom + 1f))
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider()
                IconButton(
                    onClick = { 
                        coroutineScope.launch {
                            val currentZoom = cameraPositionState.position.zoom
                            cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.zoomTo(currentZoom - 1f))
                        }
                    }
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
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val loc = try { 
                    locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) ?: 
                    locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                } catch(e: SecurityException) { null }
                
                if (loc != null) {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                LatLng(loc.latitude, loc.longitude), 14f
                            )
                        )
                    }
                }
            },
            containerColor = Color.White,
            contentColor = Color(0xFF194D33)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "My Location"
            )
        }
    }
}
