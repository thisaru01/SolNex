package com.example.solnex

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Data class representing an available time slot for charging
data class Slot(
    val id: String,
    val slotId: String,
    val stationId: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: String?,
    val scheduleTime: String?
)

// Data class representing a user's reservation
data class Reservation(
    val id: String,
    val reservationId: String,
    val stationId: String,
    val slotId: String,
    val reservationDate: String,
    val energyAmountKwh: Double,
    val status: String,
    val startTime: String?,
    val endTime: String?,
    val dayOfWeek: String?
)

class ReservationRepository(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = com.example.solnex.operator.data.ApiConfig.BASE_URL
) {
    // Fetches all available slots from the backend API
    fun getAvailableSlots(token: String, callback: (List<Slot>?, String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/slots/available")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)
                    val slots = mutableListOf<Slot>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        
                        val id = obj.optString("id", "")
                        val slotId = obj.optString("slotId", "")
                        val stationId = obj.optString("stationId", "")
                        val startTime = obj.optString("startTime", "")
                        val endTime = obj.optString("endTime", "")
                        val dayOfWeek = obj.optString("dayOfWeek", "")
                        val scheduleTime = obj.optString("scheduleTime", "")
                        
                        slots.add(Slot(id, slotId, stationId, startTime, endTime, dayOfWeek, scheduleTime))
                    }
                    callback(slots, null)
                } else {
                    callback(null, "Failed to load slots: HTTP $responseCode")
                }
            } catch (e: Exception) {
                callback(null, "Network error: ${e.message}")
            }
        }
    }

    // Submits a new energy slot reservation to the backend
    fun submitReservation(
        token: String,
        nic: String,
        stationId: String,
        slotId: String,
        reservationDate: String,
        energyAmountKwh: Double,
        callback: (Boolean, String?) -> Unit
    ) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/reservations")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val jsonParam = JSONObject().apply {
                    put("nic", nic)
                    put("stationId", stationId)
                    put("slotId", slotId)
                    put("reservationDate", reservationDate)
                    put("energyAmountKwh", energyAmountKwh)
                }

                connection.outputStream.use { os ->
                    val input = jsonParam.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val createdId = try {
                        JSONObject(responseStr).optString("id", null)
                    } catch (e: Exception) {
                        null
                    }
                    callback(true, createdId)
                } else {
                    val errorMessage = try {
                        val errorStr = connection.errorStream.bufferedReader().use { it.readText() }
                        try {
                            val jsonObj = JSONObject(errorStr)
                            jsonObj.optString("message", errorStr)
                        } catch (e: Exception) {
                            errorStr
                        }
                    } catch (e: Exception) {
                        "Unknown error"
                    }
                    callback(false, errorMessage)
                }
            } catch (e: Exception) {
                callback(false, "Network error: ${e.message}")
            }
        }
    }

    // Fetches all reservations for a specific user (prosumer) by their NIC
    fun getReservationsByNic(token: String, nic: String, callback: (List<Reservation>?, String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/reservations/user/$nic")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)
                    val reservations = mutableListOf<Reservation>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        
                        val id = obj.optString("id", "")
                        val reservationId = obj.optString("reservationId", "")
                        val stationId = obj.optString("stationId", "")
                        val slotId = obj.optString("slotId", "")
                        val reservationDate = obj.optString("reservationDate", "")
                        val energyAmountKwh = obj.optDouble("energyAmountKwh", 0.0)
                        val status = obj.optString("status", "")
                        val startTime = obj.optString("startTime", "")
                        val endTime = obj.optString("endTime", "")
                        val dayOfWeek = obj.optString("dayOfWeek", "")
                        
                        reservations.add(
                            Reservation(
                                id = id,
                                reservationId = reservationId,
                                stationId = stationId,
                                slotId = slotId,
                                reservationDate = reservationDate,
                                energyAmountKwh = energyAmountKwh,
                                status = status,
                                startTime = startTime.takeIf { it.isNotBlank() },
                                endTime = endTime.takeIf { it.isNotBlank() },
                                dayOfWeek = dayOfWeek.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                    callback(reservations, null)
                } else {
                    callback(null, "Failed to load reservations: HTTP $responseCode")
                }
            } catch (e: Exception) {
                callback(null, "Network error: ${e.message}")
            }
        }
    }

    // Cancels a pending reservation
    fun cancelReservation(token: String, id: String, callback: (Boolean, String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/reservations/$id/cancel-request")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val errorMessage = try {
                        val errorStr = connection.errorStream.bufferedReader().use { it.readText() }
                        try {
                            val jsonObj = JSONObject(errorStr)
                            jsonObj.optString("message", errorStr)
                        } catch (e: Exception) { errorStr }
                    } catch (e: Exception) { "Unknown error" }
                    callback(false, errorMessage)
                }
            } catch (e: Exception) {
                callback(false, "Network error: ${e.message}")
            }
        }
    }

    // Deletes a reservation
    fun deleteReservation(token: String, id: String, callback: (Boolean, String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/reservations/$id")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val errorMessage = try {
                        val errorStr = connection.errorStream.bufferedReader().use { it.readText() }
                        try {
                            val jsonObj = JSONObject(errorStr)
                            jsonObj.optString("message", errorStr)
                        } catch (e: Exception) { errorStr }
                    } catch (e: Exception) { "Unknown error" }
                    callback(false, errorMessage)
                }
            } catch (e: Exception) {
                callback(false, "Network error: ${e.message}")
            }
        }
    }

    // Updates a reservation's details
    fun updateReservationDetails(
        token: String,
        id: String,
        slotId: String,
        energyAmountKwh: Double,
        callback: (Boolean, String?) -> Unit
    ) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/reservations/$id/details")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val jsonParam = JSONObject().apply {
                    put("slotId", slotId)
                    put("energyAmountKwh", energyAmountKwh)
                }

                connection.outputStream.use { os ->
                    val input = jsonParam.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val errorMessage = try {
                        val errorStr = connection.errorStream.bufferedReader().use { it.readText() }
                        try {
                            val jsonObj = JSONObject(errorStr)
                            jsonObj.optString("message", errorStr)
                        } catch (e: Exception) { errorStr }
                    } catch (e: Exception) { "Unknown error" }
                    callback(false, errorMessage)
                }
            } catch (e: Exception) {
                callback(false, "Network error: ${e.message}")
            }
        }
    }
}
