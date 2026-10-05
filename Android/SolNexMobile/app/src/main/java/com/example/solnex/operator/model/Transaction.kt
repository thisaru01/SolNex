package com.example.solnex.operator.model

data class Transaction(
    val id: String? = null,
    val transactionId: String,
    val reservationId: String,
    val nic: String,
    val stationId: String,
    val energyAmountKwh: Double,
    val qrToken: String? = null,
    val status: String,
    val verifiedAt: String? = null,
    val completedAt: String? = null,
    val operatorNic: String? = null
)