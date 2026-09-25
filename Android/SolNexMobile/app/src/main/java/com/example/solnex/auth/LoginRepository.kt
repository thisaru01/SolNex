package com.example.solnex.auth

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface LoginRepository {
    fun login(request: LoginRequest, callback: (LoginResult) -> Unit)
}

class ApiLoginRepository(
    private val tokenStore: TokenStore,
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = "http://10.0.2.2:5097"
) : LoginRepository {
    override fun login(request: LoginRequest, callback: (LoginResult) -> Unit) {
        executor.execute {
            val result = try {
                val connection = (URL("$apiBaseUrl/api/auth/login").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                }
                val payload = JSONObject().apply {
                    put("identifier", request.identifier)
                    put("password", request.password)
                }.toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val json = JSONObject(response)
                    val token = json.getString("token")
                    tokenStore.save(token, json.optString("nic"), json.optString("fullName"), json.optString("role"))
                    LoginResult(true, "Welcome back.", token, json.optString("fullName"), json.optString("role"))
                } else {
                    LoginResult(false, extractMessage(response) ?: "Invalid credentials or inactive account.")
                }
            } catch (_: Exception) {
                LoginResult(false, "Could not connect to SolNex. Check the API connection and try again.")
            }
            callback(result)
        }
    }

    private fun extractMessage(response: String): String? =
        runCatching { JSONObject(response).optString("message").takeIf { it.isNotBlank() } }.getOrNull()
}

class TokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("solnex_auth", Context.MODE_PRIVATE)

    fun save(token: String, nic: String, fullName: String, role: String) {
        preferences.edit()
            .putString("token", token)
            .putString("nic", nic)
            .putString("full_name", fullName)
            .putString("role", role)
            .apply()
    }

    fun token(): String? = preferences.getString("token", null)
    fun nic(): String? = preferences.getString("nic", null)
    fun fullName(): String? = preferences.getString("full_name", null)
    fun role(): String? = preferences.getString("role", null)

    fun updateProfile(fullName: String) {
        preferences.edit().putString("full_name", fullName).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}
