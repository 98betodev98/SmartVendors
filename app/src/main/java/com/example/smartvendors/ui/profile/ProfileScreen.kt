
package com.example.smartvendors.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartvendors.ui.theme.SmartVendorsPreferences
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit,
    onPreferencesClick: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val profileState by viewModel.profileState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }

    var selectedImageBase64 by remember {
        mutableStateOf("")
    }

    var photoSelected by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val profileBitmap = remember(selectedImageBase64) {
        base64ToBitmap(selectedImageBase64)
    }

    // Abre la cámara y recibe la fotografía tomada.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val bitmap =
                result.data?.extras?.get("data") as? Bitmap

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    55,
                    outputStream
                )

                selectedImageBase64 = Base64.encodeToString(
                    outputStream.toByteArray(),
                    Base64.NO_WRAP
                )

                photoSelected = true
            }
        }
    }

    // Abre la galería y convierte la imagen seleccionada a Base64.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)

            if (base64 != null) {
                selectedImageBase64 = base64
                photoSelected = true
            }
        }
    }

    // Acción para abrir la cámara desde la imagen.
    val takePhoto: () -> Unit = {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(context.packageManager) != null) {
            cameraLauncher.launch(intent)
        }
    }

    // Cargar el perfil al entrar en la pantalla.
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(profile) {
        nombre = profile.nombre
        apellido = profile.apellido
    }

    // Recuperar la fotografía previamente guardada.
    LaunchedEffect(profile.fotoPerfil) {
        if (
            selectedImageBase64.isEmpty() &&
            profile.fotoPerfil.isNotEmpty()
        ) {
            selectedImageBase64 = profile.fotoPerfil
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Foto de perfil",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Imagen circular: al tocarla, se abre la cámara.
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable {
                    takePhoto()
                },
            contentAlignment = Alignment.Center
        ) {
            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            // Texto visible dentro de la imagen.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.60f))
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tomar foto",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Este botón abre la galería, no la cámara.
        Button(
            onClick = {
                imagePickerLauncher.launch("image/*")
            }
        ) {
            Text("Seleccionar una foto")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (photoSelected) {
            Text(
                text = "Foto seleccionada correctamente",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Datos personales",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = profile.email,
            onValueChange = {},
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (val currentState = profileState) {
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
                // No mostramos ningún mensaje.
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onPreferencesClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SmartVendorsPreferences
            )
        ) {
            Text("Preferencias")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateProfile(
                    nombre = nombre,
                    apellido = apellido,
                    fotoPerfil = selectedImageBase64
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SmartVendorsPreferences
            )
        ) {
            Text("Volver al menú")
        }
    }
}

// Convierte la fotografía seleccionada desde la galería a Base64.
private fun uriToBase64(
    context: Context,
    uri: Uri
): String? {
    return try {
        val inputStream =
            context.contentResolver.openInputStream(uri)

        val bitmap = inputStream?.use {
            BitmapFactory.decodeStream(it)
        } ?: return null

        val maxSize = 400

        val scale = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height,
            1f
        )

        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )

        val outputStream = ByteArrayOutputStream()

        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            55,
            outputStream
        )

        Base64.encodeToString(
            outputStream.toByteArray(),
            Base64.NO_WRAP
        )
    } catch (e: Exception) {
        null
    }
}

// Convierte el Base64 guardado a una imagen para mostrarla.
private fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        if (base64.isBlank()) {
            null
        } else {
            val imageBytes = Base64.decode(
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
}
