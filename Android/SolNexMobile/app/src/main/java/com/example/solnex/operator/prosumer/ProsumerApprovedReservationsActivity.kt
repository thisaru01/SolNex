package com.example.solnex.operator.prosumer

import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.example.solnex.auth.TokenStore

import com.example.solnex.operator.data.TransactionApiClient
import com.example.solnex.operator.model.Transaction

import com.example.solnex.operator.prosumer.ui.ProsumerApprovedReservationsScreen

import com.example.solnex.ui.theme.SolNexTheme

class ProsumerApprovedReservationsActivity :
    ComponentActivity() {

    private val tokenStore by lazy {
        TokenStore(this)
    }

    private var transactions by
        mutableStateOf<
            List<Transaction>
        >(emptyList())

    private var loading by
        mutableStateOf(true)

    private var error by
        mutableStateOf<String?>(
            null
        )

    private lateinit var apiClient:
        TransactionApiClient

    override fun onCreate(
        savedInstanceState:
            Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        val token =
            tokenStore.token()

        val role =
            tokenStore
                .role()
                .orEmpty()

        /*
         * Only a logged-in Prosumer
         * can access this screen.
         */
        if (
            token.isNullOrBlank() ||
            !role.equals(
                "Prosumer",
                ignoreCase = true
            )
        ) {

            finish()
            return
        }

        apiClient =
            TransactionApiClient(
                token
            )

        loadTransactions()

        setContent {

            SolNexTheme {

                ProsumerApprovedReservationsScreen(
                    transactions =
                        transactions,

                    loading =
                        loading,

                    error =
                        error,

                    onRefresh =
                        ::loadTransactions,

                    onOpenTransaction =
                        ::openTransaction,

                    onBack = {
                        finish()
                    }
                )
            }
        }
    }

    /*
     * Gets all active approved
     * transactions for this Prosumer.
     */
    private fun loadTransactions() {

        loading =
            true

        error =
            null

        Thread {

            val result =
                apiClient
                    .getActiveProsumerTransactions()

            runOnUiThread {

                loading =
                    false

                result
                    .onSuccess {

                        transactions =
                            it
                    }
                    .onFailure {

                        error =
                            it.message
                                ?: "Unable to load approved reservations."
                    }
            }

        }.start()
    }

    /*
     * Opens the selected reservation's
     * secure transaction QR screen.
     */
    private fun openTransaction(
        transaction:
            Transaction
    ) {

        startActivity(

            Intent(
                this,
                ProsumerTransactionQrActivity::class.java
            ).apply {

                putExtra(
                    ProsumerTransactionQrActivity
                        .EXTRA_RESERVATION_ID,

                    transaction
                        .reservationId
                )
            }
        )
    }
}