package com.example.smartvendors.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.domain.model.AuthState

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel(),
    onBackToLogin: () -> Unit = {}
) {

    var email by remember {
        mutableStateOf("")
    }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "SmartVendors",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Recuperar contraseña",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        when (authState) {

            AuthState.Loading -> {

                CircularProgressIndicator()
            }

            is AuthState.Success -> {

                Text(
                    text = (authState as AuthState.Success).message,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio de sesión")
                }
            }

            is AuthState.Error -> {

                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.sendPasswordReset(email)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar enlace")
                }
            }

            is AuthState.VerificationEmailSent -> {

                Text(
                    text = (authState as AuthState.VerificationEmailSent).message,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AuthState.Idle -> {

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.sendPasswordReset(email)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar enlace")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        TextButton(
            onClick = onBackToLogin
        ) {
            Text("Volver al inicio de sesión")
        }
    }
}