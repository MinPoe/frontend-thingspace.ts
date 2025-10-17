package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.NoteRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel@Inject constructor(
    private val navigationStateManager: NavigationStateManager,
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val noteRepository: NoteRepository) : DisplayViewModel(
    navigationStateManager, workspaceRepository, profileRepository, noteRepository) {
}