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
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapControls(
    modifier: Modifier = Modifier,
    mapView: MapView?,
    locationOverlay: MyLocationNewOverlay?
) {
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
                    onClick = { mapView?.controller?.zoomIn() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider()
                IconButton(
                    onClick = { mapView?.controller?.zoomOut() }
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
                locationOverlay?.myLocation?.let { location ->
                    mapView?.controller?.animateTo(location)
                    mapView?.controller?.setZoom(14.0)
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
