package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharingViewModel@Inject constructor(
    private val authRepository: AuthRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {
    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()
}