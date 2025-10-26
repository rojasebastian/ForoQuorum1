package com.example.quorum

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.quorum.data.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// El estado del UI para la pantalla de favoritos (es idéntico al de Home)
data class FavoritesUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// El "cocinero" de la pantalla de favoritos
class FavoritesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    init {
        loadFavoritePosts()
    }

    private fun loadFavoritePosts() {
        val userId = auth.currentUser?.uid ?: return // No hacer nada si no hay usuario

        Log.d("FavoritesViewModel", "Cargando posts favoritos para el usuario $userId...")
        _uiState.update { it.copy(isLoading = true) }

        // --- ¡LA CLAVE ESTÁ AQUÍ! ---
        // .whereArrayContains("favorites", userId)
        // Le dice a Firestore: "Busca en la colección 'posts' solo los documentos
        // donde el array 'favorites' contenga el ID de nuestro usuario".
        db.collection("posts")
            .whereArrayContains("favorites", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING) // Opcional: ordenar los favoritos
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FavoritesViewModel", "Error al escuchar favoritos.", error)
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error al cargar favoritos: ${error.message}")
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("FavoritesViewModel", "Favoritos recibidos: ${snapshot.size()} documentos")
                    val postsList = snapshot.documents.mapNotNull { document ->
                        document.toObject(Post::class.java)?.copy(id = document.id)
                    }
                    _uiState.update {
                        it.copy(isLoading = false, posts = postsList, error = null)
                    }
                } else {
                    Log.d("FavoritesViewModel", "Snapshot de favoritos es nulo")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
    }
}