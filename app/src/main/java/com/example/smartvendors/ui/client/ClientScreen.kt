package com.example.smartvendors.ui.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.domain.model.Client

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.smartvendors.ui.theme.SmartVendorsClientDelete
import com.example.smartvendors.ui.theme.SmartVendorsClientEdit

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    onBack: () -> Unit,
    onAddClient: () -> Unit,
    onEditClient: (Client) -> Unit,
    viewModel: ClientViewModel = viewModel()
) {

    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis clientes")
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

                errorMessage != null -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = errorMessage
                                ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )

                        Button(
                            onClick = {
                                viewModel.clearMessages()
                                viewModel.loadClients()
                            },
                            modifier = Modifier.padding(
                                top = 16.dp
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        item {

                            Button(
                                onClick = onAddClient,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Nuevo cliente")
                            }
                        }

                        if (clients.isEmpty()) {

                            item {

                                Text(
                                    text = "No tienes clientes registrados.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = 24.dp
                                        ),
                                    style = MaterialTheme
                                        .typography
                                        .bodyLarge
                                )
                            }

                        } else {

                            items(clients) { client ->

                                ClientCard(
                                    client = client,
                                    onEdit = {
                                        onEditClient(client)
                                    },
                                    onDelete = {
                                        viewModel.deleteClient(client.id)
                                    }
                                )
                            }
                        }

                        item {

                            Button(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Volver")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    client: Client,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "${client.nombre} ${client.apellido}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Documento: ${client.documento}",
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Teléfono: ${client.telefono}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Correo: ${client.correo}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Dirección: ${client.direccion}",
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(
                modifier = Modifier.padding(6.dp)
            )

            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SmartVendorsClientEdit
                )
            ) {
                Text("Editar")
            }

            Spacer(
                modifier = Modifier.padding(4.dp)
            )

            Button(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SmartVendorsClientDelete
                )
            ) {
                Text("Eliminar")
            }
        }
    }

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Eliminar cliente")
            },
            text = {
                Text(
                    "¿Está seguro de que desea eliminar a " +
                            "${client.nombre} ${client.apellido}?"
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}