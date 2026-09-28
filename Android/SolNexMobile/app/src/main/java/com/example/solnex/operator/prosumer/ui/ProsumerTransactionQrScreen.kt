package com.example.solnex.operator.prosumer.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solnex.operator.model.Transaction
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ProsumerTransactionQrScreen(
    transaction: Transaction,
    onBack: () -> Unit
) {

    val qrBitmap =
        remember(
            transaction.qrToken
        ) {
            transaction.qrToken
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let(
                    ::generateQrBitmap
                )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Text(
            text = "Transaction QR",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Approved Energy Reservation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = transaction.status.uppercase(),
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 7.dp
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (
            qrBitmap != null &&
            !transaction.status.equals(
                "Completed",
                ignoreCase = true
            )
        ) {

            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 4.dp
                )
            ) {

                Box(
                    modifier = Modifier
                        .padding(20.dp)
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(
                            androidx.compose.ui.graphics.Color.White
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription =
                            "Secure transaction QR",
                        modifier = Modifier.size(
                            260.dp
                        )
                    )
                }
            }

            Text(
                text = "Show this QR code to the Grid Operator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "The QR contains a secure server-generated transaction token. Do not share it with anyone except the Grid Operator handling your energy transfer.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Reservation Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                QrDetail(
                    "Reservation ID",
                    transaction.reservationId
                )

                QrDetail(
                    "Transaction ID",
                    transaction.transactionId
                )

                QrDetail(
                    "Station",
                    transaction.stationId
                )

                QrDetail(
                    "Prosumer NIC",
                    transaction.nic
                )

                QrDetail(
                    "Energy Amount",
                    "${transaction.energyAmountKwh} kWh"
                )

                QrDetail(
                    "Status",
                    transaction.status
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Back to Approved Reservations")
        }
    }
}

@Composable
private fun QrDetail(
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

private fun generateQrBitmap(
    value: String
): Bitmap {

    val size = 700

    val matrix =
        QRCodeWriter()
            .encode(
                value,
                BarcodeFormat.QR_CODE,
                size,
                size
            )

    val bitmap =
        Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.RGB_565
        )

    for (x in 0 until size) {
        for (y in 0 until size) {

            bitmap.setPixel(
                x,
                y,
                if (matrix[x, y]) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }
            )
        }
    }

    return bitmap
}