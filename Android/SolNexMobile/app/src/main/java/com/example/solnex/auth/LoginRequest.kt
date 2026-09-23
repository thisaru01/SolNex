package com.example.solnex.auth

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResult(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val fullName: String? = null,
    val role: String? = null
)
