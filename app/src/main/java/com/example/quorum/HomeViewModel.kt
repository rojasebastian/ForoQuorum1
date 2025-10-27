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
import java.util.Date

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    init {
        loadPosts()
    }

    private fun loadPosts() {
        _uiState.update { it.copy(isLoading = true) }
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar posts.") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val postsList = snapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)
                        val topic = if (post?.topic.isNullOrBlank()) "Química" else post!!.topic
                        post?.copy(id = doc.id, topic = topic)
                    }
                    _uiState.update { it.copy(isLoading = false, posts = postsList, error = null) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
    }

    fun addPost(title: String, content: String, topic: String) {
        val currentUser = auth.currentUser ?: return
        val newPost = Post(
            authorEmail = currentUser.email ?: "Anónimo",
            authorId = currentUser.uid,
            title = title,
            content = content,
            timestamp = Date(),
            topic = topic
        )

        viewModelScope.launch {
            try {
                db.collection("posts").add(newPost).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al crear el post.") }
            }
        }
    }

    fun toggleFavorite(postId: String, currentFavorites: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                val updatedFavorites = if (currentFavorites.contains(userId)) {
                    FieldValue.arrayRemove(userId)
                } else {
                    FieldValue.arrayUnion(userId)
                }
                postRef.update("favorites", updatedFavorites).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al actualizar favoritos.") }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                db.collection("posts").document(postId).delete().await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al borrar el post.") }
            }
        }
    }

    fun updatePost(postId: String, newTitle: String, newContent: String, topic: String) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                val updates = mapOf(
                    "title" to newTitle,
                    "content" to newContent,
                    "topic" to topic
                )
                postRef.update(updates).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al actualizar el post.") }
            }
        }
    }
}