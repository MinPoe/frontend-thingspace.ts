package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CreateWsUiStateE{
    BEFORE, DURING, AFTER
}

data class CreateWsUiState(
    val stateEnum: CreateWsUiStateE = CreateWsUiStateE.BEFORE,
    val newWsId: String = "" //Do not access without checking the enum
)

@HiltViewModel
class WsCreationViewModel@Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateWsUiState>(CreateWsUiState())
    val uiState: StateFlow<CreateWsUiState> = _uiState.asStateFlow()

    fun createWorkspace(wsName: String){
        _uiState.value = _uiState.value.copy(stateEnum = CreateWsUiStateE.DURING)
        viewModelScope.launch {
            val profileRequest = profileRepository.getProfile()
            if (profileRequest.isSuccess)
            {
                val createRequest = workspaceRepository.createWorkspace(
                    profileRequest.getOrNull()!!._id, wsName,
                    "", "") //TODO: Later add options to edit other things on creation
                if (createRequest.isSuccess){
                    _uiState.value = _uiState.value.copy(CreateWsUiStateE.AFTER,
                        createRequest.getOrNull()!!)
                }
                else{
                    _uiState.value = _uiState.value.copy(stateEnum = CreateWsUiStateE.BEFORE) //TODO: messaging later
                }
            }
            else{
                _uiState.value = _uiState.value.copy(stateEnum = CreateWsUiStateE.BEFORE)
            }
        }
    }

    fun resetUIStateEnum(){
        _uiState.value = _uiState.value.copy(stateEnum = CreateWsUiStateE.BEFORE)
    }

}