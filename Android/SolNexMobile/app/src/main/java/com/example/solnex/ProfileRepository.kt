package com.example.solnex

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface ProfileRepository {
    fun loadProfile(nic: String, token: String, callback: (ProfileResult) -> Unit)
    fun saveProfile(nic: String, token: String, fullName: String, email: String, phone: String, callback: (ProfileResult) -> Unit)
}

data class UserProfile(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val accountStatus: String
)

sealed class ProfileResult {
    data class Success(val profile: UserProfile) : ProfileResult()
    data class Error(val message: String) : ProfileResult()
}

class ApiProfileRepository(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = "http://10.0.2.2:5097"
) : ProfileRepository {
    
    override fun loadProfile(nic: String, token: String, callback: (ProfileResult) -> Unit) {
        executor.execute {
            val result = try {
                val connection = (URL("$apiBaseUrl/api/users/$nic").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
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
                    val json = JSONObject(body)
                    val profile = UserProfile(
                        nic = json.optString("nic", nic),
                        fullName = json.optString("fullName", ""),
                        email = json.optString("email", ""),
                        phone = json.optString("phone", ""),
                        role = json.optString("role", ""),
                        accountStatus = json.optString("accountStatus", "Pending")
                    )
                    ProfileResult.Success(profile)
                } else {
                    ProfileResult.Error(extractMessage(body) ?: "Failed to load profile")
                }
            } catch (exception: Exception) {
                ProfileResult.Error("Could not connect to SolNex. Check the API connection and try again.")
            }

            callback(result)
        }
    }

    override fun saveProfile(nic: String, token: String, fullName: String, email: String, phone: String, callback: (ProfileResult) -> Unit) {
        executor.execute {
            val result = try {
                val payload = JSONObject().apply {
                    put("fullName", fullName)
                    put("email", email)
                    put("phone", phone)
                }.toString()

                val connection = (URL("$apiBaseUrl/api/users/$nic").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode in 200..299) {
                    val json = JSONObject(body)
                    val profile = UserProfile(
                        nic = json.optString("nic", nic),
                        fullName = json.optString("fullName", fullName),
                        email = json.optString("email", email),
                        phone = json.optString("phone", phone),
                        role = json.optString("role", ""),
                        accountStatus = json.optString("accountStatus", "")
                    )
                    ProfileResult.Success(profile)
                } else {
                    ProfileResult.Error(extractMessage(body) ?: "Failed to update profile")
                }
            } catch (exception: Exception) {
                ProfileResult.Error("Could not connect to SolNex. Check the API connection and try again.")
            }

            callback(result)
        }
    }

    private fun extractMessage(response: String): String? {
        return runCatching { JSONObject(response).optString("message").takeIf { it.isNotBlank() } }.getOrNull()
    }
}
