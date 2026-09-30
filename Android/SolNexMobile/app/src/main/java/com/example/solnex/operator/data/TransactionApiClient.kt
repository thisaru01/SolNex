package com.example.solnex.operator.data

import com.example.solnex.operator.model.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class TransactionApiClient(
    private val token: String
) {

    /*
     * Sends the scanned QR token to the server.
     */
    fun verifyTransaction(
        qrToken: String,
        operatorNic: String
    ): Result<Transaction> {

        return runCatching {

            val body =
                JSONObject().apply {
                    put(
                        "qrToken",
                        qrToken
                    )

                    put(
                        "operatorNic",
                        operatorNic
                    )
                }

            val response =
                executeRequest(
                    method = "POST",
                    endpoint =
                        "/transactions/verify",
                    body = body
                )

            parseTransaction(
                JSONObject(
                    response
                )
            )
        }
    }

    /*
     * Completes a verified energy transfer.
     */
    fun completeTransaction(
        transactionId: String,
        operatorNic: String
    ): Result<Transaction> {

        return runCatching {

            val body =
                JSONObject().apply {
                    put(
                        "operatorNic",
                        operatorNic
                    )
                }

            val response =
                executeRequest(
                    method = "POST",
                    endpoint =
                        "/transactions/${transactionId.encodeUrlPart()}/complete",
                    body = body
                )

            parseTransaction(
                JSONObject(
                    response
                )
            )
        }
    }

    /*
     * Gets one transaction for an approved
     * Prosumer reservation.
     */
    fun getTransactionByReservation(
        reservationId: String
    ): Result<Transaction> {

        return runCatching {

            val response =
                executeRequest(
                    method = "GET",
                    endpoint =
                        "/transactions/reservation/${reservationId.encodeUrlPart()}"
                )

            parseTransaction(
                JSONObject(
                    response
                )
            )
        }
    }

    /*
     * Gets active approved reservation
     * transactions for the logged-in Prosumer.
     */
    fun getActiveProsumerTransactions():
        Result<List<Transaction>> {

        return runCatching {

            val response =
                executeRequest(
                    method = "GET",
                    endpoint =
                        "/transactions/prosumer/active"
                )

            val array =
                JSONArray(
                    response
                )

            buildList {

                for (
                    index in
                    0 until array.length()
                ) {

                    add(
                        parseTransaction(
                            array.getJSONObject(
                                index
                            )
                        )
                    )
                }
            }
        }
    }

    fun getScannedTransactions():
        Result<List<Transaction>> {

        return runCatching {

            val response =
                executeRequest(
                    method = "GET",
                    endpoint =
                        "/transactions/operator/scanned"
                )

            val array =
                JSONArray(
                    response
                )

            buildList {

                for (
                    index in
                    0 until array.length()
                ) {

                    add(
                        parseTransaction(
                            array.getJSONObject(
                                index
                            )
                        )
                    )
                }
            }
        }
    }

    /*
     * Executes REST requests to the
     * central SolNex API.
     */
    private fun executeRequest(
        method: String,
        endpoint: String,
        body: JSONObject? = null
    ): String {

        val url =
            URL(
                "${ApiConfig.BASE_URL}/api$endpoint"
            )

        val connection =
            url.openConnection()
                    as HttpURLConnection

        try {

            connection.requestMethod =
                method

            connection.connectTimeout =
                15_000

            connection.readTimeout =
                15_000

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )

            if (body != null) {

                connection.doOutput =
                    true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.outputStream
                    .use { output ->

                        output.write(
                            body
                                .toString()
                                .toByteArray(
                                    Charsets.UTF_8
                                )
                        )
                    }
            }

            val responseCode =
                connection.responseCode

            val stream =
                if (
                    responseCode in
                    200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response =
                stream
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }
                    .orEmpty()

            if (
                responseCode !in
                200..299
            ) {

                throw IllegalStateException(
                    extractErrorMessage(
                        response,
                        responseCode
                    )
                )
            }

            return response

        } finally {

            connection.disconnect()
        }
    }

    /*
     * Converts API JSON to the Android model.
     */
    private fun parseTransaction(
        json: JSONObject
    ): Transaction {

        return Transaction(

            id =
                nullableString(
                    json,
                    "id",
                    "Id"
                ),

            transactionId =
                requiredString(
                    json,
                    "transactionId",
                    "TransactionId"
                ),

            reservationId =
                requiredString(
                    json,
                    "reservationId",
                    "ReservationId"
                ),

            nic =
                requiredString(
                    json,
                    "nic",
                    "Nic"
                ),

            stationId =
                requiredString(
                    json,
                    "stationId",
                    "StationId"
                ),

            energyAmountKwh =
                if (
                    json.has(
                        "energyAmountKwh"
                    )
                ) {

                    json.optDouble(
                        "energyAmountKwh",
                        0.0
                    )

                } else {

                    json.optDouble(
                        "EnergyAmountKwh",
                        0.0
                    )
                },

            qrToken =
                nullableString(
                    json,
                    "qrToken",
                    "QrToken"
                ),

            status =
                requiredString(
                    json,
                    "status",
                    "Status"
                ),

            verifiedAt =
                nullableString(
                    json,
                    "verifiedAt",
                    "VerifiedAt"
                ),

            completedAt =
                nullableString(
                    json,
                    "completedAt",
                    "CompletedAt"
                ),

            operatorNic =
                nullableString(
                    json,
                    "operatorNic",
                    "OperatorNic"
                )
        )
    }

    private fun requiredString(
        json: JSONObject,
        firstKey: String,
        secondKey: String
    ): String {

        return nullableString(
            json,
            firstKey,
            secondKey
        ).orEmpty()
    }

    private fun nullableString(
        json: JSONObject,
        firstKey: String,
        secondKey: String
    ): String? {

        val key =
            when {

                json.has(
                    firstKey
                ) ->
                    firstKey

                json.has(
                    secondKey
                ) ->
                    secondKey

                else ->
                    return null
            }

        if (
            json.isNull(
                key
            )
        ) {
            return null
        }

        return json
            .optString(
                key
            )
            .takeIf {

                it.isNotBlank() &&
                    !it.equals(
                        "null",
                        ignoreCase =
                            true
                    )
            }
    }

    private fun extractErrorMessage(
        response: String,
        responseCode: Int
    ): String {

        return runCatching {

            val json =
                JSONObject(
                    response
                )

            json
                .optString(
                    "message"
                )
                .takeIf {
                    it.isNotBlank()
                }

        }.getOrNull()
            ?: "Request failed with status $responseCode."
    }

    private fun String.encodeUrlPart():
        String {

        return URLEncoder.encode(
            this,
            Charsets.UTF_8.name()
        )
    }
}