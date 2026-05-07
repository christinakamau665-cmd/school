package com.emobilis.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emobilis.app.data.model.Message
import com.emobilis.app.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    private val repo = MessageRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            repo.sendMessage(message)
        }
    }

    fun loadMessages(uid: String) {
        viewModelScope.launch {
            _messages.value = repo.getMessages(uid)
        }
    }
}
