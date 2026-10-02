package com.example.solnex.auth

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class ResetPasswordRequest(val identifier: String, val email: String, val newPassword: String)
data class ResetPasswordResult(val success: Boolean, val message: String)

class ApiResetPasswordRepository(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = com.example.solnex.operator.data.ApiConfig.BASE_URL
) {
    fun reset(request: ResetPasswordRequest, callback: (ResetPasswordResult) -> Unit) {
        executor.execute {
            val result = try {
                val connection = (URL("$apiBaseUrl/api/auth/reset-password").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                }
                val payload = JSONObject().apply {
                    put("identifier", request.identifier)
                    put("email", request.email)
                    put("newPassword", request.newPassword)
                }.toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()
                val message = runCatching {
                    val json = JSONObject(body)
                    json.optString("message").takeIf { it.isNotBlank() }
                        ?: json.optJSONObject("errors")?.let { errors ->
                            errors.keys().asSequence()
                                .mapNotNull { errors.optJSONArray(it)?.let { values -> (0 until values.length()).joinToString(" ") { index -> values.optString(index) } } }
                                .joinToString(" ")
                                .takeIf { it.isNotBlank() }
                        }
                }.getOrNull()
                ResetPasswordResult(responseCode in 200..299, message ?: "Password reset failed. Please try again.")
            } catch (_: Exception) {
                ResetPasswordResult(false, "Could not connect to SolNex. Check the API connection and try again.")
            }
            callback(result)
        }
    }
}