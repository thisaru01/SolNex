package com.example.solnex.operator.ui

import android.database.sqlite.SQLiteException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.solnex.operator.data.TransactionDatabaseHelper
import com.example.solnex.operator.data.TransactionApiClient
import com.example.solnex.operator.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OperatorModeScreen(
    modifier: Modifier = Modifier,
    token: String,
    operatorNic: String,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val apiClient = remember(token) { TransactionApiClient(token) }
    val transactionDatabase = remember(context) {
        TransactionDatabaseHelper(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(OperatorStep.SCANNER) }
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var scannedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }
    var historyRefresh by remember { mutableStateOf(0) }
    var fromHistory by remember { mutableStateOf(false) }

    LaunchedEffect(screen, historyRefresh, apiClient, operatorNic) {
        if (screen == OperatorStep.HISTORY) {
            historyLoading = true
            historyError = null
            try {
                scannedTransactions = withContext(Dispatchers.IO) {
                    transactionDatabase.getTransactions(operatorNic)
                }
            } catch (exception: SQLiteException) {
                historyError = "Unable to load cached transactions: ${exception.message}"
            }

            val result = withContext(Dispatchers.IO) {
                apiClient.getScannedTransactions()
            }
            result.onSuccess { transactions ->
                val operatorTransactions = transactions.map {
                    if (it.operatorNic == null) it.copy(operatorNic = operatorNic) else it
                }
                scannedTransactions = operatorTransactions
                historyError = null
                try {
                    withContext(Dispatchers.IO) {
                        transactionDatabase.saveTransactions(operatorNic, operatorTransactions)
                    }
                } catch (exception: SQLiteException) {
                    historyError = "Transactions loaded from the server, but could not be cached locally: ${exception.message}"
                }
            }.onFailure { exception ->
                val networkError = exception.message ?: "Unable to load scanned transactions."
                historyError = listOfNotNull(historyError, networkError).joinToString("\n")
            }
            historyLoading = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (screen == OperatorStep.SCANNER || screen == OperatorStep.HISTORY) {
            TabRow(
                selectedTabIndex = if (screen == OperatorStep.SCANNER) 0 else 1
            ) {
                Tab(
                    selected = screen == OperatorStep.SCANNER,
                    onClick = { screen = OperatorStep.SCANNER },
                    text = { Text("Scan QR") }
                )
                Tab(
                    selected = screen == OperatorStep.HISTORY,
                    onClick = { screen = OperatorStep.HISTORY },
                    text = { Text("History") }
                )
            }
        }

        when (screen) {
            OperatorStep.SCANNER -> {
                QrScannerScreen(
                    loading = loading,
                    error = error,
                    onQrScanned = { qrToken ->
                        loading = true
                        error = null
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                apiClient.verifyTransaction(qrToken, operatorNic)
                            }
                            loading = false
                            result
                                .onSuccess {
                                    transaction = it
                                    fromHistory = false
                                    screen = OperatorStep.DETAILS
                                    try {
                                        withContext(Dispatchers.IO) {
                                            transactionDatabase.saveTransactions(
                                                operatorNic,
                                                listOf(it)
                                            )
                                        }
                                    } catch (exception: SQLiteException) {
                                        error = "Transaction verified by the server, but could not be cached locally: ${exception.message}"
                                    }
                                }
                                .onFailure {
                                    error = it.message ?: "Transaction verification failed."
                                }
                        }
                    },
                    onBack = onExit
                )
            }

            OperatorStep.HISTORY -> {
                OperatorTransactionHistoryScreen(
                    transactions = scannedTransactions,
                    loading = historyLoading,
                    error = historyError,
                    onRefresh = { historyRefresh++ },
                    onOpenTransaction = {
                        transaction = it
                        fromHistory = true
                        error = null
                        screen = OperatorStep.DETAILS
                    }
                )
            }

            OperatorStep.DETAILS -> {
                val currentTransaction = transaction
                if (currentTransaction == null) {
                    screen = if (fromHistory) {
                        OperatorStep.HISTORY
                    } else {
                        OperatorStep.SCANNER
                    }
                } else {
                    TransactionDetailsScreen(
                        transaction = currentTransaction,
                        loading = loading,
                        error = error,
                        onComplete = {
                            loading = true
                            error = null
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    apiClient.completeTransaction(
                                        currentTransaction.transactionId,
                                        operatorNic
                                    )
                                }
                                loading = false
                                result
                                    .onSuccess {
                                        transaction = it
                                        screen = OperatorStep.COMPLETE
                                        try {
                                            withContext(Dispatchers.IO) {
                                                transactionDatabase.saveTransactions(
                                                    operatorNic,
                                                    listOf(it)
                                                )
                                            }
                                        } catch (exception: SQLiteException) {
                                            error = "Transaction completed by the server, but could not be cached locally: ${exception.message}"
                                        }
                                    }
                                    .onFailure {
                                        error = it.message
                                            ?: "Unable to complete energy transfer."
                                    }
                            }
                        },
                        onBack = {
                            transaction = null
                            error = null
                            screen = if (fromHistory) {
                                OperatorStep.HISTORY
                            } else {
                                OperatorStep.SCANNER
                            }
                        }
                    )
                }
            }

            OperatorStep.COMPLETE -> {
                val completedTransaction = transaction
                if (completedTransaction == null) {
                    screen = OperatorStep.SCANNER
                } else {
                    TransactionCompleteScreen(
                        transaction = completedTransaction,
                        error = error,
                        actionText = if (fromHistory) {
                            "Back to History"
                        } else {
                            "Scan Another Transaction"
                        },
                        onScanAnother = {
                            transaction = null
                            error = null
                            if (fromHistory) {
                                historyRefresh++
                                screen = OperatorStep.HISTORY
                            } else {
                                screen = OperatorStep.SCANNER
                            }
                        }
                    )
                }
            }
        }
    }
}

private enum class OperatorStep {
    SCANNER,
    HISTORY,
    DETAILS,
    COMPLETE
}