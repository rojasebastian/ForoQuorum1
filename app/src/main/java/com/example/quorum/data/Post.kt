package com.example.quorum.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Este es el "molde" para cada post en el foro.
// Firestore usa este 'data class' para convertir los datos automáticamente.
data class Post(
    // @DocumentId le dice a Firestore que llene este campo con el ID del documento
    @DocumentId
    val id: String = "", // ID único del post

    val title: String = "", // Título del post
    val content: String = "", // Contenido del post

    val authorEmail: String = "", // Email del usuario que lo creó
    val authorId: String = "", // ID del usuario que lo creó

    // @ServerTimestamp le dice a Firestore que ponga la hora del servidor aquí
    @ServerTimestamp
    val timestamp: Date? = null, // Fecha de creación

    // Listas para likes y favoritos (para pasos futuros)
    val likes: List<String> = emptyList(), // Lista de UserIDs que dieron like
    val favorites: List<String> = emptyList() // Lista de UserIDs que lo guardaron
)
