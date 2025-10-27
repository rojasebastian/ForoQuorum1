package com.example.quorum.data

import java.util.Date

// Modelo de datos para una publicación.
data class Post(
    val id: String = "",
    val authorEmail: String = "", // Mantener consistencia con PostCard
    val authorId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val topic: String = "", // Campo para el tema
    val favorites: List<String> = emptyList(),
    val comments: List<Comment> = emptyList()
)
