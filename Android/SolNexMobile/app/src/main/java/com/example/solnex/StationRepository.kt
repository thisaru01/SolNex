package com.example.solnex

import android.content.Context
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
    private val context: Context,
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val apiBaseUrl: String = if (android.os.Build.FINGERPRINT.contains("generic")) "http://10.0.2.2:5097" else "http://192.168.1.52:8080"
) {
    private val dbHelper = StationDatabaseHelper(context)

    // Fetches the list of all available stations from the backend API
    fun getStations(token: String, callback: (List<Station>?, String?) -> Unit) {
        executor.execute {
            // 1. First, load from SQLite and return to UI quickly
            val cachedStations = getCachedStations()
            if (cachedStations.isNotEmpty()) {
                callback(cachedStations, null)
            }

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
                    
                    // 2. Cache them
                    cacheStations(stations)
                    // 3. Update UI with fresh data
                    callback(stations, null)
                } else {
                    if (cachedStations.isEmpty()) {
                        callback(null, "Failed to load stations: HTTP $responseCode")
                    } else {
                        callback(cachedStations, "Failed to load stations: HTTP $responseCode")
                    }
                }
            } catch (e: Exception) {
                if (cachedStations.isEmpty()) {
                    callback(null, "Network error: ${e.message}")
                } else {
                    callback(cachedStations, "Network error: ${e.message}")
                }
            }
        }
    }

    private fun getCachedStations(): List<Station> {
        val stations = mutableListOf<Station>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("stations", null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val stationId = it.getString(it.getColumnIndexOrThrow("stationId"))
                val name = it.getString(it.getColumnIndexOrThrow("stationName"))
                val lat = it.getDouble(it.getColumnIndexOrThrow("latitude"))
                val lng = it.getDouble(it.getColumnIndexOrThrow("longitude"))
                val capacity = it.getDouble(it.getColumnIndexOrThrow("capacityKw"))
                val status = it.getString(it.getColumnIndexOrThrow("status"))
                stations.add(Station(id, stationId, name, lat, lng, capacity, status))
            }
        }
        return stations
    }

    private fun cacheStations(stations: List<Station>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM stations") // Clear old data
            val stmt = db.compileStatement(
                "INSERT INTO stations (id, stationId, stationName, latitude, longitude, capacityKw, status) VALUES (?, ?, ?, ?, ?, ?, ?)"
            )
            for (s in stations) {
                stmt.bindString(1, s.id)
                stmt.bindString(2, s.stationId)
                stmt.bindString(3, s.stationName)
                stmt.bindDouble(4, s.latitude)
                stmt.bindDouble(5, s.longitude)
                stmt.bindDouble(6, s.capacityKw)
                stmt.bindString(7, s.status)
                stmt.executeInsert()
                stmt.clearBindings()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    fun isFavorite(stationId: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query("favorites", arrayOf("stationId"), "stationId = ?", arrayOf(stationId), null, null, null)
        val isFav = cursor.count > 0
        cursor.close()
        return isFav
    }

    fun toggleFavorite(stationId: String, isFavorite: Boolean) {
        val db = dbHelper.writableDatabase
        if (isFavorite) {
            val values = android.content.ContentValues().apply { put("stationId", stationId) }
            db.insertWithOnConflict("favorites", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
        } else {
            db.delete("favorites", "stationId = ?", arrayOf(stationId))
        }
    }

    fun getFavoriteStationIds(): Set<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("favorites", arrayOf("stationId"), null, null, null, null, null)
        val ids = mutableSetOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                ids.add(it.getString(it.getColumnIndexOrThrow("stationId")))
            }
        }
        return ids
    }

    fun getFavoriteStations(): List<Station> {
        val favIds = getFavoriteStationIds()
        return getCachedStations().filter { favIds.contains(it.stationId) }
    }
}
