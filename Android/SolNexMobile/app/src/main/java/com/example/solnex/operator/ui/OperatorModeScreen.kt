package com.example.solnex.operator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    val apiClient = remember(token) { TransactionApiClient(token) }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(OperatorStep.SCANNER) }
    var transaction by remember { mutableStateOf<Transaction?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
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
                                    screen = OperatorStep.DETAILS
                                }
                                .onFailure {
                                    error = it.message ?: "Transaction verification failed."
                                }
                        }
                    },
                    onBack = onExit
                )
            }

            OperatorStep.DETAILS -> {
                val currentTransaction = transaction
                if (currentTransaction == null) {
                    screen = OperatorStep.SCANNER
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
                            screen = OperatorStep.SCANNER
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
                        onScanAnother = {
                            transaction = null
                            error = null
                            screen = OperatorStep.SCANNER
                        }
                    )
                }
            }
        }
    }
}

private enum class OperatorStep {
    SCANNER,
    DETAILS,
    COMPLETE
}