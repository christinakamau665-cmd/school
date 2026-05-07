package com.emobilis.app.data.model

data class Message(
    val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val receiverUid: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val type: String = "general"
)
