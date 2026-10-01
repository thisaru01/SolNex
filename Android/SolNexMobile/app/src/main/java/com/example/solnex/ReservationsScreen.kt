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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
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
    modifier: Modifier = Modifier,
    onNavigateToTab: (BottomNavItem) -> Unit = {}
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val repository = remember { ReservationRepository(context) }
    val stationRepo = remember { StationRepository(context) }
    val token = tokenStore.token().orEmpty()
    val nic = tokenStore.nic().orEmpty()

    // State variables for tracking fetched reservations, loading status, and any potential errors
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var stationMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var errorTitle by remember { mutableStateOf<String?>(null) }
    var errorSubtitle by remember { mutableStateOf<String?>(null) }
    
    // State variable for the currently active filter chip
    var selectedFilter by remember { mutableStateOf("All") }
    
    // State variable for search query
    var searchQuery by remember { mutableStateOf("") }
    
    // State variable for selected reservation to view details
    var selectedReservation by remember { mutableStateOf<Reservation?>(null) }
    
    // State variable to trigger refresh
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // State for showing the edit dialog
    var showEditDialog by remember { mutableStateOf<Reservation?>(null) }
    
    // States for confirmation popups
    var confirmCancelReservation by remember { mutableStateOf<Reservation?>(null) }
    var confirmDeleteReservation by remember { mutableStateOf<Reservation?>(null) }

    val filterOptions = listOf("All", "Pending", "Approved", "Completed", "Cancelled", "CancellationRequested", "Rejected")

    LaunchedEffect(refreshTrigger) {
        // Fetch user's reservations from the API when the screen initializes
        if (nic.isNotBlank() && token.isNotBlank()) {
            loading = true
            stationRepo.getStations(token) { fetchedStations, _ ->
                if (fetchedStations != null) {
                    stationMap = fetchedStations.associate { it.stationId to it.stationName }
                }
                repository.getReservationsByNic(token, nic) { fetchedReservations, errMsg ->
                    val finalReservations = fetchedReservations ?: emptyList()
                    val allowedStatuses = setOf("Pending", "Cancelled", "CancellationRequested", "Approved", "Rejected", "Completed")
                    reservations = finalReservations.filter { it.status in allowedStatuses }
                    
                    if (selectedReservation != null) {
                        selectedReservation = reservations.find { it.id == selectedReservation!!.id }
                    } else if (MainActivity.pendingReservationIdToOpen != null) {
                        selectedReservation = reservations.find { it.id == MainActivity.pendingReservationIdToOpen }
                        MainActivity.pendingReservationIdToOpen = null
                    }
                    // Deliberately ignoring errMsg here to prevent the disruptive popup. 
                    // It will seamlessly fall back to cached data or show the empty state.
                    loading = false
                }
            }
        } else {
            loading = false
        }
    }

    if (selectedReservation != null) {
        val res = selectedReservation!!
        androidx.activity.compose.BackHandler {
            selectedReservation = null
        }
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedReservation = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Reservation Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F2937)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val statusColor = when (res.status) {
                            "Approved", "Completed" -> Color(0xFF10B981)
                            "Pending" -> Color(0xFFF59E0B)
                            "Cancelled", "Rejected" -> Color(0xFFEF4444)
                            "CancellationRequested" -> Color(0xFF8B5CF6)
                            else -> Color(0xFF3B82F6) // Blue default
                        }
                        
                        val dividerColor = Color(0xFFF3F4F6)
                        
                        // Row 1: Status
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Status", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .border(1.dp, statusColor, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = res.status,
                                    color = statusColor,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))

                        // Row 2: Station Name
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Station Name", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            Text(stationMap[res.stationId] ?: "Unknown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        
                        // Row 3: Station ID
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Station ID", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            Text(res.stationId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        
                        // Row 4: Reservation ID
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Reservation ID", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            Text(res.reservationId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        
                        // Row 5: Date
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Date", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            Text(res.reservationDate.substringBefore("T"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }

                        if (res.dayOfWeek != null || res.startTime != null) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                            
                            // Row 6: Schedule
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Schedule", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                                Text("${res.dayOfWeek ?: ""} ${res.startTime ?: ""} - ${res.endTime ?: ""}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        
                        // Row 7: Energy
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Energy", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                            Text("${res.energyAmountKwh} kWh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                    }
                }
            }

            // Buttons pinned to bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (res.status == "Pending") {
                    Button(
                        onClick = { 
                            showEditDialog = res
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Edit Reservation", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                }
                if (res.status == "Pending" || res.status == "Approved") {
                    Button(
                        onClick = { confirmCancelReservation = res },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel Reservation", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                } else if (res.status == "Cancelled" || res.status == "Rejected") {
                    Button(
                        onClick = { confirmDeleteReservation = res },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete Record", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                }
            }
        }
        
        if (showEditDialog != null) {
            ReservationFormDialog(
                stationId = showEditDialog!!.stationId,
                stationName = "Station ${showEditDialog!!.stationId}",
                existingReservation = showEditDialog,
                onDismiss = { showEditDialog = null },
                onSuccess = { navTarget ->
                    showEditDialog = null
                    refreshTrigger++
                    if (navTarget != null) {
                        onNavigateToTab(navTarget)
                    }
                }
            )
        }

        if (error != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { error = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        errorTitle?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
                        errorSubtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { error = null }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
        
        if (confirmCancelReservation != null) {
            val resToCancel = confirmCancelReservation!!
            AlertDialog(
                onDismissRequest = { confirmCancelReservation = null },
                containerColor = Color.White,
                titleContentColor = Color(0xFF1F2937),
                textContentColor = Color(0xFF1F2937),
                title = { Text("Cancel Reservation") },
                text = { Text("Are you sure you want to cancel this reservation?") },
                confirmButton = {
                    TextButton(onClick = { 
                        repository.cancelReservation(token, resToCancel.id) { success, errMsg ->
                            if (success) {
                                refreshTrigger++ 
                            } else {
                                error = errMsg
                                errorTitle = "Cancel Reservation"
                                errorSubtitle = "Station ${resToCancel.stationId}"
                            }
                        }
                        confirmCancelReservation = null
                    }) {
                        Text("Yes, Cancel", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmCancelReservation = null }) {
                        Text("No", color = Color(0xFF4B5563))
                    }
                }
            )
        }

        if (confirmDeleteReservation != null) {
            val resToDelete = confirmDeleteReservation!!
            AlertDialog(
                onDismissRequest = { confirmDeleteReservation = null },
                containerColor = Color.White,
                titleContentColor = Color(0xFF1F2937),
                textContentColor = Color(0xFF1F2937),
                title = { Text("Delete Record") },
                text = { Text("Are you sure you want to permanently delete this reservation record?") },
                confirmButton = {
                    TextButton(onClick = { 
                        repository.deleteReservation(token, resToDelete.id) { success, errMsg ->
                            if (success) {
                                refreshTrigger++ 
                                selectedReservation = null
                            } else {
                                error = errMsg
                                errorTitle = "Delete Reservation"
                                errorSubtitle = "Station ${resToDelete.stationId}"
                            }
                        }
                        confirmDeleteReservation = null
                    }) {
                        Text("Yes, Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteReservation = null }) {
                        Text("No", color = Color(0xFF4B5563))
                    }
                }
            )
        }
        
        return
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

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search reservations...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips
        @OptIn(ExperimentalMaterial3Api::class)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                val count = if (filter == "All") reservations.size else reservations.count { it.status == filter }
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text("$filter ($count)") }
                )
            }
        }

        // Apply UI-level filtering based on the active selected filter chip and search query
        val searchFilteredReservations = reservations.filter { res ->
            val query = searchQuery.lowercase()
            val stationName = stationMap[res.stationId]?.lowercase() ?: ""
            query.isEmpty() ||
                    res.stationId.lowercase().contains(query) ||
                    res.reservationId.lowercase().contains(query) ||
                    stationName.contains(query)
        }

        val filteredReservations = if (selectedFilter == "All") {
            searchFilteredReservations
        } else {
            searchFilteredReservations.filter { it.status == selectedFilter }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
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
                    modifier = Modifier.fillMaxWidth().clickable { selectedReservation = res },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F2937)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusColor = when (res.status) {
                            "Approved", "Completed" -> Color(0xFF10B981)
                            "Pending" -> Color(0xFFF59E0B)
                            "Cancelled", "Rejected" -> Color(0xFFEF4444)
                            "CancellationRequested" -> Color(0xFF8B5CF6)
                            else -> Color(0xFF3B82F6)
                        }
                        
                        // Top Row: Status pill and View hint
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .border(1.dp, statusColor, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = res.status,
                                    color = statusColor,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Text(
                                text = "View Details",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Text(
                            text = stationMap[res.stationId] ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Station ID: ${res.stationId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                        Text(
                            text = "Reservation ID: ${res.reservationId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                        if (res.dayOfWeek != null || res.startTime != null) {
                            Text(
                                text = "Schedule: ${res.dayOfWeek ?: ""} ${res.startTime ?: ""} - ${res.endTime ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1F2937)
                            )
                        }
                        Text(
                            text = "Energy: ${res.energyAmountKwh} kWh",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }
        }
    }

    // Reservation details view is handled via an early return at the beginning of this composable.
    
    if (showEditDialog != null) {
        ReservationFormDialog(
            stationId = showEditDialog!!.stationId,
            stationName = "Station ${showEditDialog!!.stationId}",
            existingReservation = showEditDialog,
            onDismiss = { showEditDialog = null },
            onSuccess = { navTarget ->
                showEditDialog = null
                refreshTrigger++
                if (navTarget != null) {
                    onNavigateToTab(navTarget)
                }
            }
        )
    }

    if (error != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { error = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    errorTitle?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
                    errorSubtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { error = null }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
