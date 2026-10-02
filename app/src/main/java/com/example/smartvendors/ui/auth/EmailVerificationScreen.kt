package com.example.smartvendors.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.domain.model.AuthState

@Composable
fun EmailVerificationScreen(
    email: String,
    viewModel: AuthViewModel = viewModel(),
    onVerificationSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {

    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "SmartVendors",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verifica tu correo",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hemos enviado un correo de verificación a:",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Revisa tu bandeja de entrada y confirma tu cuenta.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (authState) {

            AuthState.Loading -> {

                CircularProgressIndicator()
            }

            is AuthState.Success -> {

                val message =
                    (authState as AuthState.Success).message

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onVerificationSuccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar")
                }
            }

            is AuthState.Error -> {

                val message =
                    (authState as AuthState.Error).message

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.checkEmailVerification()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ya verifiqué mi correo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        viewModel.sendEmailVerification()
                    }
                ) {
                    Text("Reenviar correo")
                }
            }

            is AuthState.VerificationEmailSent -> {

                val message =
                    (authState as AuthState.VerificationEmailSent).message

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.checkEmailVerification()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ya verifiqué mi correo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        viewModel.sendEmailVerification()
                    }
                ) {
                    Text("Reenviar correo")
                }
            }

            AuthState.Idle -> {

                Button(
                    onClick = {
                        viewModel.checkEmailVerification()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ya verifiqué mi correo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        viewModel.sendEmailVerification()
                    }
                ) {
                    Text("Reenviar correo")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBackToLogin
        ) {
            Text("Volver al inicio de sesión")
        }
    }
}