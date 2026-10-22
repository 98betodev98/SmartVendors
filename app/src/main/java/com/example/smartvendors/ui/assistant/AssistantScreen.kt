
package com.example.smartvendors.ui.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.DisposableEffect

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    viewModel: AssistantViewModel = viewModel()
) {
    var message by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val textToSpeech = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {

            }
        }
    }

    DisposableEffect(textToSpeech) {
        textToSpeech.language = java.util.Locale("es", "PE")

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val messages by viewModel.messages.collectAsState()

    val lastMessage = messages.lastOrNull()

    LaunchedEffect(lastMessage?.text, lastMessage?.isUser) {
        if (lastMessage != null && !lastMessage.isUser) {
            textToSpeech.speak(
                lastMessage.text.replace(
                    Regex("""\*\*(.*?)\*\*"""),
                    "$1"
                ),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "assistant_response"
            )
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()

        if (!spokenText.isNullOrBlank()) {
            message = spokenText
            voiceError = null

            viewModel.sendMessage(spokenText)
            message = ""
        } else {
            voiceError = "No se reconoció ninguna frase. Inténtalo nuevamente."
        }
    }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                voiceError = null

                val intent = Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                ).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        "es-PE"
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "es-PE"
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_PROMPT,
                        "Habla ahora para consultar al asistente"
                    )
                }

                if (
                    SpeechRecognizer.isRecognitionAvailable(context)
                ) {
                    speechLauncher.launch(intent)
                } else {
                    voiceError =
                        "El dispositivo no tiene reconocimiento de voz disponible."
                }
            } else {
                voiceError =
                    "Necesitas permitir el acceso al micrófono para dictar tu consulta."
            }
        }

    LaunchedEffect(messages.size, isLoading) {
        val itemCount = messages.size + if (isLoading) 1 else 0
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Asistente virtual de ventas",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { chatMessage ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (chatMessage.isUser) {
                        Arrangement.End
                    } else {
                        Arrangement.Start
                    }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (chatMessage.isUser) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = chatMessage.text,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = " Generando respuesta...",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Escribe o dicta tu consulta") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (voiceError != null) {
            Text(
                text = voiceError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    voiceError = null

                    val hasPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        if (
                            SpeechRecognizer.isRecognitionAvailable(context)
                        ) {
                            val intent = Intent(
                                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                            ).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE,
                                    "es-PE"
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                                    "es-PE"
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_PROMPT,
                                    "Habla ahora para consultar al asistente"
                                )
                            }

                            speechLauncher.launch(intent)
                        } else {
                            voiceError =
                                "El dispositivo no tiene reconocimiento de voz disponible."
                        }
                    } else {
                        microphonePermissionLauncher.launch(
                            Manifest.permission.RECORD_AUDIO
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("🎙 Micrófono")
            }

            Button(
                onClick = {
                    val question = message.trim()
                    if (question.isNotEmpty()) {
                        viewModel.sendMessage(question)
                        message = ""
                        voiceError = null
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = message.isNotBlank() && !isLoading
            ) {
                Text("Enviar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
