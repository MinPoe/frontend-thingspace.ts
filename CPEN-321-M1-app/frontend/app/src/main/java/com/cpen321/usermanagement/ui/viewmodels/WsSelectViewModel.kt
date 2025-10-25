package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.remote.dto.Profile
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.data.repository.WsMembershipStatus
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
    private val workspaceRepository: WorkspaceRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {
    companion object{
        private const val TAG = "WsSelectionViewModel"
    }

    private val _uiState = MutableStateFlow<WsSelectUIState>(WsSelectUIState())
    val uiState: StateFlow<WsSelectUIState> = _uiState.asStateFlow()


    fun loadUserAndWorkspaces(){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(state = WsSelectUIStateE.LOADING)
            _uiState.value = _uiState.value.copy(personalWs = getPersonalWs())
            _uiState.value = _uiState.value.copy(workspaces = getWorkspaces())
            Log.d(TAG, "loading workspaces done ${uiState.value.workspaces}, ${uiState.value.personalWs}")

            if(_uiState.value.personalWs != null) { //todo parallelize later or make a grouped backend call
                val workspaceManager = mutableListOf<Boolean>()
                val personalWs = _uiState.value.personalWs!!
                for (workspace in _uiState.value.workspaces) {
                    val membershipStatusRequest = workspaceRepository.getMembershipStatus(
                        personalWs._id, workspaceId = workspace._id
                    )
                    if (membershipStatusRequest.isSuccess &&
                        membershipStatusRequest.getOrNull()!! == WsMembershipStatus.MANAGER
                    ) {
                        workspaceManager.add(true)
                    }
                    else{
                        workspaceManager.add(false)
                    }
                }
                _uiState.value = _uiState.value.copy(workspaceManager = workspaceManager)
            }
            _uiState.value = _uiState.value.copy(state = WsSelectUIStateE.DISPLAYING)
        }

    }

    private suspend fun getPersonalWs():Workspace{
        val profileResult = workspaceRepository.getPersonalWorkspace()
        if (profileResult.isSuccess){
            val personalWs = profileResult.getOrNull()!!
            return personalWs
        }
        else
        {
            val error = profileResult.exceptionOrNull()
            Log.e(TAG, "Failed to load personal workspace", error)
            error?.message ?: "Failed to load profile"
            //TODO: for now!!!
            return Workspace(_id = "personal",
                profile = Profile(imagePath = null, name = "personal", description = null))
        }
    }
    private suspend fun getWorkspaces():List<Workspace>{
        val workspacesRequest = workspaceRepository.getWorkspacesForUser()
        if (workspacesRequest.isSuccess){
            return workspacesRequest.getOrNull()!!
        }
        else
        {
            val error = workspacesRequest.exceptionOrNull()
            Log.e(TAG, "Failed to load workspaces", error)
            error?.message ?: "Failed to load workspaces"
            return emptyList()
        }
    }

    fun setToUpdate(){
        _uiState.value = _uiState.value.copy(state = WsSelectUIStateE.TO_UPDATE)
    }
}

data class WsSelectUIState(
    var personalWs:Workspace? = null,
    var workspaces:List<Workspace> = emptyList(),
    var state: WsSelectUIStateE = WsSelectUIStateE.LOADING,
    var workspaceManager:List<Boolean>? = null
)

enum class WsSelectUIStateE{
    TO_UPDATE, LOADING, DISPLAYING
}