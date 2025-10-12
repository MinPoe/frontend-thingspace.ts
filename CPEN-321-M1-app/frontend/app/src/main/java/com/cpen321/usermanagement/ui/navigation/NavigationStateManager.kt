package com.cpen321.usermanagement.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.cpen321.usermanagement.data.remote.dto.NoteType

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

    //feature navigation events:

    /*The commented parameters are for moving between filter and a given main,
    chat or template screen, as well as for searching, for navigating to the note displaying screens
    from elsewhere, we can safely assume that we don't start with search/tags excluded.
    */
    data class NavigateToChat(
        val workspaceId: String,
        val selectedTags: List<String> = emptyList<String>(),
        val allTagsSelected: Boolean = true,
        val searchQuery: String = "") : NavigationEvent()

    //we get here from an already selected note, no more info needed.
    object NavigateToCopy : NavigationEvent()

    //again, we get there from an already selected note
    object NavigateToFields : NavigationEvent()

    /* we need to know workspace to fetch tags,the other params are 4 repeated travel between the
    display and the filter */
    data class NavigateToFilter(
        val workspaceId: String,
        val selectedTags: List<String>,
        val allTagsSelected: Boolean
    ) : NavigationEvent()

    //we travel there from a workspace already selected, no more info needed
    object NavigateToInvite : NavigationEvent()

    //we travel there from a workspace already selected so again no info needed
    object NavigateToMembersManager : NavigationEvent()

    object NavigateToMembers : NavigationEvent()

    /* for now, only needed note id TODO: might experiment later with workspace movement on copy*/
    data class NavigateToNote(val noteId: String) : NavigationEvent()

    data class NavigateToOtherProfile(val otherUserId: String) : NavigationEvent()
    object NavigateToSharing : NavigationEvent()

    //same as to chat or main with context
    data class NavigateToTemplate(val workspaceId: String,
                                  val selectedTags: List<String> = emptyList<String>(),
                                  val allTagsSelected: Boolean = true,
                                  val searchQuery: String = "") : NavigationEvent()

    object NavigateToWsCreation : NavigationEvent()
    data class NavigateToWsProfileManager(val workspaceId: String) : NavigationEvent()
    data class NavigateToWsProfile(val workspaceId: String) : NavigationEvent()
    data class NavigateToMainWithContext(val workspaceId: String,
                                         val selectedTags: List<String> = emptyList<String>(),
                                         val allTagsSelected: Boolean = true,
                                         val searchQuery: String = ""): NavigationEvent()

    //we get there from display screens and do not have to change any state data
    object NavigateToWsSelect: NavigationEvent()
}

data class NavigationState(
    val currentRoute: String = NavRoutes.LOADING,
    val isAuthenticated: Boolean = false,
    val needsProfileCompletion: Boolean = false,
    val isLoading: Boolean = true,
    val isNavigating: Boolean = false,
    val workspaceId: String = "",
    val otherUserId: String = "",
    val noteType: NoteType = NoteType.CONTENT, //or "chat" or "template"
    val noteId: String = "",
    val selectedTags: List<String> = emptyList<String>(),
    val allTagsSelected: Boolean = true,
    val searchQuery: String = "",
)

@Singleton
class NavigationStateManager @Inject constructor() {

    private val _navigationEvent = MutableStateFlow<NavigationEvent>(NavigationEvent.NoNavigation)
    val navigationEvent: StateFlow<NavigationEvent> = _navigationEvent.asStateFlow()

    private val _navigationState = MutableStateFlow(NavigationState())

    /** Auto-generated getters for feature state
    **/
    fun getWorkspaceId(): String {
        return _navigationState.value.workspaceId
    }

    fun getOtherUserId(): String {
        return _navigationState.value.otherUserId
    }

    fun getNoteType(): NoteType {
        return _navigationState.value.noteType
    }

    fun getNoteId(): String {
        return _navigationState.value.noteId
    }

    fun getSelectedTags(): List<String> {
        return _navigationState.value.selectedTags
    }

    fun getAllTagsSelected(): Boolean {
        return _navigationState.value.allTagsSelected
    }

    fun getSearchQuery(): String {
        return _navigationState.value.searchQuery
    }

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
    fun navigateToChat(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    ) {
        _navigationEvent.value = NavigationEvent.NavigateToChat(
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected,
            searchQuery = searchQuery
        )
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.CHAT,
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected,
            searchQuery = searchQuery,
            noteType = NoteType.CHAT //all display screens will have this irregularity
            //to ensure the user ends up at the correct display
        )
    }

    /**
     * Navigate to copy screen with standard context
     */
    fun navigateToCopy() {
        _navigationEvent.value = NavigationEvent.NavigateToCopy
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.COPY)
    }

    /**
     * Navigate to fields screen with standard context
     */
    fun navigateToFields() {
        _navigationEvent.value = NavigationEvent.NavigateToFields
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.FIELDS)
    }

    /**
     * Navigate to filter screen with standard context
     */
    fun navigateToFilter(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean
    ) {
        _navigationEvent.value = NavigationEvent.NavigateToFilter(
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected
        )
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.FILTER,
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected
        )
    }

    /**
     * Navigate to invite screen with standard context
     */
    fun navigateToInvite() {
        _navigationEvent.value = NavigationEvent.NavigateToInvite
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.INVITE)
    }

    /**
     * Navigate to members manager screen with standard context
     */
    fun navigateToMembersManager() {
        _navigationEvent.value = NavigationEvent.NavigateToMembersManager
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MEMBERS_MANAGER)
    }

    /**
     * Navigate to members screen with standard context
     */
    fun navigateToMembers() {
        _navigationEvent.value = NavigationEvent.NavigateToMembers
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.MEMBERS)
    }

    /**
     * Navigate to note screen with standard context
     */
    fun navigateToNote(noteId: String) {
        _navigationEvent.value = NavigationEvent.NavigateToNote(noteId)
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.NOTE,
            noteId = noteId
        )
    }

    /**
     * Navigate to other profile screen with standard context
     */
    fun navigateToOtherProfile(otherUserId: String) {
        _navigationEvent.value = NavigationEvent.NavigateToOtherProfile(otherUserId)
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.OTHER_PROFILE,
            otherUserId = otherUserId
        )
    }

    /**
     * Navigate to sharing screen with standard context
     */
    fun navigateToSharing() {
        _navigationEvent.value = NavigationEvent.NavigateToSharing
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.SHARING)
    }

    /**
     * Navigate to template screen with standard context
     */
    fun navigateToTemplate(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    ) {
        _navigationEvent.value = NavigationEvent.NavigateToTemplate(
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected,
            searchQuery = searchQuery
        )
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.TEMPLATE,
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected,
            searchQuery = searchQuery,
            noteType = NoteType.TEMPLATE
        )
    }

    /**
     * Navigate to workspace creation screen with standard context
     */
    fun navigateToWsCreation() {
        _navigationEvent.value = NavigationEvent.NavigateToWsCreation
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.WS_CREATION)
    }

    /**
     * Navigate to workspace profile manager screen with standard context
     */
    fun navigateToWsProfileManager(workspaceId: String) {
        _navigationEvent.value = NavigationEvent.NavigateToWsProfileManager(workspaceId)
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.WS_PROFILE_MANAGER,
            workspaceId = workspaceId
        )
    }

    /**
     * Navigate to workspace profile screen with standard context
     */
    fun navigateToWsProfile(workspaceId: String) {
        _navigationEvent.value = NavigationEvent.NavigateToWsProfile(workspaceId)
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.WS_PROFILE,
            workspaceId = workspaceId
        )
    }

    /**
     * Navigate to main screen with workspace context
     */
    fun navigateToMainWithContext(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    ) {
        _navigationEvent.value = NavigationEvent.NavigateToMainWithContext(
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected,
            searchQuery = searchQuery
        )
        _navigationState.value = _navigationState.value.copy(
            currentRoute = NavRoutes.MAIN,
            workspaceId = workspaceId,
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected,
            searchQuery = searchQuery,
            noteType = NoteType.CONTENT
        )
    }

    /**
     * Navigate to workspace select screen with standard context
     */
    fun navigateToWsSelect() {
        _navigationEvent.value = NavigationEvent.NavigateToWsSelect
        _navigationState.value = _navigationState.value.copy(currentRoute = NavRoutes.WS_SELECT)
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
