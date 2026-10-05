package com.example.solnex

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Repository for prosumer account deactivation operations.
 * Handles requesting account deactivation via API.
 */
interface DeactivationRepository {
    // Request account deactivation for prosumer
    fun requestDeactivation(nic: String, token: String, callback: (DeactivationResult) -> Unit)
}

sealed class DeactivationResult {
    object Success : DeactivationResult()
    data class Error(val message: String) : DeactivationResult()
}

class ApiDeactivationRepository(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = com.example.solnex.operator.data.ApiConfig.BASE_URL
) : DeactivationRepository {
    
    // Send deactivation request to API
    override fun requestDeactivation(nic: String, token: String, callback: (DeactivationResult) -> Unit) {
        executor.execute {
            val result = try {
                val connection = (URL("$apiBaseUrl/api/users/$nic/request-deactivation").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode in 200..299) {
                    DeactivationResult.Success
                } else {
                    DeactivationResult.Error(extractMessage(body) ?: "Deactivation request failed")
                }
            } catch (exception: Exception) {
                DeactivationResult.Error("Could not connect to SolNex. Check the API connection and try again.")
            }

            callback(result)
        }
    }

    // Extract error message from API response
    private fun extractMessage(response: String): String? {
        return runCatching { org.json.JSONObject(response).optString("message").takeIf { it.isNotBlank() } }.getOrNull()
    }
}
