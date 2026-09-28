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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.platform.LocalContext
import com.example.solnex.operator.OperatorActivity
import com.example.solnex.operator.prosumer.ProsumerApprovedReservationsActivity

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
            SolNexTheme {
                Scaffold { innerPadding ->
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
