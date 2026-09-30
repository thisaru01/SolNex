package com.example.solnex.operator.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.solnex.operator.model.Transaction
import java.io.File
import java.util.Locale

class TransactionDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext = context.applicationContext

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE $TABLE_TRANSACTIONS (
                transaction_id TEXT NOT NULL,
                operator_nic TEXT NOT NULL,
                id TEXT,
                reservation_id TEXT NOT NULL,
                nic TEXT NOT NULL,
                station_id TEXT NOT NULL,
                energy_amount_kwh REAL NOT NULL,
                status TEXT NOT NULL,
                verified_at TEXT,
                completed_at TEXT,
                PRIMARY KEY (transaction_id, operator_nic)
            )
            """.trimIndent()
        )
        createProsumerTransactionsTable(database)
        migrateLegacyProsumerTransactions(database)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createProsumerTransactionsTable(database)
        }
        if (oldVersion < 4) {
            migrateLegacyProsumerTransactions(database)
        }
    }

    fun saveTransactions(operatorNic: String, transactions: List<Transaction>) {
        require(operatorNic.isNotBlank()) { "Operator NIC is required to cache transactions." }

        val database = writableDatabase
        database.beginTransaction()
        try {
            transactions.forEach { transaction ->
                val values = ContentValues().apply {
                    put(COLUMN_TRANSACTION_ID, transaction.transactionId)
                    put(COLUMN_OPERATOR_NIC, operatorNic)
                    put(COLUMN_ID, transaction.id)
                    put(COLUMN_RESERVATION_ID, transaction.reservationId)
                    put(COLUMN_NIC, transaction.nic)
                    put(COLUMN_STATION_ID, transaction.stationId)
                    put(COLUMN_ENERGY_AMOUNT_KWH, transaction.energyAmountKwh)
                    put(COLUMN_STATUS, transaction.status)
                    put(COLUMN_VERIFIED_AT, transaction.verifiedAt)
                    put(COLUMN_COMPLETED_AT, transaction.completedAt)
                }
                if (
                    database.insertWithOnConflict(
                        TABLE_TRANSACTIONS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    ) == -1L
                ) {
                    throw SQLiteException(
                        "Unable to save transaction ${transaction.transactionId} locally."
                    )
                }
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    fun getTransactions(operatorNic: String): List<Transaction> {
        require(operatorNic.isNotBlank()) { "Operator NIC is required to load cached transactions." }

        val transactions = mutableListOf<Transaction>()
        readableDatabase.query(
            TABLE_TRANSACTIONS,
            null,
            "$COLUMN_OPERATOR_NIC = ?",
            arrayOf(operatorNic),
            null,
            null,
            "$COLUMN_COMPLETED_AT DESC, $COLUMN_VERIFIED_AT DESC, $COLUMN_TRANSACTION_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                transactions += Transaction(
                    id = cursor.getNullableString(COLUMN_ID),
                    transactionId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                    reservationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESERVATION_ID)),
                    nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                    stationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATION_ID)),
                    energyAmountKwh = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COLUMN_ENERGY_AMOUNT_KWH)
                    ),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    verifiedAt = cursor.getNullableString(COLUMN_VERIFIED_AT),
                    completedAt = cursor.getNullableString(COLUMN_COMPLETED_AT),
                    operatorNic = operatorNic
                )
            }
        }
        return transactions
    }

    fun cacheActiveProsumerTransactions(
        prosumerNic: String,
        transactions: List<Transaction>
    ) {
        val cacheKey = normalizedProsumerNic(prosumerNic)

        val database = writableDatabase
        database.beginTransaction()
        try {
            transactions.forEach { transaction ->
                insertProsumerTransaction(database, cacheKey, transaction)
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    fun saveProsumerTransaction(prosumerNic: String, transaction: Transaction) {
        insertProsumerTransaction(
            writableDatabase,
            normalizedProsumerNic(prosumerNic),
            transaction
        )
    }

    fun getActiveProsumerTransactions(prosumerNic: String): List<Transaction> {
        val cacheKey = normalizedProsumerNic(prosumerNic)

        val transactions = mutableListOf<Transaction>()
        readableDatabase.query(
            TABLE_PROSUMER_TRANSACTIONS,
            null,
            "UPPER(TRIM($COLUMN_PROSUMER_NIC)) = ?",
            arrayOf(cacheKey),
            null,
            null,
            "$COLUMN_RESERVATION_ID"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                transactions += cursor.toTransaction()
            }
        }
        return transactions
    }

    fun getProsumerTransactionByReservation(
        prosumerNic: String,
        reservationId: String
    ): Transaction? {
        val cacheKey = normalizedProsumerNic(prosumerNic)

        return readableDatabase.query(
            TABLE_PROSUMER_TRANSACTIONS,
            null,
            "UPPER(TRIM($COLUMN_PROSUMER_NIC)) = ? AND $COLUMN_RESERVATION_ID = ?",
            arrayOf(cacheKey, reservationId),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toTransaction() else null
        }
    }

    private fun insertProsumerTransaction(
        database: SQLiteDatabase,
        prosumerNic: String,
        transaction: Transaction
    ) {
        val values = ContentValues().apply {
            put(COLUMN_PROSUMER_NIC, prosumerNic)
            put(COLUMN_TRANSACTION_ID, transaction.transactionId)
            put(COLUMN_ID, transaction.id)
            put(COLUMN_RESERVATION_ID, transaction.reservationId)
            put(COLUMN_NIC, transaction.nic)
            put(COLUMN_STATION_ID, transaction.stationId)
            put(COLUMN_ENERGY_AMOUNT_KWH, transaction.energyAmountKwh)
            put(COLUMN_STATUS, transaction.status)
            put(COLUMN_VERIFIED_AT, transaction.verifiedAt)
            put(COLUMN_COMPLETED_AT, transaction.completedAt)
            put(COLUMN_OPERATOR_NIC, transaction.operatorNic)
        }
        if (
            database.insertWithOnConflict(
                TABLE_PROSUMER_TRANSACTIONS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            ) == -1L
        ) {
            throw SQLiteException(
                "Unable to save transaction ${transaction.transactionId} locally."
            )
        }
    }

    private fun android.database.Cursor.toTransaction(): Transaction =
        Transaction(
            id = getNullableString(COLUMN_ID),
            transactionId = getString(getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
            reservationId = getString(getColumnIndexOrThrow(COLUMN_RESERVATION_ID)),
            nic = getString(getColumnIndexOrThrow(COLUMN_NIC)),
            stationId = getString(getColumnIndexOrThrow(COLUMN_STATION_ID)),
            energyAmountKwh = getDouble(getColumnIndexOrThrow(COLUMN_ENERGY_AMOUNT_KWH)),
            status = getString(getColumnIndexOrThrow(COLUMN_STATUS)),
            verifiedAt = getNullableString(COLUMN_VERIFIED_AT),
            completedAt = getNullableString(COLUMN_COMPLETED_AT),
            operatorNic = getNullableString(COLUMN_OPERATOR_NIC)
        )

    private fun normalizedProsumerNic(prosumerNic: String): String {
        require(prosumerNic.isNotBlank()) { "Prosumer NIC is required to cache transactions." }
        return prosumerNic.trim().uppercase(Locale.ROOT)
    }

    private fun android.database.Cursor.getNullableString(columnName: String): String? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getString(index)
    }

    private fun createProsumerTransactionsTable(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_PROSUMER_TRANSACTIONS (
                prosumer_nic TEXT NOT NULL,
                transaction_id TEXT NOT NULL,
                id TEXT,
                reservation_id TEXT NOT NULL,
                nic TEXT NOT NULL,
                station_id TEXT NOT NULL,
                energy_amount_kwh REAL NOT NULL,
                status TEXT NOT NULL,
                verified_at TEXT,
                completed_at TEXT,
                operator_nic TEXT,
                PRIMARY KEY (prosumer_nic, transaction_id)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS transactions_by_prosumer_reservation
            ON $TABLE_PROSUMER_TRANSACTIONS ($COLUMN_PROSUMER_NIC, $COLUMN_RESERVATION_ID)
            """.trimIndent()
        )
    }

    private fun migrateLegacyProsumerTransactions(database: SQLiteDatabase) {
        val legacyFile = appContext.getDatabasePath(LEGACY_PROSUMER_DATABASE_NAME)
        if (!legacyFile.isFile) return

        val legacyDatabase = SQLiteDatabase.openDatabase(
            legacyFile.path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        try {
            val hasTransactionsTable = legacyDatabase.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?",
                arrayOf(LEGACY_TRANSACTIONS_TABLE)
            ).use { it.moveToFirst() }
            if (!hasTransactionsTable) return

            val databaseTransactionActive = database.inTransaction()
            if (!databaseTransactionActive) database.beginTransaction()
            try {
                legacyDatabase.query(
                    LEGACY_TRANSACTIONS_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        insertProsumerTransaction(
                            database,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROSUMER_NIC)),
                            Transaction(
                                id = cursor.getNullableString(COLUMN_ID),
                                transactionId = cursor.getString(
                                    cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)
                                ),
                                reservationId = cursor.getString(
                                    cursor.getColumnIndexOrThrow(COLUMN_RESERVATION_ID)
                                ),
                                nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                                stationId = cursor.getString(
                                    cursor.getColumnIndexOrThrow(COLUMN_STATION_ID)
                                ),
                                energyAmountKwh = cursor.getDouble(
                                    cursor.getColumnIndexOrThrow(COLUMN_ENERGY_AMOUNT_KWH)
                                ),
                                status = cursor.getString(
                                    cursor.getColumnIndexOrThrow(COLUMN_STATUS)
                                ),
                                verifiedAt = cursor.getNullableString(COLUMN_VERIFIED_AT),
                                completedAt = cursor.getNullableString(COLUMN_COMPLETED_AT),
                                operatorNic = cursor.getNullableString(COLUMN_OPERATOR_NIC)
                            )
                        )
                    }
                }
                if (!databaseTransactionActive) database.setTransactionSuccessful()
            } finally {
                if (!databaseTransactionActive) database.endTransaction()
            }
        } finally {
            legacyDatabase.close()
        }
    }

    companion object {
        private const val DATABASE_NAME = "solnex_transactions.db"
        private const val DATABASE_VERSION = 4
        private const val LEGACY_PROSUMER_DATABASE_NAME = "solnex_prosumer_transactions.db"
        private const val LEGACY_TRANSACTIONS_TABLE = "transactions"
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_PROSUMER_TRANSACTIONS = "prosumer_transactions"
        private const val COLUMN_TRANSACTION_ID = "transaction_id"
        private const val COLUMN_OPERATOR_NIC = "operator_nic"
        private const val COLUMN_PROSUMER_NIC = "prosumer_nic"
        private const val COLUMN_ID = "id"
        private const val COLUMN_RESERVATION_ID = "reservation_id"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_STATION_ID = "station_id"
        private const val COLUMN_ENERGY_AMOUNT_KWH = "energy_amount_kwh"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_VERIFIED_AT = "verified_at"
        private const val COLUMN_COMPLETED_AT = "completed_at"
    }
}
