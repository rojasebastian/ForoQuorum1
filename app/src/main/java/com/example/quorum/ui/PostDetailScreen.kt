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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quorum.PostDetailViewModel
import com.example.quorum.data.Comment

@Composable
fun PostDetailScreen(viewModel: PostDetailViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { 
            CommentInput(onCommentSend = { viewModel.addComment(it) }) 
        }
    ) { paddingValues ->
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
                uiState.comments.isEmpty() -> {
                    Text(text = "No hay comentarios. ¡Sé el primero en comentar!", fontSize = 18.sp)
                }
                else -> {
                    CommentList(comments = uiState.comments)
                }
            }
        }
    }
}

@Composable
fun CommentList(comments: List<Comment>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(comments) { comment ->
            CommentCard(comment = comment)
        }
    }
}

@Composable
fun CommentCard(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = comment.authorEmail,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CommentInput(onCommentSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val isTextValid = text.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            label = { Text("Escribe un comentario...") },
            maxLines = 3
        )
        IconButton(onClick = { 
            onCommentSend(text)
            text = "" // Limpia el campo después de enviar
        }, enabled = isTextValid) {
            Icon(Icons.Default.Send, contentDescription = "Enviar comentario")
        }
    }
}
