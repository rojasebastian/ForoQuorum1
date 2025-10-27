package com.example.quorum.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quorum.HomeViewModel
import com.example.quorum.data.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddPostDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    // Lógica para el filtro
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    val topics = listOf("Química", "Física", "Astronomía")
    val filteredPosts = uiState.posts.filter { post ->
        selectedTopic == null || post.topic == selectedTopic
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddPostDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear nuevo post")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Barra de filtros
            TopicFilterBar(
                topics = topics,
                selectedTopic = selectedTopic,
                onTopicSelected = { selectedTopic = it }
            )

            // Contenido principal
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.error != null -> Text(text = "Error: ${uiState.error}", color = Color.Red)
                    uiState.posts.isEmpty() -> Text(text = "No hay posts. ¡Crea el primero!", fontSize = 18.sp)
                    else -> {
                        PostList(
                            posts = filteredPosts, // Usa la lista filtrada
                            onEdit = { postToEdit = it },
                            onDelete = { postToDelete = it },
                            onToggleFavorite = { postId, favorites ->
                                viewModel.toggleFavorite(postId, favorites)
                            },
                            navController = navController
                        )
                    }
                }
            }
        }

        if (showAddPostDialog) {
            AddPostDialog(
                onDismiss = { showAddPostDialog = false },
                onConfirm = { title, content ->
                    viewModel.addPost(title, content)
                    showAddPostDialog = false
                }
            )
        }

        postToEdit?.let { post ->
            EditPostDialog(
                post = post,
                onDismiss = { postToEdit = null },
                onConfirm = { title, content ->
                    viewModel.updatePost(post.id, title, content)
                    postToEdit = null
                }
            )
        }

        postToDelete?.let { post ->
            DeleteConfirmationDialog(
                post = post,
                onDismiss = { postToDelete = null },
                onConfirm = {
                    viewModel.deletePost(post.id)
                    postToDelete = null
                }
            )
        }
    }
}

// El resto del archivo permanece sin cambios...

@Composable
fun PostList(
    posts: List<Post>,
    onEdit: (Post) -> Unit,
    onDelete: (Post) -> Unit,
    onToggleFavorite: (String, List<String>) -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            PostCard(
                modifier = Modifier,
                post = post,
                onEdit = { onEdit(post) },
                onDelete = { onDelete(post) },
                onToggleFavorite = { onToggleFavorite(post.id, post.favorites) },
                onCommentClick = { navController.navigate("${PostDetailDest.route}/${post.id}") },
            )
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isFavorite = post.favorites.contains(currentUserId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.titleLarge)
            Text(text = "por ${post.authorEmail}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Marcar como favorito",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else Color.Gray
                    )
                }

                TextButton(onClick = onCommentClick) {
                    Text("Comentar")
                }

                Spacer(modifier = Modifier.weight(1f))

                if (post.authorId == currentUserId) {
                    Row {
                        TextButton(onClick = onEdit) {
                            Text("Editar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Borrar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPostDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val isFormValid = title.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, isError = title.isBlank())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Contenido (ciencia)...") }, isError = content.isBlank(), modifier = Modifier.height(150.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, content) }, enabled = isFormValid) {
                Text("Publicar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditPostDialog(post: Post, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember(post.title) { mutableStateOf(post.title) }
    var content by remember(post.content) { mutableStateOf(post.content) }
    val isFormValid = title.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, isError = title.isBlank())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Contenido (ciencia)...") }, isError = content.isBlank(), modifier = Modifier.height(150.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, content) }, enabled = isFormValid) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun DeleteConfirmationDialog(post: Post, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Borrado") },
        text = { Text("¿Estás seguro de que quieres borrar el post \"${post.title}\"? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Borrar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun TopicFilterBar(
    topics: List<String>,
    selectedTopic: String?,
    onTopicSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(topics) { topic ->
            val isSelected = topic == selectedTopic
            Button(
                onClick = { onTopicSelected(if (isSelected) null else topic) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = topic)
            }
        }
    }
}
