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

    val wsSelectUIState = WsSelectUIState(null, null)

    fun getUserAndWorkspaces(): Pair<User, List<Workspace>>{
        if (wsSelectUIState.user==null || wsSelectUIState.workspaces==null){
            loadUserAndWorkspaces()
        }
        //TODO: think abt the default user
        return Pair(wsSelectUIState.user ?: User(_id = "",
            email = "", bio="", name="", profilePicture = ""),
            wsSelectUIState.workspaces ?: emptyList())
    }

    private fun loadUserAndWorkspaces(){
        viewModelScope.launch{
            val user = getUser()
            wsSelectUIState.user = user
            wsSelectUIState.workspaces = getWorkspaces(user._id)
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
    var user:User?,
    var workspaces:List<Workspace>?
)