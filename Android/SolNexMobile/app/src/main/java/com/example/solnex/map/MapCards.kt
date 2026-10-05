package com.example.solnex.map

import com.example.solnex.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch

@Composable
fun ClosestStationsCard(
    modifier: Modifier = Modifier,
    closestList: List<StationWithDistance>,
    onClose: () -> Unit,
    onStationSelected: (Station) -> Unit,
    cameraPositionState: CameraPositionState
) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    text = "Nearest Stations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF194D33)
                )
                TextButton(onClick = onClose) {
                    Text("✕")
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                itemsIndexed(closestList) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStationSelected(item.station)
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                            LatLng(item.station.latitude, item.station.longitude), 16f
                                        )
                                    )
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.station.stationName, fontWeight = FontWeight.Bold, color = Color(0xFF194D33))
                            Text(
                                text = "${item.station.capacityKw} kW • ${item.station.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format("%.1f km", item.distanceKm),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF194D33)
                        )
                    }
                    if (index < closestList.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedStationCard(
    modifier: Modifier = Modifier,
    station: Station,
    role: String,
    repository: StationRepository,
    token: String,
    onClose: () -> Unit,
    onFavoritesChanged: (Set<String>) -> Unit,
    onReserve: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    color = Color(0xFF194D33),
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var isFav by remember(station.stationId) { mutableStateOf(repository.isFavorite(station.stationId)) }
                    IconButton(onClick = {
                        isFav = !isFav
                        repository.toggleFavorite(station.stationId, isFav, token)
                        onFavoritesChanged(repository.getFavoriteStationIds())
                    }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color(0xFF194D33) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onClose) {
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
                    onClick = onReserve,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = station.status.equals("Active", ignoreCase = true),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF194D33),
                        contentColor = Color.White
                    )
                ) {
                    Text("Submit Reservation")
                }
            }
        }
    }
}
