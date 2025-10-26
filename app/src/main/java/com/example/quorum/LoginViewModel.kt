package com.example.quorum

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- ESTADO DE LA UI (Actualizado) ---
// Define cómo se ve la pantalla de login en cualquier momento
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,

    val isLoading: Boolean = false,     // Para mostrar un spinner de carga
    val authError: String? = null,      // Para errores de Firebase (ej: contraseña incorrecta)
    val isAuthenticated: Boolean = false // Reemplaza a 'loginSuccess'
)

// --- VIEWMODEL (El Cocinero) ---
// IL 2.2.1: Lógica desacoplada
// IL 2.3.1: Arquitectura MVVM
class LoginViewModel : ViewModel() {

    // IL 2.3.2: Persistencia Remota (Firebase Auth)
    private val auth: FirebaseAuth = Firebase.auth

    // --- Estado Interno (solo el cocinero lo toca) ---
    private val _uiState = MutableStateFlow(LoginUiState())

    // --- Estado Público (el mesero solo lo puede leer) ---
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // --- Inicialización ---
    // Chequea si el usuario YA está logueado al abrir la app
    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            _uiState.update { it.copy(isAuthenticated = true) }
        }
    }

    // --- Acciones del Usuario (Llamadas por el "Mesero") ---

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, authError = null) } // Limpia error al escribir
        validateEmail(email)
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, authError = null) } // Limpia error
        validatePassword(password)
    }

    // IL 2.1.2: Validación de formulario
    private fun validateEmail(email: String, isSubmit: Boolean = false): Boolean {
        val emailError = if (email.isBlank() && isSubmit) {
            "El correo es obligatorio"
        } else if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "El correo no es válido"
        } else {
            null // Válido
        }
        _uiState.update { it.copy(emailError = emailError) }
        return emailError == null
    }

    private fun validatePassword(password: String, isSubmit: Boolean = false): Boolean {
        val passwordError = if (password.isBlank() && isSubmit) {
            "La contraseña es obligatoria"
        } else if (password.isNotBlank() && password.length < 6) {
            "La contraseña debe tener al menos 6 caracteres"
        } else {
            null // Válido
        }
        _uiState.update { it.copy(passwordError = passwordError) }
        return passwordError == null
    }

    // Valida el formulario completo
    private fun validateForm(): Boolean {
        val email = _uiState.value.email
        val pass = _uiState.value.password
        val isEmailValid = validateEmail(email, isSubmit = true)
        val isPassValid = validatePassword(pass, isSubmit = true)
        return isEmailValid && isPassValid && email.isNotBlank() && pass.isNotBlank()
    }

    // --- Funciones de Firebase (Coroutines) ---

    fun onSignInClicked() {
        if (!validateForm()) return // Si el formulario no es válido, no continuar

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            try {
                auth.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password)
                    .await() // Espera a que Firebase responda

                // ¡Éxito!
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }

            } catch (e: Exception) {
                // ¡Error!
                Log.e("LoginViewModel", "Error en Sign In", e)
                _uiState.update {
                    it.copy(isLoading = false, authError = "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun onSignUpClicked() {
        if (!validateForm()) return // Validar antes de registrar

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            try {
                auth.createUserWithEmailAndPassword(_uiState.value.email, _uiState.value.password)
                    .await() // Espera a que Firebase cree el usuario

                // ¡Éxito!
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }

            } catch (e: Exception) {
                // ¡Error!
                Log.e("LoginViewModel", "Error en Sign Up", e)
                _uiState.update {
                    it.copy(isLoading = false, authError = "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun onSignOutClicked() {
        auth.signOut()
        _uiState.update { LoginUiState() } // Resetea el estado a los valores por defecto
    }
}

