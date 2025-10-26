package com.example.quorum

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.Comment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado del UI para la pantalla de detalle
data class PostDetailUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// El "Cocinero" de la pantalla de detalle de un post
class PostDetailViewModel(
    // SavedStateHandle nos permite recibir los argumentos de navegación, como el postId
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // Obtenemos el postId que nos llega desde la navegación
    private val postId: String = checkNotNull(savedStateHandle["postId"])

    init {
        loadComments()
    }

    // --- LECTURA DE COMENTARIOS ---
    private fun loadComments() {
        _uiState.update { it.copy(isLoading = true) }
        Log.d("PostDetailViewModel", "Cargando comentarios para el post $postId")

        // Apuntamos a la SUBCOLECCIÓN "comments" dentro del post
        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Del más antiguo al más nuevo
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("PostDetailViewModel", "Error al escuchar comentarios.", error)
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar comentarios: ${error.message}") }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("PostDetailViewModel", "Comentarios recibidos: ${snapshot.size()} documentos")
                    val commentsList = snapshot.documents.mapNotNull { document ->
                        document.toObject(Comment::class.java)?.copy(id = document.id)
                    }
                    _uiState.update { it.copy(isLoading = false, comments = commentsList, error = null) }
                } else {
                    Log.d("PostDetailViewModel", "Snapshot de comentarios es nulo")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
    }

    // --- ESCRITURA DE COMENTARIOS ---
    fun addComment(text: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para comentar") }
            return
        }

        if (text.isBlank()) {
            _uiState.update { it.copy(error = "El comentario no puede estar vacío") }
            return
        }

        val newComment = Comment(
            text = text,
            authorEmail = currentUser.email ?: "Anónimo",
            authorId = currentUser.uid
        )

        viewModelScope.launch {
            try {
                // Añadimos el comentario a la subcolección
                db.collection("posts").document(postId)
                    .collection("comments")
                    .add(newComment).await()
                Log.d("PostDetailViewModel", "Comentario agregado con éxito")
            } catch (e: Exception) {
                Log.w("PostDetailViewModel", "Error al agregar comentario", e)
                _uiState.update { it.copy(error = "Error al crear comentario: ${e.message}") }
            }
        }
    }
}
