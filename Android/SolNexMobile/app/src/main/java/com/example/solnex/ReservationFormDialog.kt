package com.example.solnex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.solnex.auth.TokenStore
import java.time.Instant
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormDialog(
    stationId: String,
    stationName: String,
    existingReservation: Reservation? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val repository = remember { ReservationRepository() }
    val token = tokenStore.token().orEmpty()
    val nic = tokenStore.nic().orEmpty()

    // State variables for fetching available slots and error handling
    var slots by remember { mutableStateOf<List<Slot>>(emptyList()) }
    var loadingSlots by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // State variables for user inputs (selected day, slot, and energy amount)
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var selectedSlot by remember { mutableStateOf<Slot?>(null) }
    var energyAmount by remember { mutableStateOf(existingReservation?.energyAmountKwh?.toString() ?: "") }
    
    // Tracks the submission state to show loading indicators
    var submitting by remember { mutableStateOf(false) }

    // Fetch available slots for the selected station when the dialog opens
    LaunchedEffect(stationId) {
        loadingSlots = true
        repository.getAvailableSlots(token) { fetchedSlots, errMsg ->
            if (fetchedSlots != null) {
                // Filter slots by stationId
                slots = fetchedSlots.filter { it.stationId == stationId }
                if (existingReservation != null) {
                    selectedDay = existingReservation.dayOfWeek
                    selectedSlot = slots.find { it.slotId == existingReservation.slotId }
                }
            } else {
                error = errMsg
            }
            loadingSlots = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(if (existingReservation != null) "Update Reservation" else "Submit Reservation", style = MaterialTheme.typography.titleLarge)
                Text(stationName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                if (loadingSlots) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (error != null) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                } else if (slots.isEmpty()) {
                    Text("No available slots for this station.")
                } else {
                    // Day dropdown
                    val availableDays = remember(slots) { slots.mapNotNull { it.dayOfWeek?.takeIf { d -> d.isNotBlank() } }.distinct() }
                    var dayExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = !dayExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDay ?: "Select Day",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            availableDays.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        selectedDay = day
                                        selectedSlot = null // reset slot when day changes
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Slots dropdown
                    val availableSlotsForDay = remember(slots, selectedDay) {
                        slots.filter { it.dayOfWeek == selectedDay }
                    }
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (selectedDay != null) expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSlot?.let { "${it.startTime} - ${it.endTime}" } ?: "Select Slot",
                            onValueChange = {},
                            readOnly = true,
                            enabled = selectedDay != null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableSlotsForDay.forEach { slot ->
                                DropdownMenuItem(
                                    text = { Text("${slot.startTime} - ${slot.endTime}") },
                                    onClick = {
                                        selectedSlot = slot
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = energyAmount,
                        onValueChange = { energyAmount = it },
                        label = { Text("Energy Amount (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss, enabled = !submitting) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = energyAmount.toDoubleOrNull()
                                // Validate the input before attempting submission
                                if (selectedSlot != null && amount != null && amount > 0) {
                                    submitting = true
                                    error = null
                                    
                                    if (existingReservation != null) {
                                        repository.updateReservationDetails(
                                            token = token,
                                            id = existingReservation.id,
                                            slotId = selectedSlot!!.slotId,
                                            energyAmountKwh = amount
                                        ) { success, errMsg ->
                                            submitting = false
                                            if (success) {
                                                onSuccess()
                                            } else {
                                                error = errMsg
                                            }
                                        }
                                    } else {
                                        // Generate the current ISO timestamp for the reservation date
                                        val dateStr = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                                        
                                        // Send the reservation request to the backend API
                                        repository.submitReservation(
                                            token = token,
                                            nic = nic,
                                            stationId = stationId,
                                            slotId = selectedSlot!!.slotId,
                                            reservationDate = dateStr,
                                            energyAmountKwh = amount
                                        ) { success, errMsg ->
                                            submitting = false
                                            if (success) {
                                                onSuccess()
                                            } else {
                                                error = errMsg
                                            }
                                        }
                                    }
                                } else {
                                    error = "Please select a slot and enter a valid energy amount."
                                }
                            },
                            enabled = selectedSlot != null && energyAmount.isNotBlank() && !submitting
                        ) {
                            if (submitting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Text(if (existingReservation != null) "Update" else "Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}
