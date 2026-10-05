package com.example.solnex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * User session data model
 */
data class UserSession(
    val nic: String,
    val fullName: String,
    val role: String
)

/**
 * Dashboard screen for prosumer profile management.
 * Allows viewing and editing profile information, and requesting account deactivation.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    user: UserSession?,
    profile: UserProfile?,
    loading: Boolean,
    error: String?,
    message: String?,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onRequestDeactivation: () -> Unit
) {
    // Format phone number with spaces for display
    fun formatPhoneNumber(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.startsWith("94") && digits.length >= 11 -> {
                val formatted = StringBuilder()
                formatted.append("+94 ")
                if (digits.length > 2) formatted.append(digits.substring(2, 5))
                if (digits.length > 5) formatted.append(" ").append(digits.substring(5, 8))
                if (digits.length > 8) formatted.append(" ").append(digits.substring(8))
                formatted.toString()
            }
            digits.startsWith("0") && digits.length >= 10 -> {
                val formatted = StringBuilder()
                formatted.append(digits.substring(0, 3))
                if (digits.length > 3) formatted.append(" ").append(digits.substring(3, 6))
                if (digits.length > 6) formatted.append(" ").append(digits.substring(6))
                formatted.toString()
            }
            else -> input
        }
    }

    val displayUser = user ?: UserSession("", "User", "Prosumer")
    var fullName by remember(displayUser.fullName) { mutableStateOf(displayUser.fullName) }
    var email by remember(profile?.email) { mutableStateOf(profile?.email.orEmpty()) }
    var phone by remember(profile?.phone) { mutableStateOf(formatPhoneNumber(profile?.phone.orEmpty())) }
    var editMode by remember { mutableStateOf(false) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeactivationDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val profileStatus = profile?.accountStatus ?: "Pending"

    val statusMessage = when (profileStatus) {
        "DeactivationRequested" -> "Deactivation request pending approval"
        else -> profileStatus
    }

    LaunchedEffect(profile) {
        fullName = profile?.fullName ?: displayUser.fullName
        email = profile?.email.orEmpty()
        phone = formatPhoneNumber(profile?.phone.orEmpty())
    }

    LaunchedEffect(error, message) {
        (message ?: error)?.let { snackbarHostState.showSnackbar(it) }
    }

    // Validate profile data and save changes
    fun validateAndSave() {
        fullNameError = if (fullName.trim().length < 2) "Enter your full name." else null
        emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Enter a valid email." else null
        val phoneDigits = phone.filter { it.isDigit() }
        phoneError = when {
            phoneDigits.startsWith("94") && phoneDigits.length != 11 -> "Phone must be +94 followed by 9 digits."
            phoneDigits.startsWith("0") && phoneDigits.length != 10 -> "Phone must be 0 followed by 9 digits."
            !phoneDigits.startsWith("94") && !phoneDigits.startsWith("0") -> "Phone must start with +94 or 0."
            else -> null
        }

        if (fullNameError == null && emailError == null && phoneError == null) {
            onSaveProfile(fullName.trim(), email.trim(), phoneDigits)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SolNex", style = MaterialTheme.typography.titleMedium)
                Text("${displayUser.role} dashboard", style = MaterialTheme.typography.headlineSmall)
            }
            TextButton(onClick = { showLogoutDialog = true }) {
                Text("Log out")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Welcome", style = MaterialTheme.typography.titleMedium)
                Text(displayUser.fullName.ifBlank { "Solar user" })
                Text("NIC: ${displayUser.nic.ifBlank { "Not available" }}")
                Text("Role: ${displayUser.role}")
                Text("Status: $statusMessage")
                Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Refresh account")
                }
            }
        }

        if (displayUser.role == "Prosumer") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Profile", style = MaterialTheme.typography.titleMedium)
                    if (loading) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { 
                                fullName = it
                                fullNameError = null
                            },
                            label = { Text("Full name") },
                            enabled = editMode,
                            modifier = Modifier.fillMaxWidth(),
                            isError = fullNameError != null
                        )
                        fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                emailError = null
                            },
                            label = { Text("Email") },
                            enabled = editMode,
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError != null
                        )
                        emailError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { 
                                phone = formatPhoneNumber(it)
                                phoneError = null
                            },
                            label = { Text("Phone") },
                            enabled = editMode,
                            modifier = Modifier.fillMaxWidth(),
                            isError = phoneError != null
                        )
                        phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    }

                    if (!editMode) {
                        Button(onClick = { editMode = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit profile")
                        }
                    } else {
                        Button(
                            onClick = {
                                validateAndSave()
                                if (fullNameError == null && emailError == null && phoneError == null) {
                                    editMode = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save changes")
                        }
                    }

                    if (profileStatus != "DeactivationRequested") {
                        Button(
                            onClick = { showDeactivationDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Request account deactivation")
                        }
                    } else {
                        Text(
                            "Deactivation request is pending Backoffice approval",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Operator mode", style = MaterialTheme.typography.titleMedium)
                    Text("Ready to verify QR transactions and manage energy transfer approvals.")
                    Text("Use the web dashboard for full administrative operations and the mobile app for quick mobile checks.")
                }
            }
        }

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        if (message != null) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(24.dp))
        SnackbarHost(hostState = snackbarHostState)
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Account deactivation confirmation dialog
    if (showDeactivationDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivationDialog = false },
            title = { Text("Confirm account deactivation") },
            text = { Text("Are you sure you want to request account deactivation? This action requires Backoffice approval and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeactivationDialog = false
                    onRequestDeactivation()
                }) {
                    Text("Request deactivation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
