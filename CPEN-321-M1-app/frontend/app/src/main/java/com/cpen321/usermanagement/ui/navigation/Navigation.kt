package com.cpen321.usermanagement.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.screens.AuthScreen
import com.cpen321.usermanagement.ui.screens.ChatScreen
import com.cpen321.usermanagement.ui.screens.LoadingScreen
import com.cpen321.usermanagement.ui.screens.MainScreen
import com.cpen321.usermanagement.ui.screens.ManageProfileScreen
import com.cpen321.usermanagement.ui.screens.OtherProfileScreen
import com.cpen321.usermanagement.ui.screens.ProfileScreenActions
import com.cpen321.usermanagement.ui.screens.ProfileCompletionScreen
import com.cpen321.usermanagement.ui.screens.ProfileScreen
import com.cpen321.usermanagement.ui.screens.TemplateScreen
import com.cpen321.usermanagement.ui.screens.WorkspacesScreen
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.ChatViewModel
import com.cpen321.usermanagement.ui.viewmodels.CopyViewModel
import com.cpen321.usermanagement.ui.viewmodels.FieldsViewModel
import com.cpen321.usermanagement.ui.viewmodels.FilterViewModel
import com.cpen321.usermanagement.ui.viewmodels.InviteViewModel
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.ui.viewmodels.MembersManagerViewModel
import com.cpen321.usermanagement.ui.viewmodels.MembersViewModel
import com.cpen321.usermanagement.ui.viewmodels.NavigationViewModel
import com.cpen321.usermanagement.ui.viewmodels.NoteViewModel
import com.cpen321.usermanagement.ui.screens.NoteCreationScreen
import com.cpen321.usermanagement.ui.viewmodels.NoteCreationViewModel
import com.cpen321.usermanagement.ui.screens.NoteEditScreen
import com.cpen321.usermanagement.ui.viewmodels.NoteEditViewModel
import com.cpen321.usermanagement.ui.viewmodels.ProfileViewModel
import com.cpen321.usermanagement.ui.viewmodels.SharingViewModel
import com.cpen321.usermanagement.ui.viewmodels.TemplateViewModel
import com.cpen321.usermanagement.ui.viewmodels.WsCreationViewModel
import com.cpen321.usermanagement.ui.viewmodels.WsProfileManagerViewModel
import com.cpen321.usermanagement.ui.viewmodels.WsProfileViewModel
import com.cpen321.usermanagement.ui.viewmodels.WsSelectViewModel
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.ui.screens.CreateWorkspaceScreen
import com.cpen321.usermanagement.ui.screens.FilterScreen
import com.cpen321.usermanagement.ui.screens.NoteScreen
import com.cpen321.usermanagement.ui.screens.WsInviteScreen
import com.cpen321.usermanagement.ui.screens.WsMembersManagerScreen
import com.cpen321.usermanagement.ui.screens.WsMembersScreen
import com.cpen321.usermanagement.ui.screens.WsProfileManagerScreen
import com.cpen321.usermanagement.ui.screens.WsProfileScreen
import com.cpen321.usermanagement.utils.FeatureActions
import com.cpen321.usermanagement.utils.INavigationActions
import com.cpen321.usermanagement.utils.IStateWrapper
import com.cpen321.usermanagement.utils.IWorkspaceActions

object NavRoutes {
    const val LOADING = "loading"
    const val AUTH = "auth"
    const val MAIN = "main"
    const val PROFILE = "profile"
    const val MANAGE_PROFILE = "manage_profile"
    const val PROFILE_COMPLETION = "profile_completion"
    const val CHAT = "chat"
    const val COPY = "copy"
    const val FIELDS = "fields"
    const val FILTER = "filter"
    const val INVITE = "invite"
    const val MEMBERS_MANAGER = "members_manager"
    const val MEMBERS = "members"
    const val NOTE = "note"
    const val NOTE_CREATION = "note_creation"
    const val NOTE_EDIT = "note_edit"
    const val OTHER_PROFILE = "other_profile"
    const val SHARING = "sharing"
    const val TEMPLATE = "template"
    const val WS_CREATION = "ws_creation"
    const val WS_PROFILE_MANAGER = "ws_profile_manager"
    const val WS_PROFILE = "ws_profile"
    const val WS_SELECT = "ws_select"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val navigationStateManager = navigationViewModel.navigationStateManager
    val navigationEvent by navigationStateManager.navigationEvent.collectAsState()
    val featureActions = FeatureActions(
        StateWrapper(navigationStateManager),
        WorkspaceActions(navigationStateManager),
        NavigationActions(navigationStateManager))

    val viewModels = initializeNavigationViewModels()

    // Handle navigation events from NavigationStateManager
    LaunchedEffect(navigationEvent) {
        handleNavigationEvent(
            navigationEvent,
            navController,
            navigationStateManager,
            viewModels.authViewModel,
            chatViewModel = viewModels.chatViewModel,
            copyViewModel = viewModels.copyViewModel,
            fieldsViewModel = viewModels.fieldsViewModel,
            filterViewModel = viewModels.filterViewModel,
            inviteViewModel = viewModels.inviteViewModel,
            membersManagerViewModel = viewModels.membersManagerViewModel,
            membersViewModel = viewModels.membersViewModel,
            noteViewModel = viewModels.noteViewModel,
            noteCreationViewModel = viewModels.noteCreationViewModel,
            noteEditViewModel = viewModels.noteEditViewModel,
            sharingViewModel = viewModels.sharingViewModel,
            templateViewModel = viewModels.templateViewModel,
            wsCreationViewModel = viewModels.wsCreationViewModel,
            wsProfileManagerViewModel = viewModels.wsProfileManagerViewModel,
            wsProfileViewModel = viewModels.wsProfileViewModel,
            wsSelectViewModel = viewModels.wsSelectViewModel,
            mainViewModel = viewModels.mainViewModel,
            profileViewModel = viewModels.profileViewModel
        )
    }

    AppNavHost(
        navController = navController,
        authViewModel = viewModels.authViewModel,
        profileViewModel = viewModels.profileViewModel,
        mainViewModel = viewModels.mainViewModel,
        chatViewModel = viewModels.chatViewModel,
        copyViewModel = viewModels.copyViewModel,
        fieldsViewModel = viewModels.fieldsViewModel,
        filterViewModel = viewModels.filterViewModel,
        inviteViewModel = viewModels.inviteViewModel,
        membersManagerViewModel = viewModels.membersManagerViewModel,
        membersViewModel = viewModels.membersViewModel,
        noteViewModel = viewModels.noteViewModel,
        noteCreationViewModel = viewModels.noteCreationViewModel,
        noteEditViewModel = viewModels.noteEditViewModel,
        sharingViewModel = viewModels.sharingViewModel,
        templateViewModel = viewModels.templateViewModel,
        wsCreationViewModel = viewModels.wsCreationViewModel,
        wsProfileManagerViewModel = viewModels.wsProfileManagerViewModel,
        wsProfileViewModel = viewModels.wsProfileViewModel,
        wsSelectViewModel = viewModels.wsSelectViewModel,
        navigationStateManager = navigationStateManager,
        featureActions = featureActions
    )
}

@Composable
private fun initializeNavigationViewModels(): NavigationViewModels {
    return NavigationViewModels(
        authViewModel = hiltViewModel(),
        profileViewModel = hiltViewModel(),
        mainViewModel = hiltViewModel(),
        chatViewModel = hiltViewModel(),
        copyViewModel = hiltViewModel(),
        fieldsViewModel = hiltViewModel(),
        filterViewModel = hiltViewModel(),
        inviteViewModel = hiltViewModel(),
        membersManagerViewModel = hiltViewModel(),
        membersViewModel = hiltViewModel(),
        noteViewModel = hiltViewModel(),
        noteCreationViewModel = hiltViewModel(),
        noteEditViewModel = hiltViewModel(),
        sharingViewModel = hiltViewModel(),
        templateViewModel = hiltViewModel(),
        wsCreationViewModel = hiltViewModel(),
        wsProfileManagerViewModel = hiltViewModel(),
        wsProfileViewModel = hiltViewModel(),
        wsSelectViewModel = hiltViewModel()
    )
}

private data class NavigationViewModels(
    val authViewModel: AuthViewModel,
    val profileViewModel: ProfileViewModel,
    val mainViewModel: MainViewModel,
    val chatViewModel: ChatViewModel,
    val copyViewModel: CopyViewModel,
    val fieldsViewModel: FieldsViewModel,
    val filterViewModel: FilterViewModel,
    val inviteViewModel: InviteViewModel,
    val membersManagerViewModel: MembersManagerViewModel,
    val membersViewModel: MembersViewModel,
    val noteViewModel: NoteViewModel,
    val noteCreationViewModel: NoteCreationViewModel,
    val noteEditViewModel: NoteEditViewModel,
    val sharingViewModel: SharingViewModel,
    val templateViewModel: TemplateViewModel,
    val wsCreationViewModel: WsCreationViewModel,
    val wsProfileManagerViewModel: WsProfileManagerViewModel,
    val wsProfileViewModel: WsProfileViewModel,
    val wsSelectViewModel: WsSelectViewModel
)

private fun handleNavigationEvent(
    navigationEvent: NavigationEvent,
    navController: NavHostController,
    navigationStateManager: NavigationStateManager,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    chatViewModel: ChatViewModel,
    copyViewModel: CopyViewModel,
    fieldsViewModel: FieldsViewModel,
    filterViewModel: FilterViewModel,
    inviteViewModel: InviteViewModel,
    membersManagerViewModel: MembersManagerViewModel,
    membersViewModel: MembersViewModel,
    noteViewModel: NoteViewModel,
    noteCreationViewModel: NoteCreationViewModel,
    noteEditViewModel: NoteEditViewModel,
    sharingViewModel: SharingViewModel,
    templateViewModel: TemplateViewModel,
    wsCreationViewModel: WsCreationViewModel,
    wsProfileManagerViewModel: WsProfileManagerViewModel,
    wsProfileViewModel: WsProfileViewModel,
    wsSelectViewModel: WsSelectViewModel,
    mainViewModel: MainViewModel
) {
    val basicViewModels = BasicNavigationViewModels(
        membersManagerViewModel,
        wsSelectViewModel,
        wsProfileViewModel,
        mainViewModel,
        templateViewModel
    )
    
    val featureViewModels = FeatureNavigationViewModels(
        filterViewModel,
        membersManagerViewModel,
        membersViewModel,
        noteEditViewModel,
        profileViewModel,
        templateViewModel
    )

    when (navigationEvent) {
        is NavigationEvent.NavigateToAuth,
        is NavigationEvent.NavigateToAuthWithMessage -> {
            handleAuthNavigation(navigationEvent, navController, authViewModel, navigationStateManager)
        }
        is NavigationEvent.NavigateToMain,
        is NavigationEvent.NavigateToMainWithMessage,
        is NavigationEvent.NavigateToMainWithContext,
        is NavigationEvent.NavigateToMainTagReset -> {
            handleMainNavigation(navigationEvent, navController, mainViewModel, navigationStateManager)
        }
        is NavigationEvent.NavigateToProfile,
        is NavigationEvent.NavigateToManageProfile,
        is NavigationEvent.NavigateToProfileCompletion,
        is NavigationEvent.NavigateToOtherProfile -> {
            handleProfileNavigation(navigationEvent, navController, profileViewModel, navigationStateManager)
        }
        is NavigationEvent.NavigateBack,
        is NavigationEvent.ClearBackStack,
        is NavigationEvent.NoNavigation -> {
            handleBasicNavigation(navigationEvent, navController, basicViewModels, navigationStateManager)
        }
        is NavigationEvent.NavigateToChat,
        is NavigationEvent.NavigateToChatTagReset,
        is NavigationEvent.NavigateToCopy,
        is NavigationEvent.NavigateToFields,
        is NavigationEvent.NavigateToFilter,
        is NavigationEvent.NavigateToInvite,
        is NavigationEvent.NavigateToMembersManager,
        is NavigationEvent.NavigateToMembers,
        is NavigationEvent.NavigateToNote,
        is NavigationEvent.NavigateToNoteCreation,
        is NavigationEvent.NavigateToNoteEdit,
        is NavigationEvent.NavigateToSharing,
        is NavigationEvent.NavigateToTemplate,
        is NavigationEvent.NavigateToTemplateTagReset -> {
            handleFeatureNavigation(navigationEvent, navController, featureViewModels, navigationStateManager)
        }
        is NavigationEvent.NavigateToWsCreation,
        is NavigationEvent.NavigateToWsProfileManager,
        is NavigationEvent.NavigateToWsProfile,
        is NavigationEvent.NavigateToWsSelect -> {
            handleWorkspaceNavigation(navigationEvent, navController, wsProfileViewModel, wsSelectViewModel, navigationStateManager)
        }
    }
}

private fun handleAuthNavigation(
    event: NavigationEvent,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    navigationStateManager: NavigationStateManager
) {
    if (event is NavigationEvent.NavigateToAuthWithMessage) {
        authViewModel.setSuccessMessage(event.message)
    }
    navController.navigate(NavRoutes.AUTH) {
        popUpTo(0) { inclusive = true }
    }
    navigationStateManager.clearNavigationEvent()
}

private fun handleMainNavigation(
    event: NavigationEvent,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    navigationStateManager: NavigationStateManager
) {
    when (event) {
        is NavigationEvent.NavigateToMainWithMessage -> {
            mainViewModel.setSuccessMessage(event.message)
        }
        else -> {}
    }
    
    navController.navigate(NavRoutes.MAIN) {
        popUpTo(0) { inclusive = true }
    }
    
    when (event) {
        is NavigationEvent.NavigateToMainWithContext -> mainViewModel.onLoad()
        is NavigationEvent.NavigateToMainTagReset -> mainViewModel.onLoadTagReset()
        else -> mainViewModel.onLoadTagReset()
    }
    
    navigationStateManager.clearNavigationEvent()
}

private fun handleProfileNavigation(
    event: NavigationEvent,
    navController: NavHostController,
    profileViewModel: ProfileViewModel,
    navigationStateManager: NavigationStateManager
) {
    when (event) {
        is NavigationEvent.NavigateToProfile -> {
            Log.d("navigation", "navigationToProfileRegistered")
            navController.navigate(NavRoutes.PROFILE)
        }
        is NavigationEvent.NavigateToManageProfile -> {
            navController.navigate(NavRoutes.MANAGE_PROFILE)
        }
        is NavigationEvent.NavigateToProfileCompletion -> {
            navController.navigate(NavRoutes.PROFILE_COMPLETION) {
                popUpTo(0) { inclusive = true }
            }
        }
        is NavigationEvent.NavigateToOtherProfile -> {
            navController.navigate(NavRoutes.OTHER_PROFILE)
            profileViewModel.loadProfile(navigationStateManager.state.getOtherUserId())
        }
        else -> {}
    }
    navigationStateManager.clearNavigationEvent()
}

private data class BasicNavigationViewModels(
    val membersManagerViewModel: MembersManagerViewModel,
    val wsSelectViewModel: WsSelectViewModel,
    val wsProfileViewModel: WsProfileViewModel,
    val mainViewModel: MainViewModel,
    val templateViewModel: TemplateViewModel
)

private fun handleBasicNavigation(
    event: NavigationEvent,
    navController: NavHostController,
    viewModels: BasicNavigationViewModels,
    navigationStateManager: NavigationStateManager
) {
    when (event) {
        is NavigationEvent.NavigateBack -> {
            navController.popBackStack()
            viewModels.membersManagerViewModel.loadUsers()
            viewModels.wsSelectViewModel.setToUpdate()
            viewModels.wsProfileViewModel.loadProfile()
            viewModels.mainViewModel.onLoad()
            viewModels.templateViewModel.onLoad()
            navigationStateManager.clearNavigationEvent()
        }
        is NavigationEvent.ClearBackStack -> {
            navController.popBackStack(navController.graph.startDestinationId, false)
            navigationStateManager.clearNavigationEvent()
        }
        is NavigationEvent.NoNavigation -> {
            // Do nothing
        }
        else -> {}
    }
}

private data class FeatureNavigationViewModels(
    val filterViewModel: FilterViewModel,
    val membersManagerViewModel: MembersManagerViewModel,
    val membersViewModel: MembersViewModel,
    val noteEditViewModel: NoteEditViewModel,
    val profileViewModel: ProfileViewModel,
    val templateViewModel: TemplateViewModel
)

private fun handleFeatureNavigation(
    event: NavigationEvent,
    navController: NavHostController,
    viewModels: FeatureNavigationViewModels,
    navigationStateManager: NavigationStateManager
) {
    when (event) {
        is NavigationEvent.NavigateToChat,
        is NavigationEvent.NavigateToChatTagReset -> {
            navController.navigate(NavRoutes.CHAT)
        }
        is NavigationEvent.NavigateToCopy -> {
            navController.navigate(NavRoutes.COPY)
        }
        is NavigationEvent.NavigateToFields -> {
            navController.navigate(NavRoutes.FIELDS)
        }
        is NavigationEvent.NavigateToFilter -> {
            navController.navigate(NavRoutes.FILTER)
            viewModels.filterViewModel.onLoad()
        }
        is NavigationEvent.NavigateToInvite -> {
            navController.navigate(NavRoutes.INVITE)
        }
        is NavigationEvent.NavigateToMembersManager -> {
            navController.navigate(NavRoutes.MEMBERS_MANAGER)
            viewModels.membersManagerViewModel.loadUsers()
        }
        is NavigationEvent.NavigateToMembers -> {
            navController.navigate(NavRoutes.MEMBERS)
            viewModels.membersViewModel.loadUsers()
        }
        is NavigationEvent.NavigateToNote -> {
            navController.navigate(NavRoutes.NOTE)
        }
        is NavigationEvent.NavigateToNoteCreation -> {
            navController.navigate(NavRoutes.NOTE_CREATION)
        }
        is NavigationEvent.NavigateToNoteEdit -> {
            navController.navigate(NavRoutes.NOTE_EDIT)
            viewModels.noteEditViewModel.loadWorkspaces()
        }
        is NavigationEvent.NavigateToSharing -> {
            navController.navigate(NavRoutes.SHARING)
        }
        is NavigationEvent.NavigateToTemplate -> {
            navController.navigate(NavRoutes.TEMPLATE) {
                popUpTo(0) { inclusive = true }
            }
            viewModels.templateViewModel.onLoad()
        }
        is NavigationEvent.NavigateToTemplateTagReset -> {
            navController.navigate(NavRoutes.TEMPLATE) {
                popUpTo(0) { inclusive = true }
            }
            viewModels.templateViewModel.onLoadTagReset()
        }
        else -> {}
    }
    navigationStateManager.clearNavigationEvent()
}

private fun handleWorkspaceNavigation(
    event: NavigationEvent,
    navController: NavHostController,
    wsProfileViewModel: WsProfileViewModel,
    wsSelectViewModel: WsSelectViewModel,
    navigationStateManager: NavigationStateManager
) {
    when (event) {
        is NavigationEvent.NavigateToWsCreation -> {
            navController.navigate(NavRoutes.WS_CREATION)
        }
        is NavigationEvent.NavigateToWsProfileManager -> {
            navController.navigate(NavRoutes.WS_PROFILE_MANAGER)
        }
        is NavigationEvent.NavigateToWsProfile -> {
            navController.navigate(NavRoutes.WS_PROFILE)
            wsProfileViewModel.loadProfile()
        }
        is NavigationEvent.NavigateToWsSelect -> {
            navController.navigate(NavRoutes.WS_SELECT)
            wsSelectViewModel.setToUpdate()
        }
        else -> {}
    }
    navigationStateManager.clearNavigationEvent()
}


class StateWrapper(private val navigationStateManager: NavigationStateManager) :
    IStateWrapper {

    // --- Getters delegation ---
    override fun getWorkspaceId(): String {
        return navigationStateManager.state.getWorkspaceId()
    }

    override fun getOtherUserId(): String {
        return navigationStateManager.state.getOtherUserId()
    }

    override fun getNoteType(): NoteType {
        return navigationStateManager.state.getNoteType()
    }

    override fun getNoteId(): String {
        return navigationStateManager.state.getNoteId()
    }

    override fun getSelectedTags(): List<String> {
        return navigationStateManager.state.getSelectedTags()
    }

    override fun getAllTagsSelected(): Boolean {
        return navigationStateManager.state.getAllTagsSelected()
    }

    override fun getSearchQuery(): String {
        return navigationStateManager.state.getSearchQuery()
    }

    override fun setSearchQuery(query: String) {
        navigationStateManager.state.setSearchQuery(query)
    }

    override fun updateTagSelection(selectedTags: List<String>, allTagsSelected: Boolean) {
        navigationStateManager.state.updateTagSelection(
            selectedTags = selectedTags,
            allTagsSelected = allTagsSelected
        )
    }
}

class WorkspaceActions(private val navigationStateManager: NavigationStateManager): IWorkspaceActions {
    override fun navigateToInvite() {
        navigationStateManager.ws.navigateToInvite()
    }

    override fun navigateToMembersManager() {
        navigationStateManager.ws.navigateToMembersManager()
    }

    override fun navigateToMembers() {
        navigationStateManager.ws.navigateToMembers()
    }

    override fun navigateToWsCreation() {
        navigationStateManager.ws.navigateToWsCreation()
    }

    override fun navigateToWsProfileManager(workspaceId: String) {
        navigationStateManager.ws.navigateToWsProfileManager(workspaceId)
    }

    override fun navigateToWsProfile(workspaceId: String) {
        navigationStateManager.ws.navigateToWsProfile(workspaceId)
    }

    override fun navigateToWsSelect() {
        navigationStateManager.ws.navigateToWsSelect()
    }
}

class NavigationActions(private val navigationStateManager: NavigationStateManager) :
    INavigationActions {
    // --- Navigation delegation ---
    override fun navigateToChat(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean,
        searchQuery: String
    ) {
        navigationStateManager.display.navigateToChat(
            workspaceId,
            selectedTags,
            allTagsSelected,
            searchQuery
        )
    }

    override fun navigateToCopy() {
        navigationStateManager.note.navigateToCopy()
    }

    override fun navigateToFields() {
        navigationStateManager.note.navigateToFields()
    }

    override fun navigateToFilter(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean
    ) {
        navigationStateManager.display.navigateToFilter(
            workspaceId,
            selectedTags,
            allTagsSelected
        )
    }

    override fun navigateToNote(noteId: String) {
        navigationStateManager.note.navigateToNote(noteId)
    }

    override fun navigateToNoteCreation() {
        navigationStateManager.note.navigateToNoteCreation()
    }

    override fun navigateToNoteEdit(noteId: String) {
        navigationStateManager.note.navigateToNoteEdit(noteId)
    }

    override fun navigateToOtherProfile(otherUserId: String) {
        navigationStateManager.navigateToOtherProfile(otherUserId)
    }

    override fun navigateToSharing() {
        navigationStateManager.note.navigateToSharing()
    }

    override fun navigateToTemplate(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean,
        searchQuery: String
    ) {
        navigationStateManager.display.navigateToTemplate(
            workspaceId,
            selectedTags,
            allTagsSelected,
            searchQuery
        )
    }

    override fun navigateToMainWithContext(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean,
        searchQuery: String
    ) {
        navigationStateManager.display.navigateToMainWithContext(
            workspaceId,
            selectedTags,
            allTagsSelected,
            searchQuery
        )
    }

    override fun navigateToChatTagReset(workspaceId: String) {
        navigationStateManager.display.navigateToChatTagReset(workspaceId)
    }

    override fun navigateToTemplateTagReset(workspaceId: String) {
        navigationStateManager.display.navigateToTemplateTagReset(workspaceId)
    }

    override fun navigateToMainTagReset(workspaceId: String) {
        navigationStateManager.display.navigateToMainTagReset(workspaceId)
    }

}


@Composable
private fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    mainViewModel: MainViewModel,
    chatViewModel: ChatViewModel,
    copyViewModel: CopyViewModel,
    fieldsViewModel: FieldsViewModel,
    filterViewModel: FilterViewModel,
    inviteViewModel: InviteViewModel,
    membersManagerViewModel: MembersManagerViewModel,
    membersViewModel: MembersViewModel,
    noteViewModel: NoteViewModel,
    noteCreationViewModel: NoteCreationViewModel,
    noteEditViewModel: NoteEditViewModel,
    sharingViewModel: SharingViewModel,
    templateViewModel: TemplateViewModel,
    wsCreationViewModel: WsCreationViewModel,
    wsProfileManagerViewModel: WsProfileManagerViewModel,
    wsProfileViewModel: WsProfileViewModel,
    wsSelectViewModel: WsSelectViewModel,
    navigationStateManager: NavigationStateManager,
    featureActions: FeatureActions
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOADING
    ) {
        addAuthRoutes(authViewModel, profileViewModel, navigationStateManager)
        addMainRoutes(mainViewModel, templateViewModel, navigationStateManager, featureActions)
        addProfileRoutes(authViewModel, profileViewModel, navigationStateManager)
        addWorkspaceRoutes(wsSelectViewModel, navigationStateManager, featureActions)
        addFeatureRoutes(
            chatViewModel, filterViewModel, noteViewModel, noteCreationViewModel,
            noteEditViewModel, navigationStateManager, featureActions
        )
        addWorkspaceManagementRoutes(
            WorkspaceManagementViewModels(
                wsProfileViewModel,
                inviteViewModel,
                membersViewModel,
                membersManagerViewModel,
                wsProfileManagerViewModel,
                wsCreationViewModel
            ),
            navigationStateManager,
            featureActions
        )
    }
}

private fun NavGraphBuilder.addAuthRoutes(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    navigationStateManager: NavigationStateManager
) {
    composable(NavRoutes.LOADING) {
        LoadingScreen(message = stringResource(R.string.checking_authentication))
    }

    composable(NavRoutes.AUTH) {
        AuthScreen(authViewModel = authViewModel, profileViewModel = profileViewModel)
    }

    composable(NavRoutes.PROFILE_COMPLETION) {
        ProfileCompletionScreen(
            profileViewModel = profileViewModel,
            onProfileCompleted = { navigationStateManager.handleProfileCompletion() },
            onProfileCompletedWithMessage = { message ->
                Log.d("AppNavigation", "Profile completed with message: $message")
                navigationStateManager.handleProfileCompletionWithMessage(message)
            }
        )
    }
}

private fun NavGraphBuilder.addMainRoutes(
    mainViewModel: MainViewModel,
    templateViewModel: TemplateViewModel,
    navigationStateManager: NavigationStateManager,
    featureActions: FeatureActions
) {
    composable(NavRoutes.MAIN) {
        MainScreen(
            mainViewModel = mainViewModel,
            onProfileClick = { navigationStateManager.navigateToProfile() },
            featureActions = featureActions
        )
    }

    composable(NavRoutes.TEMPLATE) {
        TemplateScreen(
            templateViewModel = templateViewModel,
            onProfileClick = { navigationStateManager.navigateToProfile() },
            featureActions = featureActions
        )
    }
}

private fun NavGraphBuilder.addProfileRoutes(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    navigationStateManager: NavigationStateManager
) {
    composable(NavRoutes.PROFILE) {
        Log.d("navigation", "Navigating to profile")
        ProfileScreen(
            authViewModel = authViewModel,
            profileViewModel = profileViewModel,
            actions = ProfileScreenActions(
                onBackClick = { navigationStateManager.navigateBack() },
                onManageProfileClick = { navigationStateManager.navigateToManageProfile() },
                onAccountDeleted = { navigationStateManager.handleAccountDeletion() },
                onSignOut = { navigationStateManager.handleSignOut() }
            )
        )
    }

    composable(NavRoutes.MANAGE_PROFILE) {
        ManageProfileScreen(
            profileViewModel = profileViewModel,
            onBackClick = { navigationStateManager.navigateBack() }
        )
    }

    composable(NavRoutes.OTHER_PROFILE) {
        Log.d("navigation", "navigating to other profile instead")
        OtherProfileScreen(
            profileViewModel = profileViewModel,
            onBackClick = { navigationStateManager.navigateBack() }
        )
    }
}

private fun NavGraphBuilder.addWorkspaceRoutes(
    wsSelectViewModel: WsSelectViewModel,
    navigationStateManager: NavigationStateManager,
    featureActions: FeatureActions
) {
    composable(NavRoutes.WS_SELECT) {
        WorkspacesScreen(
            workspacesViewModel = wsSelectViewModel,
            onBackClick = {
                when (navigationStateManager.state.getNoteType()) {
                    NoteType.CHAT -> {
                        navigationStateManager.display.navigateToChatTagReset(
                            navigationStateManager.state.getWorkspaceId()
                        )
                    }
                    NoteType.CONTENT -> {
                        navigationStateManager.display.navigateToMainTagReset(
                            navigationStateManager.state.getWorkspaceId()
                        )
                    }
                    NoteType.TEMPLATE -> {
                        navigationStateManager.display.navigateToTemplateTagReset(
                            navigationStateManager.state.getWorkspaceId()
                        )
                    }
                }
            },
            featureActions = featureActions,
            onPersonalProfileClick = { navigationStateManager.navigateToProfile() }
        )
    }
}

private fun NavGraphBuilder.addFeatureRoutes(
    chatViewModel: ChatViewModel,
    filterViewModel: FilterViewModel,
    noteViewModel: NoteViewModel,
    noteCreationViewModel: NoteCreationViewModel,
    noteEditViewModel: NoteEditViewModel,
    navigationStateManager: NavigationStateManager,
    featureActions: FeatureActions
) {
    composable(NavRoutes.CHAT) {
        ChatScreen(
            chatViewModel = chatViewModel,
            onProfileClick = { navigationStateManager.navigateToProfile() },
            onBackClick = { navigationStateManager.navigateBack() },
            featureActions = featureActions
        )
    }

    composable(NavRoutes.FILTER) {
        FilterScreen(
            filterViewModel = filterViewModel,
            featureActions = featureActions,
            onBackClick = { navigationStateManager.navigateBack() }
        )
    }

    composable(NavRoutes.NOTE) {
        NoteScreen(
            noteViewModel = noteViewModel,
            featureActions = featureActions,
            onBackClick = { navigationStateManager.navigateBack() }
        )
    }

    composable(NavRoutes.NOTE_CREATION) {
        LaunchedEffect(Unit) {
            noteCreationViewModel.reset()
        }
        NoteCreationScreen(
            noteCreationViewModel = noteCreationViewModel,
            onBackClick = { navigationStateManager.navigateBack() },
            featureActions = featureActions
        )
    }

    composable(NavRoutes.NOTE_EDIT) {
        LaunchedEffect(Unit) {
            noteEditViewModel.reset()
        }
        NoteEditScreen(
            noteEditViewModel = noteEditViewModel,
            onBackClick = { navigationStateManager.navigateBack() },
            featureActions = featureActions
        )
    }
}

private data class WorkspaceManagementViewModels(
    val wsProfileViewModel: WsProfileViewModel,
    val inviteViewModel: InviteViewModel,
    val membersViewModel: MembersViewModel,
    val membersManagerViewModel: MembersManagerViewModel,
    val wsProfileManagerViewModel: WsProfileManagerViewModel,
    val wsCreationViewModel: WsCreationViewModel
)

private fun NavGraphBuilder.addWorkspaceManagementRoutes(
    viewModels: WorkspaceManagementViewModels,
    navigationStateManager: NavigationStateManager,
    featureActions: FeatureActions
) {
    composable(NavRoutes.WS_PROFILE) {
        WsProfileScreen(
            viewModels.wsProfileViewModel,
            onBackClick = { navigationStateManager.navigateBack() },
            featureActions = featureActions
        )
    }

    composable(NavRoutes.INVITE) {
        WsInviteScreen(
            wsInviteViewModel = viewModels.inviteViewModel,
            featureActions = featureActions,
            onBackClick = { navigationStateManager.navigateBack() }
        )
    }

    composable(NavRoutes.MEMBERS) {
        WsMembersScreen(
            membersViewModel = viewModels.membersViewModel,
            featureActions = featureActions,
            onBackClick = { navigationStateManager.navigateBack() },
            onPersonalProfileClick = { navigationStateManager.navigateToProfile() }
        )
    }

    composable(NavRoutes.WS_PROFILE_MANAGER) {
        WsProfileManagerScreen(
            wsProfileManagerViewModel = viewModels.wsProfileManagerViewModel,
            featureActions = featureActions
        )
    }

    composable(NavRoutes.MEMBERS_MANAGER) {
        WsMembersManagerScreen(
            membersManagerViewModel = viewModels.membersManagerViewModel,
            onBackClick = { navigationStateManager.navigateBack() },
            onPersonalProfileClick = { navigationStateManager.navigateToProfile() },
            featureActions = featureActions
        )
    }

    composable(NavRoutes.WS_CREATION) {
        CreateWorkspaceScreen(
            wsCreationViewModel = viewModels.wsCreationViewModel,
            onBackClick = { navigationStateManager.navigateBack() },
            featureActions = featureActions
        )
    }
}