package com.example.smartvendors.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onBack: () -> Unit,
    performanceViewModel: PerformanceViewModel = viewModel()
) {

    val totalPedidos by performanceViewModel.totalPedidos.collectAsState()
    val ventasTotales by performanceViewModel.ventasTotales.collectAsState()
    val pedidosPendientes by performanceViewModel.pedidosPendientes.collectAsState()
    val unidadesVendidas by performanceViewModel.unidadesVendidas.collectAsState()
    val isLoading by performanceViewModel.isLoading.collectAsState()
    val error by performanceViewModel.error.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        performanceViewModel.loadPerformance()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi rendimiento")
                },
                navigationIcon = {
                    Button(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text(
                text = "Dashboard de rendimiento",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Consulta el resumen de tus ventas y pedidos.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cargando rendimiento...")
                }

            } else if (error != null) {

                Text(
                    text = error ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        performanceViewModel.refresh()
                    }
                ) {
                    Text("Reintentar")
                }

            } else {

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    item {
                        PerformanceCard(
                            titulo = "Ventas acumuladas",
                            valor = "S/ %.2f".format(ventasTotales)
                        )
                    }

                    item {
                        PerformanceCard(
                            titulo = "Pedidos registrados",
                            valor = totalPedidos.toString()
                        )
                    }

                    item {
                        PerformanceCard(
                            titulo = "Pedidos pendientes",
                            valor = pedidosPendientes.toString()
                        )
                    }

                    item {
                        PerformanceCard(
                            titulo = "Unidades vendidas",
                            valor = unidadesVendidas.toString()
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                performanceViewModel.refresh()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Actualizar rendimiento")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(
    titulo: String,
    valor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
