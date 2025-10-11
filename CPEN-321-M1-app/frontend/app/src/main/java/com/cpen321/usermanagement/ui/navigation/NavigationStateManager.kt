package com.cpen321.usermanagement.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.cpen321.usermanagement.utils.FeatureContext

sealed class NavigationEvent {
    object NavigateToAuth : NavigationEvent()
    object NavigateToMain : NavigationEvent()
    object NavigateToProfileCompletion : NavigationEvent()
    object NavigateToProfile : NavigationEvent()
    object NavigateToManageProfile : NavigationEvent()
    data class NavigateToAuthWithMessage(val message: String) : NavigationEvent()
    data class NavigateToMainWithMessage(val message: String) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    object ClearBackStack : NavigationEvent()
    object NoNavigation : NavigationEvent()

    //feature navigation events
    data class NavigateToChat(val context: FeatureContext) : NavigationEvent()
    data class NavigateToCopy(val context: FeatureContext) : NavigationEvent()
    data class NavigateToFields(val context: FeatureContext) : NavigationEvent()
    data class NavigateToFilter(val context: FeatureContext) : NavigationEvent()
    data class NavigateToInvite(val context: FeatureContext) : NavigationEvent()
    data class NavigateToMembersManager(val context: FeatureContext) : NavigationEvent()
    data class NavigateToMembers(val context: FeatureContext) : NavigationEvent()
    data class NavigateToNote(val context: FeatureContext) : NavigationEvent()
    data class NavigateToOtherProfile(val context: FeatureContext) : NavigationEvent()
    data class NavigateToSharing(val context: FeatureContext) : NavigationEvent()
    data class NavigateToTemplate(val context: FeatureContext) : NavigationEvent()
    data class NavigateToWsCreation(val context: FeatureContext) : NavigationEvent()
    data class NavigateToWsProfileManager(val context: FeatureContext) : NavigationEvent()
    data class NavigateToWsProfile(val context: FeatureContext) : NavigationEvent()
    data class NavigateToMainWithContext(val context: FeatureContext): NavigationEvent()
}


data class NavigationState(
    val currentRoute: String = NavRoutes.LOADING,
    val isAuthenticated: Boolean = false,
    val needsProfileCompletion: Boolean = false,
    val isLoading: Boolean = true,
    val isNavigating: Boolean = false,
    val context: FeatureContext = FeatureContext()
)

@Singleton
class NavigationStateManager @Inject constructor() {

    private val _navigationEvent = MutableStateFlow<NavigationEvent>(NavigationEvent.NoNavigation)
    val navigationEvent: StateFlow<NavigationEvent> = _navigationEvent.asStateFlow()

    private val _navigationState = MutableStateFlow(NavigationState())

    /**
     * Updates the authentication state and triggers appropriate navigation
     */
    fun updateAuthenticationState(
        isAuthenticated: Boolean,
        needsProfileCompletion: Boolean,
        isLoading: Boolean = false,
        currentRoute: String = _navigationState.value.currentRoute
    ) {
        val newState = _navigationState.value.copy(
            isAuthenticated = isAuthenticated,
            needsProfileCompletion = needsProfileCompletion,
            isLoading = isLoading,
            currentRoute = currentRoute
        )
        _navigationState.value = newState

        // Trigger navigation based on state
        if (!isLoading) {
            handleAuthenticationNavigation(currentRoute, isAuthenticated, needsProfileCompletion)
        }
    }

    /**
     * Handle navigation decisions based on authentication state
     */
    private fun handleAuthenticationNavigation(
        currentRoute: String,
        isAuthenticated: Boolean,
        needsProfileCompletion: Boolean
    ) {
        when {
            // From loading screen after auth check
            currentRoute == NavRoutes.LOADING -> {
                if (isAuthenticated) {
                    if (needsProfileCompletion) {
                        navigateToProfileCompletion()
                    } else {
                        navigateToMain()
                    }
                } else {
                    navigateToAuth()
                }
            }
            // From auth screen after successful login
            currentRoute.startsWith(NavRoutes.AUTH) && isAuthenticated -> {
                if (needsProfileCompletion) {
                    navigateToProfileCompletion()
                } else {
                    navigateToMain()
                }
            }
        }
    }

    /**
     * Navigate to auth screen
     */
    fun navigateToAuth() {
        _navigationEvent.value = NavigationEvent.NavigateToAuth
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.AUTH)
    }

    /**
     * Navigate to auth screen with success message
     */
    fun navigateToAuthWithMessage(message: String) {
        _navigationEvent.value = NavigationEvent.NavigateToAuthWithMessage(message)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.AUTH)
    }

    /**
     * Navigate to main screen
     */
    fun navigateToMain() {
        _navigationEvent.value = NavigationEvent.NavigateToMain
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MAIN)
    }

    /**
     * Navigate to main screen with success message
     */
    fun navigateToMainWithMessage(message: String) {
        _navigationEvent.value = NavigationEvent.NavigateToMainWithMessage(message)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MAIN)
    }

    /**
     * Navigate to chat screen with standard context
     */
    fun navigateToChat(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToChat(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.CHAT, context = context)
    }

    /**
     * Navigate to copy screen with standard context
     */
    fun navigateToCopy(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToCopy(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.COPY, context = context)
    }

    /**
     * Navigate to fields screen with standard context
     */
    fun navigateToFields(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToFields(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.FIELDS, context = context)
    }

    /**
     * Navigate to filter screen with standard context
     */
    fun navigateToFilter(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToFilter(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.FILTER, context = context)
    }

    /**
     * Navigate to invite screen with standard context
     */
    fun navigateToInvite(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToInvite(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.INVITE, context = context)
    }

    /**
     * Navigate to members manager screen with standard context
     */
    fun navigateToMembersManager(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToMembersManager(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MEMBERS_MANAGER, context = context)
    }

    /**
     * Navigate to members screen with standard context
     */
    fun navigateToMembers(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToMembers(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MEMBERS, context = context)
    }

    /**
     * Navigate to main screen with standard context
     */
    fun navigateToMainWithContext(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToMainWithContext(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MAIN, context = context)
    }

    /**
     * Navigate to note screen with standard context
     */
    fun navigateToNote(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToNote(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.NOTE, context = context)
    }

    /**
     * Navigate to other profile screen with standard context
     */
    fun navigateToOtherProfile(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToOtherProfile(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.OTHER_PROFILE, context = context)
    }

    /**
     * Navigate to sharing screen with standard context
     */
    fun navigateToSharing(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToSharing(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.SHARING, context = context)
    }

    /**
     * Navigate to template screen with standard context
     */
    fun navigateToTemplate(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToTemplate(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.TEMPLATE, context = context)
    }

    /**
     * Navigate to workspace creation screen with standard context
     */
    fun navigateToWsCreation(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToWsCreation(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.WS_CREATION, context = context)
    }

    /**
     * Navigate to workspace profile manager screen with standard context
     */
    fun navigateToWsProfileManager(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToWsProfileManager(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.WS_PROFILE_MANAGER, context = context)
    }

    /**
     * Navigate to workspace profile screen with standard context
     */
    fun navigateToWsProfile(context: FeatureContext) {
        _navigationEvent.value = NavigationEvent.NavigateToWsProfile(context)
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.WS_PROFILE, context = context)
    }

    /**
     * Navigate to profile completion screen
     */
    fun navigateToProfileCompletion() {
        _navigationEvent.value = NavigationEvent.NavigateToProfileCompletion
        _navigationState.value =
            _navigationState.value.copy(currentRoute = NavRoutes.PROFILE_COMPLETION)
    }

    /**
     * Navigate to profile screen
     */
    fun navigateToProfile() {
        _navigationEvent.value = NavigationEvent.NavigateToProfile
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.PROFILE)
    }

    /**
     * Navigate to manage profile screen
     */
    fun navigateToManageProfile() {
        _navigationEvent.value = NavigationEvent.NavigateToManageProfile
        _navigationState.value =
            _navigationState.value.copy(currentRoute = NavRoutes.MANAGE_PROFILE)
    }


    /**
     * Navigate back
     */
    fun navigateBack() {
        _navigationEvent.value = NavigationEvent.NavigateBack
    }

    /**
     * Handle account deletion
     */
    fun handleAccountDeletion() {
        _navigationState.value = _navigationState.value.copy(isNavigating = true)

        updateAuthenticationState(
            isAuthenticated = false,
            needsProfileCompletion = false,
            isLoading = false
        )
        navigateToAuthWithMessage("Account deleted successfully!")
    }

    fun handleSignOut() {
        _navigationState.value = _navigationState.value.copy(isNavigating = true)

        updateAuthenticationState(
            isAuthenticated = false,
            needsProfileCompletion = false,
            isLoading = false
        )
        navigateToAuthWithMessage("Signed Out successfully!")
    }

    /**
     * Handle profile completion
     */
    fun handleProfileCompletion() {
        _navigationState.value = _navigationState.value.copy(needsProfileCompletion = false)
        navigateToMain()
    }

    /**
     * Handle profile completion with success message
     */
    fun handleProfileCompletionWithMessage(message: String) {
        _navigationState.value = _navigationState.value.copy(needsProfileCompletion = false)
        navigateToMainWithMessage(message)
    }

    /**
     * Reset navigation events after handling
     */
    fun clearNavigationEvent() {
        _navigationEvent.value = NavigationEvent.NoNavigation
        // Clear navigating flag when navigation is complete
        _navigationState.value = _navigationState.value.copy(isNavigating = false)
    }
}
