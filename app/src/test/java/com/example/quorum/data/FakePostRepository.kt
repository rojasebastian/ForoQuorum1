package com.example.quorum.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date

// A fake repository that works with an in-memory list, for testing.
class FakePostRepository : PostRepository {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    private val postsFlow = _posts.asStateFlow()
    private var nextId = 1

    override fun getPosts(): Flow<Result<List<Post>>> {
        return postsFlow.map { Result.success(it) }
    }

    override suspend fun addPost(title: String, content: String, topic: String): Result<Unit> {
        val newPost = Post(
            id = nextId++.toString(),
            authorId = "test_author_id",
            authorEmail = "test@author.com",
            title = title,
            content = content,
            timestamp = Date(),
            topic = topic
        )
        // Emit a new list to notify observers
        _posts.value = _posts.value + newPost
        return Result.success(Unit)
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        val newPosts = _posts.value.toMutableList()
        if (newPosts.removeIf { it.id == postId }) {
            // Emit a new list to notify observers
            _posts.value = newPosts
            return Result.success(Unit)
        } else {
            return Result.failure(Exception("Post not found"))
        }
    }

    override suspend fun updatePost(
        postId: String,
        newTitle: String,
        newContent: String,
        newTopic: String
    ): Result<Unit> {
        val newPosts = _posts.value.toMutableList()
        val index = newPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val oldPost = newPosts[index]
            newPosts[index] = oldPost.copy(title = newTitle, content = newContent, topic = newTopic)
            // Emit a new list to notify observers
            _posts.value = newPosts
            return Result.success(Unit)
        } else {
            return Result.failure(Exception("Post not found"))
        }
    }

    override suspend fun toggleFavorite(postId: String, currentFavorites: List<String>): Result<Unit> {
        val newPosts = _posts.value.toMutableList()
        val index = newPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val oldPost = newPosts[index]
            val isFavorite = oldPost.favorites.contains("test_user_id")
            val newFavorites = if (isFavorite) {
                oldPost.favorites - "test_user_id"
            } else {
                oldPost.favorites + "test_user_id"
            }
            newPosts[index] = oldPost.copy(favorites = newFavorites)
            // Emit a new list to notify observers
            _posts.value = newPosts
            return Result.success(Unit)
        } else {
            return Result.failure(Exception("Post not found"))
        }
    }
}