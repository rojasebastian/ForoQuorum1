package com.example.quorum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado del UI para la pantalla principal
data class HomeUiState(
    val posts: List<Post> = emptyList(), // La lista de posts
    val isLoading: Boolean = true, // ¿Está cargando?
    val error: String? = null // Mensaje de error
)

// El "Cocinero" de la pantalla principal
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Obtenemos referencias a Auth y Firestore
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    init {
        loadPosts() // Carga los posts apenas se cree el ViewModel
    }

    // --- LECTURA DE DATOS (en tiempo real) ---
    private fun loadPosts() {
        Log.d("HomeViewModel", "Cargando posts...")
        _uiState.update { it.copy(isLoading = true) }

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("HomeViewModel", "Error al escuchar posts.", error)
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error al cargar posts: ${error.message}")
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("HomeViewModel", "Posts recibidos: ${snapshot.size()} documentos")
                    // Mapeamos los documentos para incluir el ID
                    val postsList = snapshot.documents.mapNotNull { document ->
                        document.toObject(Post::class.java)?.copy(id = document.id)
                    }
                    _uiState.update {
                        it.copy(isLoading = false, posts = postsList, error = null)
                    }
                } else {
                    Log.d("HomeViewModel", "Snapshot es nulo")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
    }

    // --- ESCRITURA DE DATOS ---
    fun addPost(title: String, content: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para postear") }
            return
        }

        val newPost = Post(
            title = title,
            content = content,
            authorEmail = currentUser.email ?: "Anónimo",
            authorId = currentUser.uid
        )

        viewModelScope.launch {
            try {
                db.collection("posts").add(newPost).await()
                Log.d("HomeViewModel", "Post agregado con éxito")
            } catch (e: Exception) {
                Log.w("HomeViewModel", "Error al agregar post", e)
                _uiState.update { it.copy(error = "Error al crear post: ${e.message}") }
            }
        }
    }

    // --- BORRADO DE DATOS ---
    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId).delete().await()
                Log.d("HomeViewModel", "Post borrado con éxito: $postId")
            } catch (e: Exception) {
                Log.w("HomeViewModel", "Error al borrar post", e)
                _uiState.update { it.copy(error = "Error al borrar el post: ${e.message}") }
            }
        }
    }

    // --- ACTUALIZACIÓN DE DATOS ---
    fun updatePost(postId: String, newTitle: String, newContent: String) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                val updates = mapOf(
                    "title" to newTitle,
                    "content" to newContent
                )
                postRef.update(updates).await()
                Log.d("HomeViewModel", "Post actualizado con éxito: $postId")
            } catch (e: Exception) {
                Log.w("HomeViewModel", "Error al actualizar post", e)
                _uiState.update { it.copy(error = "Error al actualizar el post: ${e.message}") }
            }
        }
    }

    // --- MANEJO DE FAVORITOS ---
    fun toggleFavorite(postId: String, currentFavorites: List<String>) {
        val userId = auth.currentUser?.uid ?: return // No hacer nada si no hay usuario

        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                if (currentFavorites.contains(userId)) {
                    // El usuario ya lo tiene en favoritos -> quitarlo
                    postRef.update("favorites", FieldValue.arrayRemove(userId)).await()
                    Log.d("HomeViewModel", "Usuario $userId removido de favoritos del post $postId")
                } else {
                    // El usuario no lo tiene en favoritos -> agregarlo
                    postRef.update("favorites", FieldValue.arrayUnion(userId)).await()
                    Log.d("HomeViewModel", "Usuario $userId agregado a favoritos del post $postId")
                }
            } catch (e: Exception) {
                Log.w("HomeViewModel", "Error al actualizar favoritos", e)
                _uiState.update { it.copy(error = "Error al actualizar favoritos: ${e.message}") }
            }
        }
    }
}
