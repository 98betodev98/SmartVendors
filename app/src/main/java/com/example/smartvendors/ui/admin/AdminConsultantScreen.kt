package com.example.smartvendors.ui.admin

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.domain.model.AdminConsultant

@Composable
fun AdminConsultantScreen(
    onBack: () -> Unit,
    onConsultantClick: (AdminConsultant) -> Unit,
    viewModel: AdminConsultantViewModel = viewModel()
) {

    val consultants by viewModel.consultants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConsultants()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Gestión de consultoras",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Administración de usuarios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.refresh() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar"
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF275EF5)
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Consultoras registradas",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = consultants.size.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Usuarios con rol USER",
                        color = Color.White
                    )
                }
            }

            when {

                isLoading -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Text(
                            text = "Cargando consultoras...",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                error != null -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                consultants.isEmpty() -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "No hay consultoras registradas",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                else -> {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = consultants,
                            key = { it.uid }
                        ) { consultant ->

                            ConsultantCard(
                                consultant = consultant,
                                onClick = {
                                    onConsultantClick(consultant)
                                },
                                onStatusChange = { nuevoEstado ->
                                    viewModel.updateConsultantStatus(
                                        uid = consultant.uid,
                                        activo = nuevoEstado
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsultantCard(
    consultant: AdminConsultant,
    onClick: () -> Unit,
    onStatusChange: (Boolean) -> Unit
) {

    val profileBitmap = try {

        if (consultant.fotoPerfil.isNotBlank()) {

            val cleanBase64 = consultant.fotoPerfil
                .substringAfter("base64,", consultant.fotoPerfil)

            val decodedBytes = Base64.decode(
                cleanBase64,
                Base64.DEFAULT
            )

            BitmapFactory.decodeByteArray(
                decodedBytes,
                0,
                decodedBytes.size
            )

        } else {
            null
        }

    } catch (e: Exception) {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (profileBitmap != null) {

                    Image(
                        bitmap = profileBitmap.asImageBitmap(),
                        contentDescription = "Foto de ${consultant.nombre}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF275EF5),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {

                    Text(
                        text = listOf(
                            consultant.nombre,
                            consultant.apellido
                        )
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { "Consultora sin nombre" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Rol: ${consultant.rol}",
                        color = Color(0xFF27834A),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = consultant.email.ifBlank {
                        "Correo no disponible"
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "UID Firebase",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = consultant.uid,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = if (consultant.activo) "● ACTIVA" else "● INACTIVA",
                    color = if (consultant.activo)
                        Color(0xFF27834A)
                    else
                        Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        onStatusChange(!consultant.activo)
                    }
                ) {
                    Text(
                        text = if (consultant.activo)
                            "Desactivar"
                        else
                            "Activar"
                    )
                }
            }
        }
    }
}
