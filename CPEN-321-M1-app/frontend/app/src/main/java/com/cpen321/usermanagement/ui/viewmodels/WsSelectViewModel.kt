package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun getUserAndWorkspaces(): Pair<User, List<Workspace>>{
        val user = runBlocking { getUser() }
        val workspaces = runBlocking { getWorkspaces(user._id) }
        return Pair(user, workspaces)
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