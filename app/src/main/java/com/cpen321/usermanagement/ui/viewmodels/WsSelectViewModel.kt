package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class WsSelectViewModel@Inject constructor(
    private val profileRepository: ProfileRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {
    companion object{
        private const val TAG = "WsSelectionViewModel"
    }

    private val _uiState = MutableStateFlow<WsSelectUIState>(WsSelectUIState())
    val uiState: StateFlow<WsSelectUIState> = _uiState.asStateFlow()

    fun getUserAndWorkspaces(): Pair<User, List<Workspace>>{
        if (uiState.value.user==null || uiState.value.workspaces==null){
            loadUserAndWorkspaces()
        }
        //TODO: think abt the default user
        return Pair(uiState.value.user ?: User(_id = "",
            email = "", bio="", name="", profilePicture = ""),
            uiState.value.workspaces ?: emptyList())
    }

    fun loadUserAndWorkspaces(){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getUser()
            _uiState.value = _uiState.value.copy(user = getUser())
            _uiState.value = _uiState.value.copy(workspaces = getWorkspaces(user._id))
            Log.d(TAG, "loading workspaces done ${uiState.value.workspaces}, ${uiState.value.user}")
            _uiState.value = _uiState.value.copy(isLoading = false)
        }

    }

    private suspend fun getUser():User{
        val profileResult = profileRepository.getProfile()
        if (profileResult.isSuccess){
            val user = profileResult.getOrNull()!!
            return user
        }
        else
        {
            val error = profileResult.exceptionOrNull()
            Log.e(TAG, "Failed to load profile", error)
            error?.message ?: "Failed to load profile"
            //TODO: for now!!!
            return User(_id = "", email = "", bio="", name="", profilePicture = "")
        }
    }
    private suspend fun getWorkspaces(userId:String):List<Workspace>{
        val workspacesRequest = workspaceRepository.getWorkspacesForUser(userId)
        if (workspacesRequest.isSuccess){
            return workspacesRequest.getOrNull()!!
        }
        else
        {
            val error = workspacesRequest.exceptionOrNull()
            Log.e(TAG, "Failed to load profile", error)
            error?.message ?: "Failed to load profile"
            return emptyList()
        }
    }
}

data class WsSelectUIState(
    var user:User? = null,
    var workspaces:List<Workspace>? = null,
    var isLoading:Boolean = false
)