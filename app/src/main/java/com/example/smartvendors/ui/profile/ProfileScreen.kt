package com.example.smartvendors.ui.profile

import androidx.compose.foundation.layout.navigationBarsPadding


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream


import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit = {}
) {

    val profile by viewModel.profile.collectAsState()
    val profileState by viewModel.profileState.collectAsState()

    var nombre by remember {
        mutableStateOf("")
    }

    var apellido by remember {
        mutableStateOf("")
    }

    var recibirOfertas by remember {
        mutableStateOf(true)
    }

    var recibirCapacitaciones by remember {
        mutableStateOf(true)
    }

    var recibirNotificaciones by remember {
        mutableStateOf(true)
    }

    var selectedImageBase64 by remember {
        mutableStateOf("")
    }

    var photoSelected by remember {
        mutableStateOf(false)
    }

    val profileBitmap =
        remember(selectedImageBase64) {

            base64ToBitmap(
                selectedImageBase64
            )
        }

    val context = LocalContext.current

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                val base64 =
                    uriToBase64(
                        context = context,
                        uri = uri
                    )

                if (base64 != null) {

                    selectedImageBase64 = base64
                    photoSelected = true
                }
            }
        }

    LaunchedEffect(Unit) {

        viewModel.loadProfile()
    }

    LaunchedEffect(profile) {

        nombre = profile.nombre
        apellido = profile.apellido

        recibirOfertas = profile.recibirOfertas
        recibirCapacitaciones =
            profile.recibirCapacitaciones
        recibirNotificaciones =
            profile.recibirNotificaciones
    }

    LaunchedEffect(profile.fotoPerfil) {

        if (
            selectedImageBase64.isEmpty() &&
            profile.fotoPerfil.isNotEmpty()
        ) {
            selectedImageBase64 =
                profile.fotoPerfil
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Foto de perfil",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (profileBitmap != null) {

            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

        } else {

        Text(
                text = "No hay foto de perfil"
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (photoSelected) {

            Text(
                text = "Foto seleccionada correctamente",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = {
                imagePickerLauncher.launch("image/*")
            }
        ) {
            Text("Seleccionar foto")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HorizontalDivider()

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Datos personales",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = profile.email,
            onValueChange = {},
            label = {
                Text("Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(
            modifier = Modifier.height(12.dp)
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
            modifier = Modifier.height(12.dp)
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
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Preferencias y notificaciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        PreferenceSwitch(
            title = "Ofertas y promociones",
            checked = recibirOfertas,
            onCheckedChange = {
                recibirOfertas = it
            }
        )

        PreferenceSwitch(
            title = "Capacitaciones",
            checked = recibirCapacitaciones,
            onCheckedChange = {
                recibirCapacitaciones = it
            }
        )

        PreferenceSwitch(
            title = "Notificaciones",
            checked = recibirNotificaciones,
            onCheckedChange = {
                recibirNotificaciones = it
            }
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        val currentState = profileState

        when (currentState) {

            ProfileState.Loading -> {

                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp)
                )
            }

            is ProfileState.Success -> {

                Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is ProfileState.Error -> {

                Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            ProfileState.Idle -> {
                // No mostramos nada
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {

                viewModel.updateProfile(
                    nombre = nombre,
                    apellido = apellido,
                    fotoPerfil = selectedImageBase64,
                    recibirOfertas = recibirOfertas,
                    recibirCapacitaciones =
                        recibirCapacitaciones,
                    recibirNotificaciones =
                        recibirNotificaciones
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Guardar cambios")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Volver")
        }
    }
}

private fun uriToBase64(
    context: Context,
    uri: Uri
): String? {

    return try {

        val inputStream =
            context.contentResolver
                .openInputStream(uri)

        val bitmap =
            BitmapFactory.decodeStream(inputStream)

        inputStream?.close()

        if (bitmap == null) {
            return null
        }

        val maxSize = 400

        val scale =
            minOf(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height,
                1f
            )

        val resizedBitmap =
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )

        val outputStream =
            ByteArrayOutputStream()

        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            55,
            outputStream
        )

        val imageBytes =
            outputStream.toByteArray()

        Base64.encodeToString(
            imageBytes,
            Base64.NO_WRAP
        )

    } catch (e: Exception) {

        null
    }
}

private fun base64ToBitmap(
    base64: String
) = try {

    if (base64.isBlank()) {
        null
    } else {

        val imageBytes =
            Base64.decode(
                base64,
                Base64.DEFAULT
            )

        BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size
        )
    }

} catch (e: Exception) {

    null
}

@Composable
private fun PreferenceSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}