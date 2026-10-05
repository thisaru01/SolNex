package com.example.solnex.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

/**
 * Login screen for prosumer authentication.
 * Allows users to sign in using NIC or email and password.
 */
@Composable
fun LoginScreen(
    result: LoginResult?,
    onSubmit: (LoginRequest) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(result) {
        if (result != null) {
            isSubmitting = false
            snackbarHostState.showSnackbar(result.message)
        }
    }

    // Validate and submit login form
    fun submit() {
        val trimmedIdentifier = identifier.trim()
        validationError = when {
            trimmedIdentifier.isEmpty() -> "Enter your NIC or email."
            password.isEmpty() -> "Enter your password."
            trimmedIdentifier.all { it.isDigit() || it == 'V' || it == 'X' } && !trimmedIdentifier.matches(Regex("^(\\d{9}[VX]|\\d{12})$")) -> "NIC must be 9 digits ending with V/X or 12 digits."
            else -> null
        }
        if (validationError == null) {
            isSubmitting = true
            onSubmit(LoginRequest(trimmedIdentifier, password))
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            Text("Welcome back", style = MaterialTheme.typography.headlineLarge)
            Text("Sign in to your SolNex account.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(identifier, { identifier = it }, label = { Text("NIC or email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { androidx.compose.material3.IconButton(onClick = { showPassword = !showPassword }) { androidx.compose.material3.Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (showPassword) "Hide password" else "Show password") } })
            validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            result?.takeIf { !it.success }?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
            result?.takeIf { it.success }?.let { Text(it.message, color = MaterialTheme.colorScheme.primary) }
            Button(onClick = ::submit, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                if (isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Sign in")
            }
            TextButton(onClick = onRegister, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                Text("New prosumer? Register")
            }
            TextButton(onClick = onForgotPassword, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                Text("Forgot password?")
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}
