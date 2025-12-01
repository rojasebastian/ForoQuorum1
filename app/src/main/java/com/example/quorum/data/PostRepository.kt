package com.example.quorum.data

import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
    suspend fun addPost(title: String, content: String, topic: String): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun updatePost(postId: String, newTitle: String, newContent: String, newTopic: String): Result<Unit>
    suspend fun toggleFavorite(postId: String, currentFavorites: List<String>): Result<Unit>
}
