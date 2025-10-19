package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.repository.NoteRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.ui.navigation.AppNavigation
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class MainUiState(
    val successMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigationStateManager: NavigationStateManager,
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val noteRepository: NoteRepository) : DisplayViewModel(
    navigationStateManager, workspaceRepository, profileRepository, noteRepository) {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun setSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    suspend fun loadAllUserTags(){
        val tagsRequest = workspaceRepository.getAllTags(
            navigationStateManager.getWorkspaceId())
        if (tagsRequest.isSuccess){
            val allTags = tagsRequest.getOrNull()!!
            navigationStateManager.updateTagSelection(allTags, true)
        }
        else{
            navigationStateManager.updateTagSelection(emptyList(),
                false)
        }
    }
}
