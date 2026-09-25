package com.example.solnex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solnex.auth.LoginActivity
import com.example.solnex.auth.TokenStore
import com.example.solnex.ui.theme.SolNexTheme
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private data class UserSession(
    val nic: String,
    val fullName: String,
    val role: String
)

private data class UserProfile(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val accountStatus: String
)

class MainActivity : ComponentActivity() {
    private val tokenStore by lazy { TokenStore(this) }

    private var currentUser by mutableStateOf<UserSession?>(null)
    private var profile by mutableStateOf<UserProfile?>(null)
    private var loading by mutableStateOf(true)
    private var error by mutableStateOf<String?>(null)
    private var message by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (tokenStore.token() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        currentUser = UserSession(
            nic = tokenStore.nic().orEmpty(),
            fullName = tokenStore.fullName().orEmpty(),
            role = tokenStore.role().orEmpty()
        )

        loadProfile()

        setContent {
            SolNexTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        user = currentUser,
                        profile = profile,
                        loading = loading,
                        error = error,
                        message = message,
                        onLogout = {
                            tokenStore.clear()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        },
                        onRefresh = ::loadProfile,
                        onSaveProfile = ::saveProfile,
                        onRequestDeactivation = ::requestDeactivation
                    )
                }
            }
        }
    }

    private fun loadProfile() {
        val nic = tokenStore.nic() ?: return
        val token = tokenStore.token() ?: return
        loading = true
        error = null

        Thread {
            val response = try {
                val connection = (URL("http://10.0.2.2:5097/api/users/$nic").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode in 200..299) {
                    val json = JSONObject(body)
                    val current = UserProfile(
                        nic = json.optString("nic", nic),
                        fullName = json.optString("fullName", tokenStore.fullName().orEmpty()),
                        email = json.optString("email", ""),
                        phone = json.optString("phone", ""),
                        role = json.optString("role", tokenStore.role().orEmpty()),
                        accountStatus = json.optString("accountStatus", "Pending")
                    )
                    val updatedName = current.fullName
                    tokenStore.updateProfile(updatedName)
                    currentUser = currentUser?.copy(fullName = updatedName)
                    currentUser to current
                } else {
                    null to null
                }
            } catch (_: Exception) {
                null to null
            }

            runOnUiThread {
                loading = false
                if (response.first != null && response.second != null) {
                    profile = response.second
                    error = null
                } else {
                    error = "Unable to load account details right now."
                }
            }
        }.start()
    }

    private fun saveProfile(fullName: String, email: String, phone: String) {
        val nic = tokenStore.nic() ?: return
        val token = tokenStore.token() ?: return

        Thread {
            val response = try {
                val payload = JSONObject().apply {
                    put("fullName", fullName)
                    put("email", email)
                    put("phone", phone)
                }.toString()

                val connection = (URL("http://10.0.2.2:5097/api/users/$nic").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode in 200..299) {
                    val json = JSONObject(body)
                    val updated = UserProfile(
                        nic = json.optString("nic", nic),
                        fullName = json.optString("fullName", fullName),
                        email = json.optString("email", email),
                        phone = json.optString("phone", phone),
                        role = json.optString("role", tokenStore.role().orEmpty()),
                        accountStatus = json.optString("accountStatus", profile?.accountStatus ?: "Pending")
                    )
                    tokenStore.updateProfile(updated.fullName)
                    currentUser = currentUser?.copy(fullName = updated.fullName)
                    updated to null
                } else {
                    null to null
                }
            } catch (_: Exception) {
                null to null
            }

            runOnUiThread {
                if (response.first != null) {
                    profile = response.first
                    message = "Profile updated successfully."
                    error = null
                } else {
                    message = null
                    error = "Profile update failed. Please try again."
                }
            }
        }.start()
    }

    private fun requestDeactivation() {
        val nic = tokenStore.nic() ?: return
        val token = tokenStore.token() ?: return

        Thread {
            val success = try {
                val connection = (URL("http://10.0.2.2:5097/api/users/$nic/deactivate").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()
                responseCode in 200..299
            } catch (_: Exception) {
                false
            }

            runOnUiThread {
                if (success) {
                    tokenStore.clear()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    error = "Deactivation request could not be sent. Try again later."
                    message = null
                }
            }
        }.start()
    }
}

@Composable
private fun DashboardScreen(
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
    val displayUser = user ?: UserSession("", "User", "Prosumer")
    var fullName by remember(displayUser.fullName) { mutableStateOf(displayUser.fullName) }
    var email by remember(profile?.email) { mutableStateOf(profile?.email.orEmpty()) }
    var phone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
    var editMode by remember { mutableStateOf(false) }
    val profileStatus = profile?.accountStatus ?: "Pending"

    LaunchedEffect(profile) {
        fullName = profile?.fullName ?: displayUser.fullName
        email = profile?.email.orEmpty()
        phone = profile?.phone.orEmpty()
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
            TextButton(onClick = onLogout) {
                Text("Log out")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Welcome", style = MaterialTheme.typography.titleMedium)
                Text(displayUser.fullName.ifBlank { "Solar user" })
                Text("NIC: ${displayUser.nic.ifBlank { "Not available" }}")
                Text("Role: ${displayUser.role}")
                Text("Status: $profileStatus")
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
                            onValueChange = { fullName = it },
                            label = { Text("Full name") },
                            enabled = editMode,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            enabled = editMode,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            enabled = editMode,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!editMode) {
                        Button(onClick = { editMode = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit profile")
                        }
                    } else {
                        Button(
                            onClick = {
                                onSaveProfile(fullName.trim(), email.trim(), phone.trim())
                                editMode = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save changes")
                        }
                    }

                    Button(
                        onClick = onRequestDeactivation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request account deactivation")
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
    }
}
