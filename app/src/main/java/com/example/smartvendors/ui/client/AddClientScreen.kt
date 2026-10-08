package com.example.smartvendors.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.domain.model.Client

@Composable
fun AddClientScreen(
    onBack: () -> Unit,
    onClientSaved: () -> Unit,
    viewModel: ClientViewModel = viewModel()
) {

    var nombre by remember {
        mutableStateOf("")
    }

    var apellido by remember {
        mutableStateOf("")
    }

    var documento by remember {
        mutableStateOf("")
    }

    var telefono by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var direccion by remember {
        mutableStateOf("")
    }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onClientSaved()
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Nuevo cliente",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                Text("Nombre")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.padding(6.dp)
        )

        OutlinedTextField(
            value = apellido,
            onValueChange = {
                apellido = it
            },
            label = {
                Text("Apellido")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.padding(6.dp)
        )

        OutlinedTextField(
            value = documento,
            onValueChange = {
                documento = it
            },
            label = {
                Text("Documento")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.padding(6.dp)
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
            },
            label = {
                Text("Teléfono")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.padding(6.dp)
        )

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
            },
            label = {
                Text("Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.padding(6.dp)
        )

        OutlinedTextField(
            value = direccion,
            onValueChange = {
                direccion = it
            },
            label = {
                Text("Dirección")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )

        Spacer(
            modifier = Modifier.padding(16.dp)
        )

        if (isLoading) {

            CircularProgressIndicator()

        } else {

            Button(
                onClick = {

                    val client = Client(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        documento = documento.trim(),
                        telefono = telefono.trim(),
                        correo = correo.trim(),
                        direccion = direccion.trim()
                    )

                    viewModel.addClient(client)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar cliente")
            }
        }

        Spacer(
            modifier = Modifier.padding(6.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        errorMessage?.let { message ->

            Spacer(
                modifier = Modifier.padding(6.dp)
            )

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}