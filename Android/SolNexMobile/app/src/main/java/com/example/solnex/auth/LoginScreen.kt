package com.example.solnex.auth

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    result: LoginResult?,
    onSubmit: (LoginRequest) -> Unit,
    onRegister: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result != null) isSubmitting = false
    }

    fun submit() {
        validationError = when {
            identifier.trim().isEmpty() -> "Enter your NIC or email."
            password.isEmpty() -> "Enter your password."
            else -> null
        }
        if (validationError == null) {
            isSubmitting = true
            onSubmit(LoginRequest(identifier.trim(), password))
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Welcome back", style = MaterialTheme.typography.headlineLarge)
            Text("Sign in to your SolNex account.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(identifier, { identifier = it }, label = { Text("NIC or email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            result?.takeIf { !it.success }?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
            result?.takeIf { it.success }?.let { Text(it.message, color = MaterialTheme.colorScheme.primary) }
            Button(onClick = ::submit, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                if (isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Sign in")
            }
            TextButton(onClick = onRegister, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                Text("New prosumer? Register")
            }
        }
    }
}
