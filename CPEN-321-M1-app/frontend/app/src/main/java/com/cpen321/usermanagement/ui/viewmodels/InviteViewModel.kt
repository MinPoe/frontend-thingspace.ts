package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.WorkspaceRepository
import com.cpen321.usermanagement.data.repository.WsMembershipStatus
import com.cpen321.usermanagement.ui.navigation.NavigationEvent
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InviteUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val typedEmail: String = ""
)

@HiltViewModel
class InviteViewModel@Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val profileRepository: ProfileRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteUiState())
    val uiState: StateFlow<InviteUiState> = _uiState.asStateFlow()

    companion object{
        val TAG = "InviteViewModel"
    }

    fun onInviteClick(typedEmail: String){
        setIsLoading(true)
        _uiState.value = _uiState.value.copy(typedEmail = typedEmail)
        viewModelScope.launch {
            val profileRequest = profileRepository.getProfileByEmail(_uiState.value.typedEmail)
            if (profileRequest.isSuccess){
                val user = profileRequest.getOrNull()!!
                val membershipStatusRequest = workspaceRepository.getMembershipStatus(user._id)
                if (membershipStatusRequest.isSuccess){
                    val membershipStatus = membershipStatusRequest.getOrNull()!!
                    when (membershipStatus) {
                        WsMembershipStatus.NONMEMBER ->{
                            val inviteRequest = workspaceRepository.addMember(user._id,
                                navigationStateManager.getWorkspaceId())
                            if (inviteRequest.isSuccess){
                                _uiState.value = _uiState.value.copy(successMessage = "The user got added to the workspace.",
                                    errorMessage = null)
                            }
                            else{
                                Log.e(TAG, "Could not add the user to the workspace!")
                                _uiState.value = _uiState.value.copy(errorMessage = "Could not add the user to the workspace!")
                            }
                        }
                        WsMembershipStatus.MEMBER -> {
                            _uiState.value = _uiState.value.copy(errorMessage = "The user is already a member!")
                        }
                        WsMembershipStatus.BANNED -> {
                            _uiState.value = _uiState.value.copy(errorMessage = "That user is banned!")
                        }
                        WsMembershipStatus.MANAGER -> {
                            _uiState.value = _uiState.value.copy(errorMessage = "The user is already a member!")
                        }
                    }
                }
                else{
                    Log.e(TAG, "Could not check the membership status of the user!")
                    _uiState.value = _uiState.value.copy(errorMessage = "Could not check the membership status of the user!")
                }
            }
            else{
                Log.e(TAG, "Could not retrieve the profile mathing the given email!")
                _uiState.value = _uiState.value.copy(errorMessage = "Could not retrieve the profile matching the given email!")
            }
        }
        setIsLoading(false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun setIsLoading(isLoading:Boolean){
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
}