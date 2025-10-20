package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.repository.NoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.chunked

@HiltViewModel
open class DisplayViewModel @Inject constructor(
    private val navigationStateManager: NavigationStateManager,
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val noteRepository: NoteRepository) : ViewModel() {

    private var _wsname = "personal"
    private var _wsid = ""
    private var _wsdescr = ""
    private var _wspic = ""

    private var _notesPerPage = 10

    protected var _notesFound: List<List<Note>> = emptyList()

    companion object {
        private const val TAG = "DisplayViewModel"
    }

    fun getNotesTitlesFound(page: Int):List<Note>{
        return  _notesFound[page] //TODO: for now
    }

    fun onLoad(){
        viewModelScope.launch{
            cacheUpdateWorkspaceOrUser(navigationStateManager.getWorkspaceId())
            searchResults()
        }
    }

    fun getWorkspaceName():String{
        val workspaceId = navigationStateManager.getWorkspaceId()
        viewModelScope.launch{cacheUpdateWorkspaceOrUser(workspaceId)}
        return _wsname //TODO: if "" should move to userId
    }

    fun searchedNotesUpdate(){
        //TODO: Add pagination later
        viewModelScope.launch { searchResults() }
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

    protected open suspend fun searchResults(){
        val tags = navigationStateManager.getSelectedTags() //TODO: Add TAG logic

        val noteSearchResult = noteRepository.findNotes( //TODO: Pagination later
            workspaceId = navigationStateManager.getWorkspaceId(),
            noteType = navigationStateManager.getNoteType(),
            searchQuery = navigationStateManager.getSearchQuery(),
            tagsToInclude = tags,
            notesPerPage = _notesPerPage
            )
        if (noteSearchResult.isSuccess){
            val rawNotesFound = noteSearchResult.getOrNull()!!
            _notesFound = rawNotesFound.chunked(_notesPerPage)
        }
        else{
            _notesFound = emptyList()
        }
    }

    suspend fun loadAllUserTags(){
        val tagsRequest = workspaceRepository.getAllTags(
            navigationStateManager.getWorkspaceId())
        if (tagsRequest.isSuccess){
            val allTags = tagsRequest.getOrNull()!!
            navigationStateManager.updateTagSelection(allTags, true)
        }
        else{
            navigationStateManager.updateTagSelection(emptyList(),
                false)
        }
    }
}