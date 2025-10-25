package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.repository.NoteRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val navigationStateManager: NavigationStateManager,
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val noteRepository: NoteRepository) : DisplayViewModel(
    navigationStateManager, workspaceRepository, profileRepository, noteRepository) {
    private var authors: List<User>? = emptyList()

    companion object {
        val TAG = "ChatViewModel"
    }

        override suspend fun searchResults(){
            super.searchResults()
            //TODO: deal with pagination later
            val authorsRequest = noteRepository.getAuthors(
                _notesFound.flatten().map {it._id })
            if (authorsRequest.isSuccess){
                authors = authorsRequest.getOrNull()!!
            }
            else{
                Log.e(TAG, "Message authors could not be identified")
                authors = null
            }

        }

    fun getNoteAuthors():List<User>?{
        return authors
    }
}