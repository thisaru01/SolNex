package com.example.solnex.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.solnex.ui.theme.SolNexTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

class ResetPasswordActivity : ComponentActivity() {
    private val repository by lazy { ApiResetPasswordRepository() }
    private var result by mutableStateOf<ResetPasswordResult?>(null)
    private var submitting by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolNexTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ResetPasswordScreen(
                        result = result,
                        submitting = submitting,
                        onSubmit = { request ->
                            result = null
                            submitting = true
                            repository.reset(request) { resetResult -> runOnUiThread {
                                result = resetResult
                                submitting = false
                                if (resetResult.success) {
                                    Toast.makeText(this, resetResult.message, Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                            } }
                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ResetPasswordScreen(
    result: ResetPasswordResult?,
    submitting: Boolean,
    onSubmit: (ResetPasswordRequest) -> Unit,
    onBack: () -> Unit
) {
    var identifier by androidx.compose.runtime.remember { mutableStateOf("") }
    var email by androidx.compose.runtime.remember { mutableStateOf("") }
    var password by androidx.compose.runtime.remember { mutableStateOf("") }
    var confirmPassword by androidx.compose.runtime.remember { mutableStateOf("") }
    var showPassword by androidx.compose.runtime.remember { mutableStateOf(false) }
    var showConfirmPassword by androidx.compose.runtime.remember { mutableStateOf(false) }
    var error by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Reset password", style = MaterialTheme.typography.headlineLarge)
        Text("Verify your account with your NIC or email and registered email address.")
        OutlinedTextField(identifier, { identifier = it; error = null }, label = { Text("NIC or email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(email, { email = it; error = null }, label = { Text("Registered email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(password, { password = it; error = null }, label = { Text("New password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (showPassword) "Hide password" else "Show password") } })
        OutlinedTextField(confirmPassword, { confirmPassword = it; error = null }, label = { Text("Confirm password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) { Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (showConfirmPassword) "Hide password" else "Show password") } })
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let { Text(it.message, color = if (it.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
        Button(onClick = {
            error = when {
                identifier.isBlank() || email.isBlank() -> "Enter your identifier and registered email."
                password.length < 8 -> "Password must contain at least 8 characters."
                password != confirmPassword -> "Passwords do not match."
                else -> null
            }
            if (error == null) onSubmit(ResetPasswordRequest(identifier.trim(), email.trim(), password))
        }, enabled = !submitting, modifier = Modifier.fillMaxWidth()) {
            if (submitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Reset password")
        }
        TextButton(onClick = onBack, enabled = !submitting, modifier = Modifier.fillMaxWidth()) { Text("Back to sign in") }
    }
}