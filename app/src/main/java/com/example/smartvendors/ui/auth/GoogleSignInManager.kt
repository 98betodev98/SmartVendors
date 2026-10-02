package com.example.smartvendors.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.smartvendors.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

class GoogleSignInManager(
    private val context: Context
) {

    private val credentialManager =
        CredentialManager.create(context)

    suspend fun getGoogleIdToken(): String {

        val googleIdOption =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    context.getString(
                        R.string.default_web_client_id
                    )
                )
                .setAutoSelectEnabled(false)
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

        val result =
            credentialManager.getCredential(
                context = context,
                request = request
            )

        val credential =
            result.credential

        val googleIdTokenCredential =
            com.google.android.libraries.identity.googleid
                .GoogleIdTokenCredential
                .createFrom(credential.data)

        return googleIdTokenCredential.idToken
    }
}