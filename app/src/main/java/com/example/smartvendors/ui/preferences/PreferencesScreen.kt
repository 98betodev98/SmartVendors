package com.example.smartvendors.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = remember {
        PreferencesViewModel()
    },
    onBack: () -> Unit
) {

    val recibirOfertas by
    viewModel.recibirOfertas.collectAsState()

    val recibirCapacitaciones by
    viewModel.recibirCapacitaciones.collectAsState()

    val recibirNotificaciones by
    viewModel.recibirNotificaciones.collectAsState()

    val state by
    viewModel.preferencesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPreferences()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Preferencias",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        PreferenceItem(
            title = "Ofertas y promociones",
            checked = recibirOfertas,
            onCheckedChange = {
                viewModel.setRecibirOfertas(it)
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        PreferenceItem(
            title = "Capacitaciones",
            checked = recibirCapacitaciones,
            onCheckedChange = {
                viewModel.setRecibirCapacitaciones(it)
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        PreferenceItem(
            title = "Notificaciones",
            checked = recibirNotificaciones,
            onCheckedChange = {
                viewModel.setRecibirNotificaciones(it)
            }
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        when (state) {

            PreferencesState.Loading -> {

                CircularProgressIndicator()
            }

            is PreferencesState.Success -> {

                Text(
                    text = (state as PreferencesState.Success).message,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is PreferencesState.Error -> {

                Text(
                    text = (state as PreferencesState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            PreferencesState.Idle -> {
                // Sin mensaje
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {
                viewModel.savePreferences()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Guardar preferencias"
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

@Composable
private fun PreferenceItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}