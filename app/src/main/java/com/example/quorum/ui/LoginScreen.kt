package com.example.quorum.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quorum.LoginUiState
import com.example.quorum.ui.theme.QuorumTheme

// --- PANTALLA DE LOGIN (El Mesero) ---
// Esta es la "V" (Vista) de MVVM.
// No contiene NADA de lógica.
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignInClicked: () -> Unit, // Renombrado
    onSignUpClicked: () -> Unit  // Nuevo
) {
    // 'Column' centra todo verticalmente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Foro de Ciencia",
            fontSize = 28.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- CAMPO DE EMAIL ---
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.emailError != null || uiState.authError != null,
            singleLine = true,
            enabled = !uiState.isLoading // Deshabilitar mientras carga
        )
        if (uiState.emailError != null) {
            Text(
                text = uiState.emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CAMPO DE CONTRASEÑA ---
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.passwordError != null || uiState.authError != null,
            singleLine = true,
            enabled = !uiState.isLoading // Deshabilitar mientras carga
        )
        if (uiState.passwordError != null) {
            Text(
                text = uiState.passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }

        // --- ERROR GENERAL DE FIREBASE ---
        if (uiState.authError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.authError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN DE CARGA Y BOTONES ---
        if (uiState.isLoading) {
            CircularProgressIndicator() // Spinner de carga
        } else {
            // --- BOTÓN DE INICIAR SESIÓN ---
            Button(
                onClick = onSignInClicked,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar Sesión", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BOTÓN DE REGISTRO ---
            OutlinedButton(
                onClick = onSignUpClicked, // Llama a la nueva función
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Crear Cuenta Nueva", fontSize = 16.sp)
            }
        }
    }
}

// --- PREVISUALIZACIÓN ---
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    QuorumTheme {
        val fakeState = LoginUiState(authError = "Error: La contraseña es incorrecta")
        LoginScreen(
            uiState = fakeState,
            onEmailChanged = {},
            onPasswordChanged = {},
            onSignInClicked = {},
            onSignUpClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    QuorumTheme {
        val fakeState = LoginUiState(isLoading = true)
        LoginScreen(
            uiState = fakeState,
            onEmailChanged = {},
            onPasswordChanged = {},
            onSignInClicked = {},
            onSignUpClicked = {}
        )
    }
}

