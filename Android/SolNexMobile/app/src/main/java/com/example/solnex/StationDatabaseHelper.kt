package com.example.solnex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StationDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "stations.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE stations (
                id TEXT PRIMARY KEY,
                stationId TEXT,
                stationName TEXT,
                latitude REAL,
                longitude REAL,
                capacityKw REAL,
                status TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE favorites (
                stationId TEXT PRIMARY KEY
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS favorites (
                    stationId TEXT PRIMARY KEY
                )
                """.trimIndent()
            )
        }
    }
}
