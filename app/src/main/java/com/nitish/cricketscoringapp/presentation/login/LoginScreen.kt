package com.nitish.cricketscoringapp.presentation.login

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.nitish.cricketscoringapp.R
import com.nitish.cricketscoringapp.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val webClientId = context.getString(R.string.default_web_client_id)
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collectLatest { onSignedIn() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(EmeraldPrimary.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsCricket,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Cricket Scorer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = c.textPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Live Match Tracker",
                fontSize = 13.sp,
                color = EmeraldPrimary,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Sign in to sync your matches across\ndevices and keep your data safe.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val idToken = getGoogleIdToken(credentialManager, context, webClientId)
                            viewModel.signInWithGoogle(idToken)
                        } catch (e: GetCredentialCancellationException) {
                            // User dismissed — do nothing
                        } catch (e: Exception) {
                            viewModel.setError("Sign-in failed: ${e.message}")
                        }
                    }
                },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.surface,
                    contentColor = c.textPrimary,
                    disabledContainerColor = c.surface2,
                    disabledContentColor = c.textSecondary
                ),
                border = BorderStroke(1.dp, c.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = EmeraldPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(state.loadingMessage, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(
                        "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Continue with Google", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Divider ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = c.outline)
                Text(
                    "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = c.outline)
            }

            Spacer(Modifier.height(14.dp))

            // ── Guest button ──────────────────────────────────────────────────
            OutlinedButton(
                onClick = { viewModel.continueAsGuest() },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.textSecondary),
                border = BorderStroke(1.dp, c.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Continue as Guest",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = c.textSecondary
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Guest data is stored only on this device\nand won't sync to the cloud.",
                style = MaterialTheme.typography.labelSmall,
                color = c.textTertiary,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketRedDim),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CricketRed.copy(alpha = 0.4f))
                ) {
                    Text(
                        state.error!!,
                        color = CricketRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Step 1: try the native bottom-sheet (GetGoogleIdOption).
 * Step 2: on NoCredentialException fall back to the full account picker (GetSignInWithGoogleOption).
 */
private suspend fun getGoogleIdToken(
    credentialManager: CredentialManager,
    context: Context,
    webClientId: String
): String {
    return try {
        val result = credentialManager.getCredential(
            request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .build()
                )
                .build(),
            context = context
        )
        extractIdToken(result.credential)
    } catch (e: NoCredentialException) {
        val result = credentialManager.getCredential(
            request = GetCredentialRequest.Builder()
                .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
                .build(),
            context = context
        )
        extractIdToken(result.credential)
    }
}

private fun extractIdToken(credential: androidx.credentials.Credential): String {
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
    throw Exception("Unexpected credential type: ${credential.type}")
}
