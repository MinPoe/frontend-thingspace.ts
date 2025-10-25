package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Message
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.repository.MessageRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.withContext


data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val authors: Map<String, User> = emptyMap(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5000)
                checkForNewMessages()
            }
        }
    }

    private suspend fun checkForNewMessages() {
        try {
            val workspaceId = navigationStateManager.getWorkspaceId()
            if (workspaceId.isEmpty()) return

            withContext(Dispatchers.IO) {
                val result = workspaceRepository.pollForNewMessages(workspaceId)
                if (result.isSuccess && result.getOrNull() == true) {
                    withContext(Dispatchers.Main) {
                        loadMessages()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Polling error", e)
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    fun loadMessages() {
        val workspaceId = navigationStateManager.getWorkspaceId()
        if (workspaceId.isEmpty()) {
            Log.e(TAG, "No workspace ID available")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = messageRepository.getMessages(workspaceId)

            if (result.isSuccess) {
                val messages = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    messages = messages.reversed(), // Show oldest first
                    currentUserId = profileRepository.getCurrentUserId(),
                    isLoading = false
                )

                // Load author profiles
                loadAuthors(messages)
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to load messages", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error?.message ?: "Failed to load messages"
                )
            }
        }
    }

    private suspend fun loadAuthors(messages: List<Message>) {
        val authorIds = messages.map { it.authorId }.distinct()
        val authorsMap = mutableMapOf<String, User>()

        authorIds.forEach { authorId ->
            val result = profileRepository.getOtherProfile(authorId)
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    authorsMap[authorId] = user
                }
            }
        }

        _uiState.value = _uiState.value.copy(authors = authorsMap)
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val workspaceId = navigationStateManager.getWorkspaceId()
        if (workspaceId.isEmpty()) {
            Log.e(TAG, "No workspace ID available")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            val result = messageRepository.sendMessage(workspaceId, content.trim())

            if (result.isSuccess) {
                // Reload messages to get the new one
                loadMessages()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to send message", error)
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = error?.message ?: "Failed to send message"
                )
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            val result = messageRepository.deleteMessage(messageId)

            if (result.isSuccess) {
                // Remove from local state
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.filter { it.id != messageId }
                )
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to delete message", error)
                _uiState.value = _uiState.value.copy(
                    error = error?.message ?: "Failed to delete message"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}