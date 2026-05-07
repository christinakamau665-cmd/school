package com.emobilis.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.emobilis.app.data.model.Message
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            db.collection("messages").add(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(uid: String): List<Message> {
        return try {
            db.collection("messages")
                .whereEqualTo("receiverUid", uid)
                .get().await()
                .toObjects(Message::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
