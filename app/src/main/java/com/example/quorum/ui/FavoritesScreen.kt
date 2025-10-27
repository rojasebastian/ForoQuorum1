package com.example.quorum.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quorum.FavoritesViewModel
import com.example.quorum.HomeViewModel
import com.example.quorum.data.Post

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val uiState by favoritesViewModel.uiState.collectAsState()

    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }
    val topics = listOf("Química", "Física", "Astronomía")

    Scaffold {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(text = "Error: ${uiState.error}", color = Color.Red)
                }

                uiState.posts.isEmpty() -> {
                    Text(text = "Aún no tienes posts favoritos", fontSize = 18.sp)
                }

                else -> {
                    PostList(
                        posts = uiState.posts,
                        onEdit = { postToEdit = it },
                        onDelete = { postToDelete = it },
                        onToggleFavorite = { postId, favorites ->
                            homeViewModel.toggleFavorite(postId, favorites)
                        },
                        navController = navController
                    )
                }
            }
        }

        postToEdit?.let { post ->
            EditPostDialog(
                post = post,
                topics = topics,
                onDismiss = { postToEdit = null },
                onConfirm = { title, content, topic ->
                    homeViewModel.updatePost(post.id, title, content, topic)
                    postToEdit = null
                }
            )
        }

        postToDelete?.let { post ->
            DeleteConfirmationDialog(
                post = post,
                onDismiss = { postToDelete = null },
                onConfirm = {
                    homeViewModel.deletePost(post.id)
                    postToDelete = null
                }
            )
        }
    }
}
