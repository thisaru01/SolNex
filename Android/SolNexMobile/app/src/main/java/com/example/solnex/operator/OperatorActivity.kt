package com.example.solnex.operator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.solnex.auth.TokenStore
import com.example.solnex.operator.data.TransactionApiClient
import com.example.solnex.operator.model.Transaction
import com.example.solnex.operator.ui.OperatorHomeScreen
import com.example.solnex.operator.ui.QrScannerScreen
import com.example.solnex.operator.ui.TransactionCompleteScreen
import com.example.solnex.operator.ui.TransactionDetailsScreen
import com.example.solnex.ui.theme.SolNexTheme
import java.util.concurrent.Executors

class OperatorActivity :
    ComponentActivity() {

    private val tokenStore by lazy {
        TokenStore(this)
    }

    private val executor =
        Executors.newSingleThreadExecutor()

    private lateinit var apiClient:
        TransactionApiClient

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val token =
            tokenStore.token()

        val operatorNic =
            tokenStore.nic().orEmpty()

        val operatorName =
            tokenStore.fullName().orEmpty()

        val role =
            tokenStore.role().orEmpty()

        /*
         * Operator Mode can only be opened by
         * an authenticated Grid Operator.
         */
        if (
            token.isNullOrBlank() ||
            !role.equals(
                "GridOperator",
                ignoreCase = true
            )
        ) {
            finish()
            return
        }

        apiClient =
            TransactionApiClient(token)

        setContent {

            SolNexTheme {

                var screen by remember {
                    mutableStateOf(
                        OperatorScreen.HOME
                    )
                }

                var transaction by remember {
                    mutableStateOf<Transaction?>(
                        null
                    )
                }

                var loading by remember {
                    mutableStateOf(false)
                }

                var error by remember {
                    mutableStateOf<String?>(
                        null
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { 

                    when (screen) {

                        OperatorScreen.HOME -> {

                            OperatorHomeScreen(
                                operatorName =
                                    operatorName,

                                operatorNic =
                                    operatorNic,

                                onScanQr = {
                                    error = null
                                    screen =
                                        OperatorScreen.SCANNER
                                },

                                onBack = {
                                    finish()
                                }
                            )
                        }

                        OperatorScreen.SCANNER -> {

                            QrScannerScreen(
                                loading = loading,
                                error = error,

                                onQrScanned = {
                                        qrToken ->

                                    loading = true
                                    error = null

                                    executor.execute {

                                        val result =
                                            apiClient
                                                .verifyTransaction(
                                                    qrToken,
                                                    operatorNic
                                                )

                                        runOnUiThread {

                                            loading =
                                                false

                                            result
                                                .onSuccess {
                                                        verified ->

                                                    transaction =
                                                        verified

                                                    screen =
                                                        OperatorScreen.DETAILS
                                                }
                                                .onFailure {
                                                        exception ->

                                                    error =
                                                        exception.message
                                                            ?: "Transaction verification failed."
                                                }
                                        }
                                    }
                                },

                                onBack = {
                                    error = null
                                    screen =
                                        OperatorScreen.HOME
                                }
                            )
                        }

                        OperatorScreen.DETAILS -> {

                            val currentTransaction =
                                transaction

                            if (
                                currentTransaction ==
                                null
                            ) {

                                screen =
                                    OperatorScreen.HOME

                            } else {

                                TransactionDetailsScreen(
                                    transaction =
                                        currentTransaction,

                                    loading =
                                        loading,

                                    error =
                                        error,

                                    onComplete = {

                                        loading =
                                            true

                                        error =
                                            null

                                        executor.execute {

                                            val result =
                                                apiClient
                                                    .completeTransaction(
                                                        currentTransaction.transactionId,
                                                        operatorNic
                                                    )

                                            runOnUiThread {

                                                loading =
                                                    false

                                                result
                                                    .onSuccess {
                                                            completed ->

                                                        transaction =
                                                            completed

                                                        screen =
                                                            OperatorScreen.COMPLETE
                                                    }
                                                    .onFailure {
                                                            exception ->

                                                        error =
                                                            exception.message
                                                                ?: "Unable to complete energy transfer."
                                                    }
                                            }
                                        }
                                    },

                                    onBack = {
                                        error = null
                                        screen =
                                            OperatorScreen.HOME
                                    }
                                )
                            }
                        }

                        OperatorScreen.COMPLETE -> {

                            val completedTransaction =
                                transaction

                            if (
                                completedTransaction ==
                                null
                            ) {

                                screen =
                                    OperatorScreen.HOME

                            } else {

                                TransactionCompleteScreen(
                                    transaction =
                                        completedTransaction,

                                    onBackHome = {

                                        transaction =
                                            null

                                        error =
                                            null

                                        screen =
                                            OperatorScreen.HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Releases the background thread executor.
     */
    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}

private enum class OperatorScreen {
    HOME,
    SCANNER,
    DETAILS,
    COMPLETE
}