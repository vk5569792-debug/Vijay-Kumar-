package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository

    val allThreads: StateFlow<List<ChatThread>>
    
    private val _activeThreadId = MutableStateFlow<Long?>(null)
    val activeThreadId: StateFlow<Long?> = _activeThreadId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _customSystemPrompt = MutableStateFlow("")
    val customSystemPrompt: StateFlow<String> = _customSystemPrompt.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        val database = ChatDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao())

        allThreads = repository.allThreads.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Monitor threads and ensure at least one thread exists
        viewModelScope.launch {
            allThreads.collect { threads ->
                if (threads.isEmpty() && _isGenerating.value == false) {
                    val newId = repository.createNewThread("Initial Conversation")
                    _activeThreadId.value = newId
                } else if (_activeThreadId.value == null && threads.isNotEmpty()) {
                    _activeThreadId.value = threads.first().id
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessage>> = _activeThreadId
        .flatMapLatest { threadId ->
            if (threadId != null) {
                repository.getMessagesForThread(threadId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun selectThread(threadId: Long) {
        _activeThreadId.value = threadId
    }

    fun startNewThread() {
        viewModelScope.launch {
            val newId = repository.createNewThread("New Chat")
            _activeThreadId.value = newId
            _inputText.value = ""
        }
    }

    fun deleteThread(threadId: Long) {
        viewModelScope.launch {
            repository.deleteThread(threadId)
            if (_activeThreadId.value == threadId) {
                _activeThreadId.value = allThreads.value.firstOrNull { it.id != threadId }?.id
            }
        }
    }

    fun sendMessage() {
        val prompt = _inputText.value.trim()
        val threadId = _activeThreadId.value
        if (prompt.isEmpty() || threadId == null || _isGenerating.value) return

        _inputText.value = ""
        _isGenerating.value = true

        viewModelScope.launch {
            repository.sendMessageToJarvis(
                threadId = threadId,
                userPrompt = prompt,
                temperature = _temperature.value,
                customSystemPrompt = _customSystemPrompt.value
            )
            _isGenerating.value = false
        }
    }

    fun setTemperature(temp: Float) {
        _temperature.value = temp
    }

    fun setCustomSystemPrompt(prompt: String) {
        _customSystemPrompt.value = prompt
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearAllThreads()
            _activeThreadId.value = null
            _inputText.value = ""
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(application) as T
        }
    }
}
