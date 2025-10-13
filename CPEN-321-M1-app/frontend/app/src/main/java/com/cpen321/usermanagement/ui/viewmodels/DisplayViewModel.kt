package com.cpen321.usermanagement.ui.viewmodels

import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository

@HiltViewModel
open class DisplayViewModel @Inject constructor(
    private val navigationStateManager: NavigationStateManager,
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository) : ViewModel() {

    private val _wsname = "personal"
    private val _wsid = ""
    fun getWorkspaceName():String{
        val workspaceId = navigationStateManager.getWorkspaceId()
        return if (workspaceId != "") workspaceId else "personal" //TODO: if "" should move to userId
    }
}