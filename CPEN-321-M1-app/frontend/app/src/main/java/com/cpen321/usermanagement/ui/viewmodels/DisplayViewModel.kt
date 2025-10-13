package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.remote.dto.Workspace
import kotlinx.coroutines.runBlocking

@HiltViewModel
open class DisplayViewModel @Inject constructor(
    private val navigationStateManager: NavigationStateManager,
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository) : ViewModel() {

    private var _wsname = "personal"
    private var _wsid = ""
    private var _wsdescr = ""
    private var _wspic = ""

    companion object {
        private const val TAG = "DisplayViewModel"
    }

    fun getWorkspaceName():String{
        val workspaceId = navigationStateManager.getWorkspaceId()
        runBlocking{cacheUpdateWorkspaceOrUser(workspaceId)}
        return _wsname //TODO: if "" should move to userId
    }

    private suspend fun cacheUpdateWorkspaceOrUser(workspaceId:String){
        if (_wsid != workspaceId) {
            val wsRequest = workspaceRepository.getWorkspace(workspaceId)
            if (wsRequest.isSuccess) {
                val ws: Workspace = wsRequest.getOrNull()!!
                _wsid = workspaceId
                _wsname = ws.workspaceName
                _wspic = ws.workspacePicture ?: ""
                _wsdescr = ws.workspaceDescription ?: ""
            }
            else{
                val profileResult = profileRepository.getProfile()
                if (profileResult.isSuccess){
                    val user = profileResult.getOrNull()!!
                    if (user._id==workspaceId){
                        _wsid = workspaceId
                        _wspic = user.profilePicture
                        _wsdescr = user.bio ?: ""
                        _wsname = user.name
                    }
                }
                else
                {
                    val error = profileResult.exceptionOrNull()
                    Log.e(TAG, "Failed to load workspace/profile", error)
                    error?.message ?: "Failed to load workspace/profile"
                }
            }
        }
    }
}