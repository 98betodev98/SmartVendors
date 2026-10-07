package com.example.smartvendors.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    viewModel: InitialSetupViewModel,
    onSetupComplete: () -> Unit,
    onBack: () -> Unit
) {

    val nombreNegocio by
    viewModel.nombreNegocio.collectAsState()

    val categoriaVenta by
    viewModel.categoriaVenta.collectAsState()

    val state by
    viewModel.setupState.collectAsState()

    var expanded by remember {
        mutableStateOf(false)
    }

    val categorias = listOf(
        "Cosmética",
        "Cuidado personal",
        "Perfumería",
        "Maquillaje",
        "Accesorios"
    )

    LaunchedEffect(Unit) {
        viewModel.loadSetup()
    }

    LaunchedEffect(state) {

        if (
            state is InitialSetupState.Success
        ) {
            onSetupComplete()
            viewModel.clearState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Configuración inicial",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Configura tu información para comenzar a utilizar SmartVendors.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        OutlinedTextField(
            value = nombreNegocio,
            onValueChange = {
                viewModel.setNombreNegocio(it)
            },
            label = {
                Text("Nombre del negocio")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {

            OutlinedTextField(
                value = categoriaVenta,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Categoría de venta")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                categorias.forEach { categoria ->

                    DropdownMenuItem(
                        text = {
                            Text(categoria)
                        },
                        onClick = {

                            viewModel.setCategoriaVenta(
                                categoria
                            )

                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        when (state) {

            InitialSetupState.Loading -> {

                CircularProgressIndicator()
            }

            is InitialSetupState.Error -> {

                Text(
                    text =
                        (state as InitialSetupState.Error)
                            .message,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            InitialSetupState.Idle -> {
                // Sin mensaje
            }

            is InitialSetupState.Success -> {
                // La navegación se realiza automáticamente
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {
                viewModel.saveSetup()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Guardar configuración"
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Volver"
            )
        }
    }
}