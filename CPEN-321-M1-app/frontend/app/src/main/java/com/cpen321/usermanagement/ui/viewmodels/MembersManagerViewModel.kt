package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersManagerViewModel@Inject constructor(
    private val profileRepository: ProfileRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val navigationStateManager: NavigationStateManager
) : MembersViewModel(workspaceRepository, profileRepository, navigationStateManager) {
    fun ban(userId: String){
       viewModelScope.launch {
           _uiState.value = _uiState.value.copy(isLoading = true)
           val banRequest = workspaceRepository.banMember(userId,
               navigationStateManager.state.getWorkspaceId())
           if (banRequest.isSuccess){
               val newMemberList = _uiState.value.members.toMutableList()
               newMemberList.removeIf { it._id == userId }
               _uiState.value = _uiState.value.copy(members = newMemberList.toList())
           } //TODO: add messaging later
           _uiState.value = _uiState.value.copy(isLoading = false)
       }
    }
}