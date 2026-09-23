package com.example.solnex.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    onSubmit: (RegisterRequest) -> Unit,
    onBackToLogin: () -> Unit,
    result: RegisterResult?
) {
    var nic by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result != null) {
            isSubmitting = false
        }
    }

    fun submit() {
        error = when {
            nic.trim().length < 5 -> "Enter a valid NIC."
            fullName.trim().length < 2 -> "Enter your full name."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email."
            phone.trim().isEmpty() -> "Enter your phone number."
            password.length < 8 -> "Password must contain at least 8 characters."
            password != confirmPassword -> "Passwords do not match."
            else -> null
        }
        if (error == null) {
            isSubmitting = true
            onSubmit(RegisterRequest(nic.trim(), fullName.trim(), email.trim(), phone.trim(), password))
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Join SolNex", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Register as a solar prosumer. Your account will be pending until it is activated.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(nic, { nic = it }, label = { Text("National Identity Card") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(fullName, { fullName = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            OutlinedTextField(confirmPassword, { confirmPassword = it }, label = { Text("Confirm password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            result?.let {
                Text(
                    it.message,
                    color = if (it.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Button(onClick = ::submit, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                if (isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Create account")
            }
            TextButton(onClick = onBackToLogin, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
                Text("Already registered? Sign in")
            }
        }
    }
}
