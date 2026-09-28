package com.example.solnex.operator.prosumer

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.solnex.auth.TokenStore

import com.example.solnex.operator.data.TransactionApiClient
import com.example.solnex.operator.model.Transaction

import com.example.solnex.operator.prosumer.ui.ProsumerTransactionQrScreen

import com.example.solnex.ui.theme.SolNexTheme

class ProsumerTransactionQrActivity :
    ComponentActivity() {

    private val tokenStore by lazy {
        TokenStore(this)
    }

    private var transaction by
        mutableStateOf<
            Transaction?
        >(null)

    private var loading by
        mutableStateOf(true)

    private var error by
        mutableStateOf<
            String?
        >(null)

    override fun onCreate(
        savedInstanceState:
            Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        val reservationId =
            intent.getStringExtra(
                EXTRA_RESERVATION_ID
            )

        val token =
            tokenStore.token()

        val role =
            tokenStore
                .role()
                .orEmpty()

        if (
            reservationId
                .isNullOrBlank() ||
            token
                .isNullOrBlank() ||
            !role.equals(
                "Prosumer",
                ignoreCase = true
            )
        ) {

            finish()
            return
        }

        val apiClient =
            TransactionApiClient(
                token
            )

        loadTransaction(
            apiClient,
            reservationId
        )

        setContent {

            SolNexTheme {

                when {

                    loading -> {

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize(),

                            horizontalAlignment =
                                Alignment
                                    .CenterHorizontally,

                            verticalArrangement =
                                Arrangement
                                    .Center
                        ) {

                            CircularProgressIndicator()

                            Text(
                                modifier =
                                    Modifier
                                        .padding(
                                            16.dp
                                        ),

                                text =
                                    "Loading approved reservation transaction..."
                            )
                        }
                    }

                    error != null -> {

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        20.dp
                                    ),

                            verticalArrangement =
                                Arrangement
                                    .Center,

                            horizontalAlignment =
                                Alignment
                                    .CenterHorizontally
                        ) {

                            Text(
                                text =
                                    error
                                        .orEmpty(),

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )
                        }
                    }

                    transaction !=
                        null -> {

                        ProsumerTransactionQrScreen(
                            transaction =
                                transaction!!,

                            onBack = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun loadTransaction(
        apiClient:
            TransactionApiClient,

        reservationId:
            String
    ) {

        Thread {

            val result =
                apiClient
                    .getTransactionByReservation(
                        reservationId
                    )

            runOnUiThread {

                loading =
                    false

                result
                    .onSuccess {

                        transaction =
                            it
                    }
                    .onFailure {

                        error =
                            it.message
                                ?: "Unable to load the approved reservation transaction."
                    }
            }

        }.start()
    }

    companion object {

        const val EXTRA_RESERVATION_ID =
            "reservationId"
    }
}