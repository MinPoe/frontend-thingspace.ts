package com.cpen321.usermanagement.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.remote.dto.Profile
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DeletingTracer{
    NOT, DURING, DONE
}

data class WsProfileManagerUiState(
    // Loading states
    val isLoadingProfile: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isLoadingPhoto: Boolean = false,
    val deleting: DeletingTracer = DeletingTracer.NOT,

    // Data states
    val workspace: Workspace? = null,

    // Message states
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class WsProfileManagerViewModel@Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(WsProfileManagerUiState())
    val uiState: StateFlow<WsProfileManagerUiState> = _uiState.asStateFlow()

    fun loadProfile(otherProfileId:String?=null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfile = true, errorMessage = null)

            val profileResult: Result<Workspace> = workspaceRepository.getWorkspace(
                navigationStateManager.getWorkspaceId())

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
                        Log.e(TAG, "Failed to load workspace profile", error)
                        error?.message ?: "Failed to load workspace profile"
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

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun setLoadingPhoto(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoadingPhoto = isLoading)
    }

    fun uploadProfilePicture(pictureUri: Uri) {
        viewModelScope.launch {
            val result = workspaceRepository.updateWorkspacePicture(
                workspaceProfilePicture = pictureUri.toString(),
                workspaceId = navigationStateManager.getWorkspaceId()
            )
            if (result.isSuccess and (_uiState.value.workspace != null)) {
                val currentWorkspace = _uiState.value.workspace!!
                val updatedWorkspace = currentWorkspace.copy(
                    profile = currentWorkspace.profile.copy(imagePath = pictureUri.toString())
                )
                _uiState.value = _uiState.value.copy(isLoadingPhoto = false, workspace= updatedWorkspace, successMessage = "Profile picture updated successfully!")
            }else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to update profile", error)
                val errorMessage = error?.message ?: "Failed to update profile"
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    errorMessage = errorMessage
                )
            }

        }
    }

    fun updateProfile(name: String, description: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isSavingProfile = true,
                    errorMessage = null,
                    successMessage = null,
                )

            val result = workspaceRepository.updateWorkspaceProfile(
                navigationStateManager.getWorkspaceId(),
                name,
                description)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    workspace = Workspace(
                        _id = navigationStateManager.getWorkspaceId(),
                        profile = Profile(
                            imagePath = _uiState.value.workspace?.profile?.imagePath,
                            name = name,
                            description = description
                        )
                    ),
                    successMessage = "Profile updated successfully!"
                )
                onSuccess()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to update profile", error)
                val errorMessage = error?.message ?: "Failed to update profile"
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun deleteWorkspace():Boolean{
        _uiState.value = _uiState.value.copy(deleting = DeletingTracer.DURING)
        var deleteSuccessful = false
        viewModelScope.launch {
            val delRequest = workspaceRepository.deleteWorkspace(
                navigationStateManager.getWorkspaceId())
            if (delRequest.isSuccess){
                deleteSuccessful = true
            }
        } //TODO: handle errors and messaging later
        _uiState.value = _uiState.value.copy(deleting = DeletingTracer.DONE)
        return deleteSuccessful
    }

    fun setDelTracer(deleting: DeletingTracer){
        _uiState.value = _uiState.value.copy(deleting = deleting)
    }
}