package com.example.smartvendors.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.domain.model.Product
import coil.compose.AsyncImage

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onBack: () -> Unit,
    viewModel: CatalogViewModel = viewModel()
) {

    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember {
        mutableStateOf("")
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Catálogo de productos")
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
                        modifier = Modifier.align(Alignment.Center)
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
                            text = errorMessage ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )

                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.loadProducts()
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                viewModel.searchProducts(query)
                            },
                            label = {
                                Text("Buscar productos")
                            },
                            placeholder = {
                                Text("Ej. mascara, lipstick...")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp
                                ),
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {

                            TextButton(
                                onClick = {
                                    categoryExpanded = true
                                }
                            ) {
                                Text("Categoría: $selectedCategory")
                            }

                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = {
                                    categoryExpanded = false
                                }
                            ) {

                                DropdownMenuItem(
                                    text = {
                                        Text("Todas")
                                    },
                                    onClick = {
                                        categoryExpanded = false
                                        viewModel.filterByCategory("Todas")
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Beauty")
                                    },
                                    onClick = {
                                        categoryExpanded = false
                                        viewModel.filterByCategory("Beauty")
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Fragrances")
                                    },
                                    onClick = {
                                        categoryExpanded = false
                                        viewModel.filterByCategory("Fragrances")
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Furniture")
                                    },
                                    onClick = {
                                        categoryExpanded = false
                                        viewModel.filterByCategory("Furniture")
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text("Groceries")
                                    },
                                    onClick = {
                                        categoryExpanded = false
                                        viewModel.filterByCategory("Groceries")
                                    }
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(products) { product ->
                                ProductCard(product)
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
}

@Composable
private fun ProductCard(product: Product) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            AsyncImage(
                model = product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = product.category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "⭐ ${product.rating}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Precio: S/ %.2f".format(product.price),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Descuento: %.1f%%".format(product.discountPercentage),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Stock: ${product.stock}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}