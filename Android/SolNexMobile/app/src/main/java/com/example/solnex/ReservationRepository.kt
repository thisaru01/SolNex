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
    private val apiBaseUrl: String = "http://10.0.2.2:5097"
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
                    callback(true, null)
                } else {
                    val errorResponse = try {
                        connection.errorStream.bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        "Unknown error"
                    }
                    callback(false, "Failed to submit: HTTP $responseCode - $errorResponse")
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
}
