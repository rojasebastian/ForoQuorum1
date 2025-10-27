package com.example.quorum.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quorum.CommentWithPostInfo
import com.example.quorum.HomeViewModel
import com.example.quorum.ProfileViewModel
import com.example.quorum.data.Post

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    homeViewModel: HomeViewModel,
    navController: NavController
) {
    val uiState by profileViewModel.uiState.collectAsState()

    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }
    val topics = listOf("Química", "Física", "Astronomía")
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // --- ENCABEZADO ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = uiState.userEmail ?: "...", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Divider()
            }
        }

        // --- SECCIÓN MIS PUBLICACIONES ---
        item {
            Text(
                text = "Mis Publicaciones",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            )
        }
        if (uiState.isLoadingPosts) {
            item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
        } else if (uiState.posts.isNotEmpty()) {
            items(uiState.posts) {
                PostCard(
                    post = it,
                    onEdit = { postToEdit = it },
                    onDelete = { postToDelete = it },
                    onToggleFavorite = { homeViewModel.toggleFavorite(it.id, it.favorites) },
                    onCommentClick = { navController.navigate("${PostDetailDest.route}/${it.id}") },
                    onShare = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Mira esta publicación: ${it.title} - ${it.content}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            item { Text("Aún no has creado ningún post", modifier = Modifier.padding(16.dp)) }
        }

        // --- SECCIÓN MIS COMENTARIOS ---
        item {
            Spacer(Modifier.height(16.dp))
            Divider()
            Text(
                text = "Mis Comentarios",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
            )
        }
        if (uiState.isLoadingComments) {
            item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
        } else if (uiState.comments.isNotEmpty()) {
            items(uiState.comments) {
                UserCommentCard(
                    commentInfo = it,
                    onClick = { navController.navigate("${PostDetailDest.route}/${it.postId}") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            item { Text("Aún no has hecho ningún comentario", modifier = Modifier.padding(16.dp)) }
        }
    }

    postToEdit?.let {
        EditPostDialog(
            post = it,
            topics = topics,
            onDismiss = { postToEdit = null },
            onConfirm = { title, content, topic ->
                homeViewModel.updatePost(it.id, title, content, topic)
                postToEdit = null
            }
        )
    }

    postToDelete?.let {
        DeleteConfirmationDialog(
            post = it,
            onDismiss = { postToDelete = null },
            onConfirm = {
                homeViewModel.deletePost(it.id)
                postToDelete = null
            }
        )
    }
}

@Composable
fun UserCommentCard(commentInfo: CommentWithPostInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "\"${commentInfo.comment.text}\"", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "en respuesta a: ${commentInfo.postTitle}",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
