package com.example.smartvendors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
import com.example.smartvendors.ui.recommendations.RecommendationScreen
import com.example.smartvendors.ui.menu.MenuScreen

import com.example.smartvendors.ui.cart.CartViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import kotlinx.coroutines.launch
import com.example.smartvendors.ui.auth.GoogleSignInManager
import com.example.smartvendors.ui.auth.AuthViewModel

import com.example.smartvendors.ui.assistant.AssistantScreen


import androidx.activity.compose.BackHandler
import com.example.smartvendors.domain.model.Client
import com.example.smartvendors.ui.cart.CartScreen
import com.example.smartvendors.ui.statistics.SalesStatisticsScreen

import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )


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

                var isCheckingSession by remember {
                    mutableStateOf(true)
                }

                LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser

                    if (user == null) {
                        currentScreen = "login"
                        isCheckingSession = false
                    } else {
                        try {
                            user.reload().await()

                            if (auth.currentUser?.isEmailVerified == true) {
                                setupViewModel.checkSetupCompleted { completed ->
                                    currentScreen = if (completed) {
                                        "menu"
                                    } else {
                                        "setup"
                                    }

                                    isCheckingSession = false
                                }
                            } else {
                                auth.signOut()
                                authViewModel.clearState()
                                currentScreen = "login"
                                isCheckingSession = false
                            }
                        } catch (e: Exception) {
                            currentScreen = "login"
                            isCheckingSession = false
                        }
                    }
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
                            currentScreen = "menu"
                        }

                        "menu" -> {
                            FirebaseAuth.getInstance().signOut()
                            authViewModel.clearState()
                            currentScreen = "login"
                        }

                        "preferences" -> {
                            currentScreen = "profile"
                        }

                        "profile" -> {
                            currentScreen = "menu"
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
                            currentScreen = "menu"
                        }

                        "clients" -> {
                            currentScreen = "menu"
                        }

                        "assistant" -> {
                            currentScreen = "menu"
                        }

                        "statistics" -> {
                            currentScreen = "menu"
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

                val cartViewModel: CartViewModel = viewModel()

                if (isCheckingSession) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.padding(
                                    horizontal = 32.dp,
                                    vertical = 28.dp
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF80418C)
                                )

                                Text(
                                    text = "SmartVendors",
                                    modifier = Modifier.padding(top = 20.dp),
                                    color = Color(0xFF80418C),
                                    style = androidx.compose.material3.MaterialTheme
                                        .typography.titleLarge
                                )

                                Text(
                                    text = "Verificando sesión...",
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                } else
                {
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
                                                "menu"
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

                        "menu" -> {
                            MenuScreen(
                                onProfileClick = {
                                    currentScreen = "profile"
                                },
                                onCatalogClick = {
                                    currentScreen = "catalog"
                                },
                                onClientsClick = {
                                    currentScreen = "clients"
                                },
                                onRecommendationsClick = {
                                    currentScreen = "recommendations"
                                },
                                onAssistantClick = {
                                    currentScreen = "assistant"
                                },
                                onStatisticsClick = {
                                    currentScreen = "statistics"
                                },
                                onLogoutClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    authViewModel.clearState()
                                    currentScreen = "login"
                                }
                            )
                        }

                        "profile" -> {
                            ProfileScreen(
                                onBack = {
                                    currentScreen = "menu"
                                },
                                onPreferencesClick = {
                                    currentScreen = "preferences"
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
                            InitialSetupScreen(
                                viewModel = setupViewModel,
                                onSetupComplete = {
                                    currentScreen = "menu"
                                },
                                onBack = {
                                    currentScreen = "menu"
                                }
                            )
                        }

                        "catalog" -> {
                            CatalogScreen(
                                onBack = {
                                    currentScreen = "menu"
                                },
                                onCartClick = {
                                    currentScreen = "cart"
                                },
                                cartViewModel = cartViewModel
                            )
                        }

                        "cart" -> {
                            CartScreen(
                                onBack = {
                                    currentScreen = "catalog"
                                },
                                onConfirmOrder = {
                                    currentScreen = "catalog"
                                },
                                viewModel = cartViewModel
                            )
                        }

                        "clients" -> {
                            ClientScreen(
                                onBack = {
                                    currentScreen = "menu"
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

                        "recommendations" -> {
                            RecommendationScreen(
                                onBack = {
                                    currentScreen = "menu"
                                }
                            )
                        }

                        "assistant" -> {
                            AssistantScreen(
                                onBack = {
                                    currentScreen = "menu"
                                }
                            )
                        }

                        "statistics" -> {
                            SalesStatisticsScreen(
                                onBack = {
                                    currentScreen = "menu"
                                }
                            )
                        }
                    }
                }




            }
        }
    }
}