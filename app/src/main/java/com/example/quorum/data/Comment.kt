package com.example.quorum.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Este es el "molde" para cada comentario dentro de un post.
data class Comment(
    @DocumentId
    val id: String = "", // ID único del comentario

    val text: String = "", // El contenido del comentario

    val authorEmail: String = "", // Email del usuario que lo creó
    val authorId: String = "", // ID del usuario que lo creó

    @ServerTimestamp
    val timestamp: Date? = null // Fecha de creación para ordenarlos
)
