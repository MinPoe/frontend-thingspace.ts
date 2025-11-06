package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WsLeaveState{
    NOT, DURING, DONE
}

data class WsProfileUiState(
    // Loading states
    val isLoadingProfile: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isLoadingPhoto: Boolean = false,
    val leaveState: WsLeaveState = WsLeaveState.NOT,

    // Data states
    val workspace: Workspace? = null,

    // Message states
    val errorMessage: String? = null,
)

@HiltViewModel
open class WsProfileViewModel@Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    companion object{
        val TAG = "WsProfileViewModel"
    }

    private val _uiState = MutableStateFlow(WsProfileUiState())
    val uiState: StateFlow<WsProfileUiState> = _uiState.asStateFlow()


    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfile = true, errorMessage = null)

            val profileResult: Result<Workspace>
            profileResult = workspaceRepository.getWorkspace(
                navigationStateManager.state.getWorkspaceId()
            )


            if (profileResult.isSuccess) {
                val workspace = profileResult.getOrNull()!!

                _uiState.value = _uiState.value.copy(
                    isLoadingProfile = false,
                    workspace = workspace,
                )
            } else {
                val errorMessage = when {
                    profileResult.isFailure -> {
                        val error = profileResult.exceptionOrNull()
                        Log.e(TAG, "Failed to load profile", error)
                        error?.message ?: "Failed to load profile"
                    }

                    else -> {
                        Log.e(TAG, "Failed to load data")
                        "Failed to load data"
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoadingProfile = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun setLoadingPhoto(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoadingPhoto = isLoading)
    }

    fun leaveWorkspace():Boolean{
        _uiState.value = _uiState.value.copy(leaveState = WsLeaveState.DURING)
        var leaveSuccessful = false
        viewModelScope.launch {
            val profileRequest = profileRepository.getProfile()
            if (profileRequest.isSuccess){
              leaveSuccessful = workspaceRepository.leave(profileRequest.getOrNull()!!._id,
                   navigationStateManager.state.getWorkspaceId(),).isSuccess
            }
        } //TODO: handle errors and messaging later
        _uiState.value = _uiState.value.copy(leaveState = WsLeaveState.DONE)
        return leaveSuccessful
    }

    fun resetLeaveState(){
        _uiState.value = _uiState.value.copy(leaveState = WsLeaveState.NOT)
    }

}