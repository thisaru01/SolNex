package com.example.solnex.auth

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface RegisterRepository {
    fun register(request: RegisterRequest, callback: (RegisterResult) -> Unit)
}

class ApiRegisterRepository(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = "http://10.0.2.2:5097"
) : RegisterRepository {
    override fun register(request: RegisterRequest, callback: (RegisterResult) -> Unit) {
        executor.execute {
            val result = try {
                val connection = (URL("$apiBaseUrl/api/users/register").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                }

                val payload = JSONObject().apply {
                    put("nic", request.nic)
                    put("fullName", request.fullName)
                    put("email", request.email)
                    put("phone", request.phone)
                    put("password", request.password)
                }.toString()

                connection.outputStream.use { output ->
                    output.write(payload.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val response = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    RegisterResult(true, "Registration submitted. Your account is pending activation.")
                } else {
                    RegisterResult(false, extractMessage(response) ?: "Registration failed. Please try again.")
                }
            } catch (exception: Exception) {
                RegisterResult(false, "Could not connect to SolNex. Check the API connection and try again.")
            }

            callback(result)
        }
    }

    private fun extractMessage(response: String): String? {
        return runCatching { JSONObject(response).optString("message").takeIf { it.isNotBlank() } }.getOrNull()
    }
}
