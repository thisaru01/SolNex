package com.example.solnex.operator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solnex.operator.model.Transaction

@Composable
fun TransactionCompleteScreen(
    transaction: Transaction,
    onBackHome: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = RoundedCornerShape(100.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "✓",
                modifier = Modifier.padding(
                    horizontal = 28.dp,
                    vertical = 20.dp
                ),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = "Transfer Completed",
            modifier = Modifier.padding(
                top = 22.dp
            ),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "The energy transaction was completed successfully and recorded by SolNex.",
            modifier = Modifier.padding(
                top = 8.dp
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 28.dp
                ),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 3.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                CompletionRow(
                    "Transaction ID",
                    transaction.transactionId
                )

                CompletionRow(
                    "Reservation ID",
                    transaction.reservationId
                )

                CompletionRow(
                    "Energy",
                    "${transaction.energyAmountKwh} kWh"
                )

                CompletionRow(
                    "Station",
                    transaction.stationId
                )

                CompletionRow(
                    "Status",
                    transaction.status
                )

                CompletionRow(
                    "Completed At",
                    transaction.completedAt
                        ?: "Completed"
                )
            }
        }

        Button(
            onClick = onBackHome,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 28.dp
                )
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Return to Operator Home",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CompletionRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}