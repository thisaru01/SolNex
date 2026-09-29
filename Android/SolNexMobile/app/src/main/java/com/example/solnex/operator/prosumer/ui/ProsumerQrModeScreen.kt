package com.example.solnex.operator.prosumer.ui

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
import com.example.solnex.operator.data.TransactionApiClient
import com.example.solnex.operator.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProsumerQrModeScreen(
    modifier: Modifier = Modifier,
    token: String
) {
    val apiClient = remember(token) { TransactionApiClient(token) }
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
            val result = withContext(Dispatchers.IO) {
                apiClient.getActiveProsumerTransactions()
            }
            loadingReservations = false
            result
                .onSuccess { transactions = it }
                .onFailure {
                    error = it.message ?: "Unable to load approved reservations."
                }
        }
    }

    fun openTransaction(transaction: Transaction) {
        loadingQr = true
        error = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                apiClient.getTransactionByReservation(transaction.reservationId)
            }
            loadingQr = false
            result
                .onSuccess { selectedTransaction = it }
                .onFailure {
                    error = it.message
                        ?: "Unable to load the approved reservation transaction."
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