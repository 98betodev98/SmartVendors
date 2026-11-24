
package com.example.smartvendors.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesStatisticsScreen(
    onBack: () -> Unit,
    viewModel: SalesStatisticsViewModel = viewModel()
) {

    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {

        Text(
            text = "Estadísticas de ventas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Resumen de tus pedidos registrados",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Cargando estadísticas...")
                }
            }

            error != null -> {
                Column {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(onClick = { viewModel.loadStatistics() }) {
                        Text("Reintentar")
                    }
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // TARJETAS DE RESUMEN

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatisticCard(
                                title = "Pedidos",
                                value = statistics.totalOrders.toString(),
                                modifier = Modifier.weight(1f)
                            )

                            StatisticCard(
                                title = "Pendientes",
                                value = statistics.pendingOrders.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatisticCard(
                                title = "Monto acumulado",
                                value = formatCurrency(statistics.totalAmount),
                                modifier = Modifier.weight(1f)
                            )

                            StatisticCard(
                                title = "Ventas del mes",
                                value = formatCurrency(statistics.monthlyAmount),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        StatisticCard(
                            title = "Unidades solicitadas",
                            value = statistics.totalUnits.toString(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // GRAFICA 1: VENTAS POR PERIODO

                    item {
                        LineChartCard(
                            title = "Evolución de ventas",
                            description = "Ingresos de los últimos seis meses",
                            points = statistics.monthlySales
                        )
                    }

                    // GRAFICA 2: PRODUCTOS MAS VENDIDOS

                    item {
                        BarChartCard(
                            title = "Productos más vendidos",
                            description = "Unidades solicitadas por producto",
                            products = statistics.topProductsByUnits,
                            useRevenue = false
                        )
                    }

                    // GRAFICA 3: INGRESOS POR PRODUCTO

                    item {
                        BarChartCard(
                            title = "Ingresos por producto",
                            description = "Ingresos generados por cada producto",
                            products = statistics.topProductsByRevenue,
                            useRevenue = true
                        )
                    }

                    // PEDIDOS RECIENTES

                    item {
                        Text(
                            text = "Pedidos recientes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (statistics.orders.isEmpty()) {

                        item {
                            Text("Todavía no tienes pedidos registrados.")
                        }

                    } else {

                        items(
                            items = statistics.orders.take(10),
                            key = { it.id }
                        ) { order ->

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    Text(
                                        text = "Pedido #${order.id.take(8)}",
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = "Fecha: ${
                                            SimpleDateFormat(
                                                "dd/MM/yyyy HH:mm",
                                                Locale("es", "PE")
                                            ).format(Date(order.fecha))
                                        }"
                                    )

                                    Text(
                                        text = "Productos: ${
                                            order.items.sumOf {
                                                it.quantity.coerceAtLeast(0)
                                            }
                                        } unidades"
                                    )

                                    Text("Estado: ${order.estado}")

                                    Text(
                                        text = "Total: ${formatCurrency(order.total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al perfil")
        }
    }
}

// TARJETA DE ESTADISTICA

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// GRAFICA DE LINEAS

@Composable
private fun LineChartCard(
    title: String,
    description: String,
    points: List<MonthlySalesPoint>
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(20.dp))

            if (points.isEmpty() || points.all { it.amount <= 0.0 }) {

                Text("Todavía no hay ventas suficientes para mostrar la gráfica.")

            } else {

                val lineColor = MaterialTheme.colorScheme.primary
                val pointColor = MaterialTheme.colorScheme.tertiary
                val gridColor = MaterialTheme.colorScheme.outlineVariant

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {

                    val maxValue = (points.maxOf { it.amount } * 1.2)
                        .coerceAtLeast(1.0)

                    val leftPadding = 12.dp.toPx()
                    val rightPadding = 12.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 12.dp.toPx()

                    val chartWidth = size.width - leftPadding - rightPadding
                    val chartHeight = size.height - topPadding - bottomPadding

                    // Lineas de referencia

                    for (i in 0..4) {

                        val y = topPadding + chartHeight * i / 4f

                        drawLine(
                            color = gridColor,
                            start = Offset(leftPadding, y),
                            end = Offset(size.width - rightPadding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Puntos de ventas

                    val coordinates = points.mapIndexed { index, point ->

                        val x = if (points.size == 1) {
                            leftPadding + chartWidth / 2
                        } else {
                            leftPadding +
                                    chartWidth * index / (points.size - 1)
                        }

                        val y = topPadding + chartHeight -
                                (point.amount / maxValue).toFloat() * chartHeight

                        Offset(x, y)
                    }

                    // Lineas que unen los puntos

                    coordinates.zipWithNext().forEach { (start, end) ->

                        drawLine(
                            color = lineColor,
                            start = start,
                            end = end,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Circulos de los puntos

                    coordinates.forEach { point ->

                        drawCircle(
                            color = pointColor,
                            radius = 6.dp.toPx(),
                            center = point
                        )

                        drawCircle(
                            color = lineColor,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEach { point ->
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

// GRAFICAS DE BARRAS

@Composable
private fun BarChartCard(
    title: String,
    description: String,
    products: List<ProductSalesPoint>,
    useRevenue: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))

            if (products.isEmpty()) {

                Text("Todavía no hay productos vendidos.")

            } else {

                val maxValue = if (useRevenue) {
                    products.maxOf { it.revenue }.coerceAtLeast(1.0)
                } else {
                    products.maxOf { it.units }.toDouble().coerceAtLeast(1.0)
                }

                // Obtener colores fuera del Canvas
                val barColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                products.forEach { product ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (useRevenue) {
                                    formatCurrency(product.revenue)
                                } else {
                                    "${product.units} unidades"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                        ) {

                            val fraction = if (useRevenue) {
                                product.revenue / maxValue
                            } else {
                                product.units / maxValue
                            }.toFloat().coerceIn(0f, 1f)

                            val cornerRadius = size.height / 2

                            // Fondo de la barra

                            drawRoundRect(
                                color = trackColor,
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    cornerRadius,
                                    cornerRadius
                                )
                            )

                            // Barra de progreso

                            drawRoundRect(
                                color = barColor,
                                size = Size(
                                    width = size.width * fraction,
                                    height = size.height
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    cornerRadius,
                                    cornerRadius
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return "S/ " + String.format(
        Locale("es", "PE"),
        "%.2f",
        amount
    )
}
