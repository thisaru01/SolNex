package com.example.solnex.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
 * Registration screen for new prosumer accounts.
 * Allows users to register with NIC as primary key and pending activation status.
 */
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
    var nicError by remember { mutableStateOf<String?>(null) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(result) {
        if (result != null) {
            isSubmitting = false
            snackbarHostState.showSnackbar(result.message)
        }
    }

    // Validate and submit registration form
    fun submit() {
        nicError = if (!nic.trim().matches(Regex("^(\\d{9}[VX]|\\d{12})$"))) "NIC must be 9 digits ending with V/X or 12 digits." else null
        fullNameError = if (fullName.trim().length < 2) "Enter your full name." else null
        emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Enter a valid email." else null
        val phoneDigits = phone.filter { it.isDigit() }
        phoneError = when {
            phoneDigits.startsWith("94") && phoneDigits.length != 11 -> "Phone must be +94 followed by 9 digits."
            phoneDigits.startsWith("0") && phoneDigits.length != 10 -> "Phone must be 0 followed by 9 digits."
            !phoneDigits.startsWith("94") && !phoneDigits.startsWith("0") -> "Phone must start with +94 or 0."
            else -> null
        }
        passwordError = if (password.length < 8) "Password must contain at least 8 characters." else null
        confirmPasswordError = if (password != confirmPassword) "Passwords do not match." else null

        if (nicError == null && fullNameError == null && emailError == null && phoneError == null && passwordError == null && confirmPasswordError == null) {
            isSubmitting = true
            onSubmit(RegisterRequest(nic.trim(), fullName.trim(), email.trim(), phoneDigits, password))
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            Text("Join SolNex", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Register as a solar prosumer. Your account will be pending until it is activated.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(nic, { 
                nic = it
                nicError = null
            }, label = { Text("National Identity Card") }, modifier = Modifier.fillMaxWidth(), singleLine = true, isError = nicError != null)
            nicError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            OutlinedTextField(fullName, { 
                fullName = it
                fullNameError = null
            }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, isError = fullNameError != null)
            fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            OutlinedTextField(email, { 
                email = it
                emailError = null
            }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = emailError != null)
            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            OutlinedTextField(phone, { 
                phone = formatPhoneNumber(it)
                phoneError = null
            }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), isError = phoneError != null)
            phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            OutlinedTextField(password, { 
                password = it
                passwordError = null
            }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { androidx.compose.material3.IconButton(onClick = { showPassword = !showPassword }) { androidx.compose.material3.Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (showPassword) "Hide password" else "Show password") } }, isError = passwordError != null)
            passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            OutlinedTextField(confirmPassword, { 
                confirmPassword = it
                confirmPasswordError = null
            }, label = { Text("Confirm password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { androidx.compose.material3.IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) { androidx.compose.material3.Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = if (showConfirmPassword) "Hide password" else "Show password") } }, isError = confirmPasswordError != null)
            confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

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
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}
