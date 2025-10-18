package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel@Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    var _allTags:List<String> = emptyList()

    fun onLoad(){
        viewModelScope.launch{updateAvailableTags()}
    }

    fun getAvailTags(): List<String>{
        return _allTags
    }

    private suspend fun updateAvailableTags(){
        val result:Result<List<String>> = workspaceRepository.getAllTags(
            navigationStateManager.getWorkspaceId())
        if(result.isSuccess){
            _allTags = result.getOrNull()!!
        }
        else{
            _allTags = emptyList()
        }
    }
}