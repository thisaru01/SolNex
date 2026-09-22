package com.example.solnex.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.solnex.ui.theme.SolNexTheme

class RegisterActivity : ComponentActivity() {
    private val registerRepository: RegisterRepository by lazy { ApiRegisterRepository() }
    private val localUserDatabase: LocalUserDatabase by lazy { LocalUserDatabase(this) }
    private var registrationResult by mutableStateOf<RegisterResult?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SolNexTheme {
                RegisterScreen(
                    result = registrationResult,
                    onSubmit = ::submitRegistration,
                    onBackToLogin = { finish() }
                )
            }
        }
    }

    private fun submitRegistration(request: RegisterRequest) {
        registrationResult = null
        registerRepository.register(request) { result ->
            runOnUiThread {
                registrationResult = result
                if (result.success) {
                    localUserDatabase.savePendingUser(request.nic)
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        localUserDatabase.close()
        super.onDestroy()
    }
}