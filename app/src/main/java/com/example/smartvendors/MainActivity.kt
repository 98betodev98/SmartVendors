package com.example.smartvendors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.smartvendors.ui.auth.EmailVerificationScreen
import com.example.smartvendors.ui.auth.LoginScreen
import com.example.smartvendors.ui.auth.RegisterScreen
import com.example.smartvendors.ui.auth.ForgotPasswordScreen
import com.example.smartvendors.ui.theme.SmartVendorsTheme
import com.example.smartvendors.ui.setup.InitialSetupScreen
import com.example.smartvendors.ui.profile.ProfileScreen
import com.example.smartvendors.ui.preferences.PreferencesScreen
import com.example.smartvendors.ui.setup.InitialSetupViewModel
import com.example.smartvendors.ui.catalog.CatalogScreen
import com.example.smartvendors.ui.client.ClientScreen
import com.example.smartvendors.ui.client.AddClientScreen
import com.example.smartvendors.ui.client.EditClientScreen


import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import kotlinx.coroutines.launch
import com.example.smartvendors.ui.auth.GoogleSignInManager
import com.example.smartvendors.ui.auth.AuthViewModel


import androidx.activity.compose.BackHandler
import com.example.smartvendors.domain.model.Client

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            SmartVendorsTheme {

                val googleSignInManager = remember {
                    GoogleSignInManager(this@MainActivity)
                }

                val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                val setupViewModel: InitialSetupViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                val coroutineScope = rememberCoroutineScope()

                var currentScreen by remember {
                    mutableStateOf("login")
                }

                var registeredEmail by remember {
                    mutableStateOf("")
                }

                var selectedClient by remember {
                    mutableStateOf<Client?>(null)
                }

                BackHandler {

                    when (currentScreen) {

                        "setup" -> {
                            currentScreen = "profile"
                        }

                        "preferences" -> {
                            currentScreen = "profile"
                        }

                        "profile" -> {
                            authViewModel.clearState()
                            currentScreen = "login"
                        }

                        "register" -> {
                            currentScreen = "login"
                        }

                        "forgotPassword" -> {
                            currentScreen = "login"
                        }

                        "verification" -> {
                            currentScreen = "login"
                        }

                        "catalog" -> {
                            currentScreen = "profile"
                        }

                        "clients" -> {
                            currentScreen = "profile"
                        }

                        "addClient" -> {
                            currentScreen = "clients"
                        }

                        "editClient" -> {
                            selectedClient = null
                            currentScreen = "clients"
                        }

                        else -> {
                            // En Login dejamos que Android cierre la aplicación
                        }
                    }
                }

                when (currentScreen) {

                    "login" -> {

                        LoginScreen(
                            viewModel = authViewModel,

                            onRegisterClick = {
                                currentScreen = "register"
                            },

                            onForgotPasswordClick = {
                                currentScreen = "forgotPassword"
                            },

                            onLoginSuccess = {
                                setupViewModel.checkSetupCompleted { completed ->

                                    currentScreen =
                                        if (completed) {
                                            "profile"
                                        } else {
                                            "setup"
                                        }
                                }
                            },

                            onGoogleClick = {

                                coroutineScope.launch {

                                    try {

                                        val idToken =
                                            googleSignInManager
                                                .getGoogleIdToken()

                                        authViewModel
                                            .loginWithGoogle(idToken)

                                    } catch (e: Exception) {

                                        Toast.makeText(
                                            this@MainActivity,
                                            e.message
                                                ?: "No se pudo iniciar sesión con Google",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )
                    }

                    "register" -> {

                        RegisterScreen(
                            onRegisterSuccess = { email ->

                                registeredEmail = email
                                currentScreen = "verification"
                            },

                            onBackToLogin = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "verification" -> {

                        EmailVerificationScreen(
                            email = registeredEmail,

                            onVerificationSuccess = {

                                currentScreen = "login"
                            },

                            onBackToLogin = {

                                currentScreen = "login"
                            }
                        )
                    }

                    "forgotPassword" -> {

                        ForgotPasswordScreen(
                            onBackToLogin = {
                                currentScreen = "login"
                            }
                        )
                    }

                    "profile" -> {

                        ProfileScreen(
                            onBack = {
                                authViewModel.clearState()
                                currentScreen = "login"
                            },
                            onPreferencesClick = {
                                currentScreen = "preferences"
                            },
                            onCatalogClick = {
                                currentScreen = "catalog"
                            },
                            onClientsClick = {
                                currentScreen = "clients"
                            }
                        )
                    }

                    "preferences" -> {
                        PreferencesScreen(
                            onBack = {
                                currentScreen = "profile"
                            }
                        )
                    }

                    "setup" -> {

                        val setupViewModel: InitialSetupViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel()

                        InitialSetupScreen(
                            viewModel = setupViewModel,

                            onSetupComplete = {
                                currentScreen = "profile"
                            },

                            onBack = {
                                currentScreen = "profile"
                            }
                        )
                    }

                    "catalog" -> {
                        CatalogScreen(
                            onBack = {
                                currentScreen = "profile"
                            }
                        )
                    }

                    "clients" -> {
                        ClientScreen(
                            onBack = {
                                currentScreen = "profile"
                            },
                            onAddClient = {
                                currentScreen = "addClient"
                            },
                            onEditClient = { client ->
                                selectedClient = client
                                currentScreen = "editClient"
                            }
                        )
                    }

                    "addClient" -> {
                        AddClientScreen(
                            onBack = {
                                currentScreen = "clients"
                            },
                            onClientSaved = {
                                currentScreen = "clients"
                            }
                        )
                    }

                    "editClient" -> {
                        selectedClient?.let { client ->

                            EditClientScreen(
                                client = client,
                                onBack = {
                                    currentScreen = "clients"
                                },
                                onClientUpdated = {
                                    selectedClient = null
                                    currentScreen = "clients"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}