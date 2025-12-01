package com.example.quorum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.Apod
import com.example.quorum.data.NasaApiService
import com.example.quorum.data.Post
import com.example.quorum.data.PostRepository
import com.example.quorum.data.PostRepositoryImpl
import com.example.quorum.data.RetrofitClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val apod: Apod? = null
)

class HomeViewModel(
    private val postRepository: PostRepository,
    private val nasaApiService: NasaApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
        fetchApod()
    }

    private fun fetchApod() {
        viewModelScope.launch {
            try {
                val response = nasaApiService.getAstronomyPictureOfTheDay("DEMO_KEY")
                if (response.mediaType == "image") {
                    _uiState.update { it.copy(apod = response) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching from NASA API", e)
                // Optional: Show a specific error for APOD failure
            }
        }
    }

    private fun loadPosts() {
        _uiState.update { it.copy(isLoading = true) }
        postRepository.getPosts()
            .onEach { result ->
                result
                    .onSuccess { posts ->
                        _uiState.update { it.copy(isLoading = false, posts = posts, error = null) }
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = "Error al cargar posts.") }
                    }
            }
            .launchIn(viewModelScope)
    }

    fun addPost(title: String, content: String, topic: String) {
        viewModelScope.launch {
            postRepository.addPost(title, content, topic)
                .onFailure { _uiState.update { it.copy(error = "Error al crear el post.") } }
        }
    }

    fun toggleFavorite(postId: String, currentFavorites: List<String>) {
        viewModelScope.launch {
            postRepository.toggleFavorite(postId, currentFavorites)
                .onFailure { _uiState.update { it.copy(error = "Error al actualizar favoritos.") } }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId)
                .onFailure { _uiState.update { it.copy(error = "Error al borrar el post.") } }
        }
    }

    fun updatePost(postId: String, newTitle: String, newContent: String, newTopic: String) {
        viewModelScope.launch {
            postRepository.updatePost(postId, newTitle, newContent, newTopic)
                .onFailure { _uiState.update { it.copy(error = "Error al actualizar el post.") } }
        }
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val auth = Firebase.auth
            val db = Firebase.firestore
            val postRepository = PostRepositoryImpl(auth, db)
            val nasaApiService = RetrofitClient.instance
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(postRepository, nasaApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
