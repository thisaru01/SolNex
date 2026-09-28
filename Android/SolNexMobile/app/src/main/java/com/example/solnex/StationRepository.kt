package com.example.solnex

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class Station(
    val id: String,
    val stationId: String,
    val stationName: String,
    val latitude: Double,
    val longitude: Double,
    val capacityKw: Double,
    val status: String
)

class StationRepository(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = "http://10.0.2.2:5097"
) {
    fun getStations(token: String, callback: (List<Station>?, String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("$apiBaseUrl/api/stations")
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
                    val stations = mutableListOf<Station>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        
                        // Parse safely, some fields might be null or missing
                        val id = obj.optString("id", "")
                        val stationId = obj.optString("stationId", id)
                        val name = obj.optString("stationName", "Unknown Station")
                        val lat = obj.optDouble("latitude", Double.NaN)
                        val lng = obj.optDouble("longitude", Double.NaN)
                        val capacity = obj.optDouble("capacityKw", 0.0)
                        val status = obj.optString("status", "Unknown")
                        
                        if (!lat.isNaN() && !lng.isNaN()) {
                            stations.add(Station(id, stationId, name, lat, lng, capacity, status))
                        }
                    }
                    callback(stations, null)
                } else {
                    callback(null, "Failed to load stations: HTTP $responseCode")
                }
            } catch (e: Exception) {
                callback(null, "Network error: ${e.message}")
            }
        }
    }
}
