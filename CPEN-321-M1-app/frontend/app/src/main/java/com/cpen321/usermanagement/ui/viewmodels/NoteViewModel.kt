package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null
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

    fun clearNote() {
        _noteState.value = NoteState()
    }
}