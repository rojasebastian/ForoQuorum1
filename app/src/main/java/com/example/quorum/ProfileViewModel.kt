package com.example.quorum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.Comment
import com.example.quorum.data.Post
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

// Estructura para unir un comentario con la información de su post
data class CommentWithPostInfo(
    val comment: Comment,
    val postTitle: String,
    val postId: String
)

// El estado del UI para la pantalla de perfil
data class ProfileUiState(
    val userEmail: String? = null,
    val isLoadingPosts: Boolean = true,
    val isLoadingComments: Boolean = true,
    val posts: List<Post> = emptyList(),
    val comments: List<CommentWithPostInfo> = emptyList(),
    val error: String? = null
)

// El "cocinero" de la pantalla de perfil
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    init {
        val userEmail = auth.currentUser?.email
        _uiState.update { it.copy(userEmail = userEmail) }

        loadUserPosts()
        loadUserComments()
    }

    private fun loadUserPosts() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoadingPosts = true) }

        db.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoadingPosts = false, error = "Error al cargar tus posts: ${error.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val postsList = snapshot.toObjects(Post::class.java)
                    _uiState.update { it.copy(isLoadingPosts = false, posts = postsList) }
                }
            }
    }

    private fun loadUserComments() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoadingComments = true) }

        viewModelScope.launch {
            try {
                // Consulta de grupo de colecciones: busca en todas las colecciones "comments"
                val commentsSnapshot = db.collectionGroup("comments")
                    .whereEqualTo("authorId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING) // <-- Eliminado para evitar la necesidad de un índice
                    .get()
                    .await()

                val commentsWithPostInfo = commentsSnapshot.documents.mapNotNull { commentDoc ->
                    val comment = commentDoc.toObject(Comment::class.java) ?: return@mapNotNull null
                    
                    // El comentario sabe quién es su "padre" (el post)
                    val postRef = commentDoc.reference.parent.parent
                    val postSnapshot = postRef?.get()?.await()
                    val post = postSnapshot?.toObject(Post::class.java)
                    
                    CommentWithPostInfo(
                        comment = comment,
                        postTitle = post?.title ?: "Post Desconocido",
                        postId = postRef?.id ?: ""
                    )
                }
                
                _uiState.update { it.copy(isLoadingComments = false, comments = commentsWithPostInfo) }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al cargar comentarios del usuario", e)
                _uiState.update { it.copy(isLoadingComments = false, error = "Error al cargar tus comentarios: ${e.message}") }
            }
        }
    }
}
