package com.example.solnex.auth

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalUserDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_NIC TEXT PRIMARY KEY,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_SAVED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(database)
    }

    fun savePendingUser(nic: String) {
        val values = ContentValues().apply {
            put(COLUMN_NIC, nic)
            put(COLUMN_STATUS, "Pending")
            put(COLUMN_SAVED_AT, System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    companion object {
        private const val DATABASE_NAME = "solnex_users.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "local_users"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_STATUS = "account_status"
        private const val COLUMN_SAVED_AT = "saved_at"
    }
}
