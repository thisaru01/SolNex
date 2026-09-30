package com.example.solnex.operator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.solnex.operator.model.Transaction

@Composable
fun OperatorTransactionHistoryScreen(
    transactions: List<Transaction>,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scanned Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transactions.size} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onRefresh,
                    enabled = !loading
                ) {
                    Text("Refresh")
                }
            }
        }

        if (loading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .then(Modifier),
                    strokeWidth = 3.dp
                )
            }
        }

        if (!error.isNullOrBlank()) {
            item {
                Text(
                    text = error,
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (!loading && transactions.isEmpty() && error.isNullOrBlank()) {
            item {
                Text(
                    text = "Scanned transactions will appear here.",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(
            items = transactions,
            key = { it.transactionId }
        ) { transaction ->
            ScannedTransactionCard(
                transaction = transaction,
                onClick = { onOpenTransaction(transaction) }
            )
        }
    }
}

@Composable
private fun ScannedTransactionCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val canComplete = transaction.status.equals(
        "Verified",
        ignoreCase = true
    )
    val canOpenDetails = canComplete || transaction.status.equals(
        "Completed",
        ignoreCase = true
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canOpenDetails,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = transaction.transactionId,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${transaction.energyAmountKwh} kWh  ·  ${transaction.stationId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Reservation ${transaction.reservationId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(transaction.status)
                    append("  ·  ")
                    append(
                        transaction.verifiedAt?.substringBefore('T')
                            ?: transaction.completedAt?.substringBefore('T')
                            ?: "Scanned"
                    )
                    if (canOpenDetails) {
                        append(
                            if (canComplete) {
                                "  ·  Tap to complete"
                            } else {
                                "  ·  Tap to view details"
                            }
                        )
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}