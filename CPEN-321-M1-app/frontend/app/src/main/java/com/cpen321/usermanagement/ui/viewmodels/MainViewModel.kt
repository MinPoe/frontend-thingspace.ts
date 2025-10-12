package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.cpen321.usermanagement.ui.viewmodels.DisplayViewModel

data class MainUiState(
    val successMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(private val navigationStateManager: NavigationStateManager) : DisplayViewModel(navigationStateManager) {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun setSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }


}
