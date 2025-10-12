package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel@Inject constructor(
    private val authRepository: AuthRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {
    fun getWorkspaceName():String{
        val workspaceId = navigationStateManager.getWorkspaceId()
        return if (workspaceId != "") workspaceId else "personal" //TODO: if null should move to userId
    }
}