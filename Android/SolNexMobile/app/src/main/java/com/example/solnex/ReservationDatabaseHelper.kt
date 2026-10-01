package com.example.solnex

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReservationDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "reservations.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE reservations (
                id TEXT PRIMARY KEY,
                reservationId TEXT,
                stationId TEXT,
                slotId TEXT,
                reservationDate TEXT,
                energyAmountKwh REAL,
                status TEXT,
                startTime TEXT,
                endTime TEXT,
                dayOfWeek TEXT,
                nic TEXT,
                isCancellationRejected INTEGER DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade logic if needed
        db.execSQL("DROP TABLE IF EXISTS reservations")
        onCreate(db)
    }
}
