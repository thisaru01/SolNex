package com.example.solnex.auth

data class RegisterRequest(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

data class RegisterResult(
    val success: Boolean,
    val message: String
)
