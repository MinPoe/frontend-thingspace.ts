package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.NoteRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    private val _noteState = MutableStateFlow(NoteState())
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _noteState.value = _noteState.value.copy(isLoading = true, error = null)

            val result = noteRepository.getNote(noteId)

            result.fold(
                onSuccess = { note ->
                    _noteState.value = NoteState(
                        note = note,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _noteState.value = NoteState(
                        note = null,
                        isLoading = false,
                        error = exception.message ?: "Failed to load note"
                    )
                }
            )
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _noteState.value = _noteState.value.copy(isDeleting = true, error = null)

            val result = noteRepository.deleteNote(noteId)

            result.fold(
                onSuccess = {
                    _noteState.value = _noteState.value.copy(
                        isDeleting = false,
                        isDeleted = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _noteState.value = _noteState.value.copy(
                        isDeleting = false,
                        error = exception.message ?: "Failed to delete note"
                    )
                }
            )
        }
    }

    fun clearNote() {
        _noteState.value = NoteState()
    }
}