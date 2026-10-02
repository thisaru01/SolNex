package com.example.solnex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solnex.auth.LoginActivity
import com.example.solnex.auth.TokenStore
import com.example.solnex.ui.theme.SolNexTheme
import com.example.solnex.ui.theme.SolNexNavIndicator
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.solnex.operator.ui.OperatorModeScreen
import com.example.solnex.operator.prosumer.ui.ProsumerQrModeScreen

enum class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Home("Home", Icons.Default.Home),
    Map("Map", Icons.Default.LocationOn),
    Reservations("Reservations", Icons.Default.DateRange),
    QR("QR", Icons.Default.Share),
    Profile("Profile", Icons.Default.Person)
}
class MainActivity : ComponentActivity() {
    companion object {
        var pendingReservationIdToOpen: String? = null
    }

    private val tokenStore by lazy { TokenStore(this) }
    private val profileRepository by lazy { ApiProfileRepository() }
    private val deactivationRepository by lazy { ApiDeactivationRepository() }

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
            var currentTab by remember { mutableStateOf(BottomNavItem.Home) }

            SolNexTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            BottomNavItem.values().forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = {
                                        Text(
                                            text = item.title,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                                        )
                                    },
                                    selected = currentTab == item,
                                    onClick = { currentTab = item },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = SolNexNavIndicator,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        BottomNavItem.Home -> {
                            HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                user = currentUser,
                                profile = profile,
                                onNavigateToTab = { currentTab = it }
                            )
                        }
                        BottomNavItem.Map -> {
                            MapScreen(
                                modifier = Modifier.padding(innerPadding),
                                token = tokenStore.token().orEmpty(),
                                role = tokenStore.role().orEmpty(),
                                onNavigateToTab = { currentTab = it }
                            )
                        }
                        BottomNavItem.Reservations -> {
                            ReservationsScreen(
                                modifier = Modifier.padding(innerPadding),
                                onNavigateToTab = { currentTab = it }
                            )
                        }
                        BottomNavItem.QR -> {
                            when {
                                tokenStore.role().equals("GridOperator", ignoreCase = true) -> {
                                    OperatorModeScreen(
                                        modifier = Modifier.padding(innerPadding),
                                        token = tokenStore.token().orEmpty(),
                                        operatorNic = tokenStore.nic().orEmpty(),
                                        onExit = { currentTab = BottomNavItem.Home }
                                    )
                                }
                                tokenStore.role().equals("Prosumer", ignoreCase = true) -> {
                                    ProsumerQrModeScreen(
                                        modifier = Modifier.padding(innerPadding),
                                        token = tokenStore.token().orEmpty()
                                    )
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("QR is not available for this account.")
                                    }
                                }
                            }
                        }
                        BottomNavItem.Profile -> {
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
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${currentTab.title} Screen Coming Soon")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadProfile() {
        val nic = tokenStore.nic() ?: return
        val token = tokenStore.token() ?: return
        loading = true
        error = null

        profileRepository.loadProfile(nic, token) { result ->
            runOnUiThread {
                loading = false
                when (result) {
                    is ProfileResult.Success -> {
                        profile = result.profile
                        tokenStore.updateProfile(result.profile.fullName)
                        currentUser = currentUser?.copy(fullName = result.profile.fullName)
                        error = null
                        
                        // Log out if account is deactivated
                        if (result.profile.accountStatus == "Inactive") {
                            tokenStore.clear()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    }
                    is ProfileResult.Error -> {
                        error = result.message
                    }
                }
            }
        }
    }

    private fun saveProfile(fullName: String, email: String, phone: String) {
        val nic = tokenStore.nic() ?: return
        val token = tokenStore.token() ?: return

        profileRepository.saveProfile(nic, token, fullName, email, phone) { result ->
            runOnUiThread {
                when (result) {
                    is ProfileResult.Success -> {
                        profile = result.profile
                        tokenStore.updateProfile(result.profile.fullName)
                        currentUser = currentUser?.copy(fullName = result.profile.fullName)
                        message = "Profile updated successfully."
                        error = null
                    }
                    is ProfileResult.Error -> {
                        message = null
                        error = result.message
                    }
                }
            }
        }
    }

    private fun requestDeactivation() {
        val nic = tokenStore.nic() ?: return
        val token = tokenStore.token() ?: return

        deactivationRepository.requestDeactivation(nic, token) { result ->
            runOnUiThread {
                when (result) {
                    is DeactivationResult.Success -> {
                        message = "Deactivation request submitted. Awaiting Backoffice approval."
                        error = null
                        loadProfile()
                    }
                    is DeactivationResult.Error -> {
                        error = result.message
                        message = null
                    }
                }
            }
        }
    }
}
