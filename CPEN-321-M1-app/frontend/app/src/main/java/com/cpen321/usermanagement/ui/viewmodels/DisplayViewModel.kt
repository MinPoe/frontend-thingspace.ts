package com.cpen321.usermanagement.ui.viewmodels

import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel

@HiltViewModel
open class DisplayViewModel @Inject constructor(private val navigationStateManager: NavigationStateManager) : ViewModel() {
    fun getWorkspaceName():String{
        val workspaceId = navigationStateManager.getWorkspaceId()
        return if (workspaceId != "") workspaceId else "personal" //TODO: if "" should move to userId
    }
}