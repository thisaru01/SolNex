package com.example.solnex.map

import com.example.solnex.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun MapSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearchResults: Boolean,
    onShowSearchResultsChange: (Boolean) -> Unit,
    showFavoritesOnly: Boolean,
    onShowFavoritesOnlyChange: (Boolean) -> Unit,
    fetchingClosest: Boolean,
    onFetchClosest: () -> Unit,
    error: String?,
    stations: List<Station>,
    onStationSelected: (Station) -> Unit,
    cameraPositionState: CameraPositionState
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                onSearchQueryChange(it)
                onShowSearchResultsChange(it.isNotBlank())
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Search stations...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onShowFavoritesOnlyChange(!showFavoritesOnly) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (showFavoritesOnly)
                        Color(0xFF194D33)
                    else
                        Color.White,
                    contentColor = if (showFavoritesOnly)
                        Color.White
                    else
                        Color(0xFF194D33)
                )
            ) {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorites",
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    "Favorites Only", 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onFetchClosest,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF194D33)
                )
            ) {
                if (fetchingClosest) {
                    CircularProgressIndicator(
                        color = Color(0xFF194D33), 
                        modifier = Modifier.padding(end = 6.dp).size(18.dp), 
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Nearby stations",
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Text(
                    "Nearby stations", 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Offline banner
        AnimatedVisibility(
            visible = error != null && stations.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📡",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Offline - showing cached data. Retrying…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSearchResults && searchQuery.isNotBlank()
        ) {
            val filtered = stations.filter { 
                it.status.equals("Active", ignoreCase = true) && 
                it.stationName.contains(searchQuery, ignoreCase = true) 
            }
            if (filtered.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(filtered) { station ->
                            Text(
                                text = station.stationName,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSearchQueryChange(station.stationName)
                                        onShowSearchResultsChange(false)
                                        onStationSelected(station)
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(station.latitude, station.longitude), 16f
                                                )
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
