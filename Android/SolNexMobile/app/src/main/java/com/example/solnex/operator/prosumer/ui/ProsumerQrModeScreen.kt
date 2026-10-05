package com.example.solnex.operator.prosumer.ui

import android.database.sqlite.SQLiteException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solnex.operator.data.TransactionDatabaseHelper
import com.example.solnex.operator.data.TransactionApiClient
import com.example.solnex.operator.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProsumerQrModeScreen(
    modifier: Modifier = Modifier,
    token: String,
    prosumerNic: String
) {
    val context = LocalContext.current
    val apiClient = remember(token) { TransactionApiClient(token) }
    val transactionDatabase = remember(context) {
        TransactionDatabaseHelper(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var loadingReservations by remember { mutableStateOf(true) }
    var loadingQr by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refreshReservations() {
        loadingReservations = true
        error = null
        scope.launch {
            try {
                val cachedTransactions = withContext(Dispatchers.IO) {
                    transactionDatabase.getActiveProsumerTransactions(prosumerNic)
                }
                if (cachedTransactions.isNotEmpty()) {
                    val mergedTransactions = buildMap {
                        transactions.forEach { put(it.reservationId, it) }
                        cachedTransactions.forEach { put(it.reservationId, it) }
                    }.values.toList()
                    transactions = mergedTransactions
                    withContext(Dispatchers.IO) {
                        transactionDatabase.cacheActiveProsumerTransactions(
                            prosumerNic,
                            mergedTransactions
                        )
                    }
                } else if (transactions.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        transactionDatabase.cacheActiveProsumerTransactions(
                            prosumerNic,
                            transactions
                        )
                    }
                }
            } catch (exception: SQLiteException) {
                error = "Unable to load or save cached reservations: ${exception.message}"
            }

            val result = withContext(Dispatchers.IO) {
                apiClient.getActiveProsumerTransactions()
            }
            loadingReservations = false
            result.onSuccess { activeTransactions ->
                val displayedTransactions = buildMap {
                    transactions.forEach { put(it.reservationId, it) }
                    activeTransactions.forEach { put(it.reservationId, it) }
                }.values.toList()
                transactions = displayedTransactions
                error = null
                try {
                    withContext(Dispatchers.IO) {
                        transactionDatabase.cacheActiveProsumerTransactions(
                            prosumerNic,
                            displayedTransactions
                        )
                    }
                } catch (exception: SQLiteException) {
                    error = "Reservations loaded from the server, but could not be cached locally: ${exception.message}"
                }
            }.onFailure { exception ->
                val networkError = exception.message ?: "Unable to load approved reservations."
                error = listOfNotNull(error, networkError).joinToString("\n")
            }
        }
    }

    fun openTransaction(transaction: Transaction) {
        loadingQr = true
        error = null
        scope.launch {
            val cachedTransaction = try {
                withContext(Dispatchers.IO) {
                    transactionDatabase.getProsumerTransactionByReservation(
                        prosumerNic,
                        transaction.reservationId
                    )
                }
            } catch (exception: SQLiteException) {
                error = "Unable to load the cached reservation: ${exception.message}"
                null
            }
            if (cachedTransaction != null) {
                selectedTransaction = cachedTransaction
            }

            val result = withContext(Dispatchers.IO) {
                apiClient.getTransactionByReservation(transaction.reservationId)
            }
            loadingQr = false
            result.onSuccess { freshTransaction ->
                selectedTransaction = freshTransaction
                transactions = transactions.map {
                    if (it.reservationId == freshTransaction.reservationId) {
                        freshTransaction.copy(qrToken = null)
                    } else {
                        it
                    }
                }
                try {
                    withContext(Dispatchers.IO) {
                        transactionDatabase.saveProsumerTransaction(prosumerNic, freshTransaction)
                    }
                } catch (exception: SQLiteException) {
                    error = "Secure QR loaded from the server, but transaction details could not be cached locally: ${exception.message}"
                }
            }.onFailure { exception ->
                if (cachedTransaction != null) {
                    error = "Showing saved transaction details. Reconnect to load its secure QR."
                } else if (error == null) {
                    error = exception.message
                        ?: "Unable to load the approved reservation transaction."
                }
            }
        }
    }

    LaunchedEffect(apiClient) {
        refreshReservations()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            selectedTransaction != null -> {
                ProsumerTransactionQrScreen(
                    transaction = selectedTransaction!!,
                    error = error,
                    onBack = {
                        selectedTransaction = null
                        error = null
                    }
                )
            }
            loadingQr -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading approved reservation transaction...")
                }
            }
            else -> {
                ProsumerApprovedReservationsScreen(
                    transactions = transactions,
                    loading = loadingReservations,
                    error = error,
                    onRefresh = ::refreshReservations,
                    onOpenTransaction = ::openTransaction
                )
            }
        }
    }
}