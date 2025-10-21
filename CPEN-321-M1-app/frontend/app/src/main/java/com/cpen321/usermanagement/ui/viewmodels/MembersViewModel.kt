package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MembersUiState(
    val members:List<User> = emptyList(),
    val isLoading:Boolean = false,
    val user:User? = null,
    val workspaceId:String? = null
)

@HiltViewModel
open class MembersViewModel@Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {
    companion object {
        private const val TAG = "WsSelectionViewModel"
    }

    protected val _uiState = MutableStateFlow<MembersUiState>(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

    fun getUsers(): Pair<User, List<User>> {
        if (uiState.value.workspaceId != navigationStateManager.getWorkspaceId()) {
            viewModelScope.launch { loadUsers() }
            _uiState.value = _uiState.value.copy(
                workspaceId = navigationStateManager.getWorkspaceId())
        }
        //TODO: think abt the default user
        return Pair(uiState.value.user ?: User("",
            "", "","", ""), uiState.value.members)
    }

    private suspend fun loadUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        val profileResult = profileRepository.getProfile()
        if (profileResult.isSuccess){
            _uiState.value = _uiState.value.copy(user = profileResult.getOrNull()!!)
        }
        else{
            _uiState.value = _uiState.value.copy(user = null)
        }

        val profilesResult = workspaceRepository.getWorkspaceMembers(
            navigationStateManager.getWorkspaceId())
        if (profilesResult.isSuccess) {
            val members = profilesResult.getOrNull()!!.toMutableList()
            if (_uiState.value.user != null) {
                val user = _uiState.value.user!!
                members.removeIf { it._id == user._id }
            }
            _uiState.value = _uiState.value.copy(members = members, isLoading = false)
        } else {
            _uiState.value = _uiState.value.copy(members = emptyList(), isLoading = false)
            //TODO: for now!!!

        }
    }
}