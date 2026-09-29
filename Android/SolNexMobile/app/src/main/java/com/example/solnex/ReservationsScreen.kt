package com.example.solnex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.solnex.auth.TokenStore

@Composable
fun ReservationsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val repository = remember { ReservationRepository() }
    val token = tokenStore.token().orEmpty()
    val nic = tokenStore.nic().orEmpty()

    // State variables for tracking fetched reservations, loading status, and any potential errors
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // State variable for the currently active filter chip
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Pending", "Approved", "Cancelled", "CancellationRequested")

    LaunchedEffect(Unit) {
        // Fetch user's reservations from the API when the screen initializes
        if (nic.isNotBlank() && token.isNotBlank()) {
            repository.getReservationsByNic(token, nic) { fetchedReservations, errMsg ->
                if (fetchedReservations != null) {
                    val allowedStatuses = setOf("Pending", "Cancelled", "CancellationRequested", "Approved")
                    reservations = fetchedReservations.filter { it.status in allowedStatuses }
                } else {
                    error = errMsg
                }
                loading = false
            }
        } else {
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title & Header
        Column {
            Text(
                text = "Reservations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage and track your energy slot reservations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Filter chips
        @OptIn(ExperimentalMaterial3Api::class)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        // Apply UI-level filtering based on the active selected filter chip
        val filteredReservations = if (selectedFilter == "All") {
            reservations
        } else {
            reservations.filter { it.status == selectedFilter }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
        } else if (error != null) {
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        } else if (filteredReservations.isEmpty()) {
            // Placeholder content card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Reservations",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Reservations Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Your energy slot bookings and scheduled charging reservations will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Render a card for each reservation that matches the selected filter criteria
            filteredReservations.forEach { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDEE4FA), // Image 1 light blue background
                        contentColor = Color(0xFF253B73)    // Image 1 dark blue text
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Station ID: ${res.stationId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF253B73)
                        )
                        Text(
                            text = "Reservation ID: ${res.reservationId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF556994) // slightly lighter for secondary text
                        )
                        if (res.dayOfWeek != null || res.startTime != null) {
                            Text(
                                text = "Schedule: ${res.dayOfWeek ?: ""} ${res.startTime ?: ""} - ${res.endTime ?: ""}",
                                color = Color(0xFF253B73)
                            )
                        }
                        Text(
                            text = "Energy: ${res.energyAmountKwh} kWh",
                            color = Color(0xFF253B73)
                        )
                        
                        // Determine the color of the status pill dynamically based on the current status
                        val statusColor = when (res.status) {
                            "Approved" -> Color(0xFF10B981) // Green
                            "Pending" -> Color(0xFFF59E0B) // Orange
                            "Cancelled" -> Color(0xFFEF4444) // Red
                            "CancellationRequested" -> Color(0xFF8B5CF6) // Purple
                            else -> Color(0xFF253B73) // Default dark blue
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .border(1.dp, statusColor, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = res.status,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            
                            Text(
                                text = "View",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
