package com.example.solnex.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.solnex.ui.theme.SolNexTheme

/**
 * Login activity for prosumer authentication.
 * Handles user login via API and navigation to main app on success.
 */
class LoginActivity : ComponentActivity() {
    private val tokenStore by lazy { TokenStore(this) }
    private val loginRepository by lazy { ApiLoginRepository(tokenStore) }
    private var loginResult by mutableStateOf<LoginResult?>(null)

    // Initialize login screen with Compose UI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolNexTheme {
                LoginScreen(
                    result = loginResult,
                    onSubmit = ::submitLogin,
                    onRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    // Submit login request and handle authentication result
    private fun submitLogin(request: LoginRequest) {
        loginResult = null
        loginRepository.login(request) { result ->
            runOnUiThread {
                loginResult = result
                if (result.success) {
                    startActivity(Intent(this, com.example.solnex.MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

