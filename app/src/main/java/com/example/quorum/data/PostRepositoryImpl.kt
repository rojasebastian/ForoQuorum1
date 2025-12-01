package com.example.quorum.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class PostRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : PostRepository {

    override fun getPosts(): Flow<Result<List<Post>>> = callbackFlow {
        val listener = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val postsList = snapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)
                        val topic = if (post?.topic.isNullOrBlank()) "Química" else post!!.topic
                        post?.copy(id = doc.id, topic = topic)
                    }
                    trySend(Result.success(postsList))
                } else {
                    trySend(Result.success(emptyList()))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addPost(title: String, content: String, topic: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val newPost = Post(
                authorEmail = currentUser.email ?: "Anónimo",
                authorId = currentUser.uid,
                title = title,
                content = content,
                timestamp = Date(),
                topic = topic
            )
            db.collection("posts").add(newPost).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            db.collection("posts").document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postId: String, newTitle: String, newContent: String, newTopic: String): Result<Unit> {
        return try {
            val postRef = db.collection("posts").document(postId)
            val updates = mapOf(
                "title" to newTitle,
                "content" to newContent,
                "topic" to newTopic
            )
            postRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(postId: String, currentFavorites: List<String>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val postRef = db.collection("posts").document(postId)
            val updatedFavorites = if (currentFavorites.contains(userId)) {
                FieldValue.arrayRemove(userId)
            } else {
                FieldValue.arrayUnion(userId)
            }
            postRef.update("favorites", updatedFavorites).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}