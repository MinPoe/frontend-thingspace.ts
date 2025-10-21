package com.cpen321.usermanagement.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.screens.AuthScreen
import com.cpen321.usermanagement.ui.screens.ChatScreen
import com.cpen321.usermanagement.ui.screens.LoadingScreen
import com.cpen321.usermanagement.ui.screens.MainScreen
//import com.cpen321.usermanagement.ui.screens.ManageHobbiesScreen
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
import com.cpen321.usermanagement.utils.IFeatureActions
import kotlinx.coroutines.runBlocking

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
    val featureActions = FeatureActions(navigationStateManager)

    // Initialize view models required for navigation-level scope
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val copyViewModel: CopyViewModel = hiltViewModel()
    val fieldsViewModel: FieldsViewModel = hiltViewModel()
    val filterViewModel: FilterViewModel = hiltViewModel()
    val inviteViewModel: InviteViewModel = hiltViewModel()
    val membersManagerViewModel: MembersManagerViewModel = hiltViewModel()
    val membersViewModel: MembersViewModel = hiltViewModel()
    val noteViewModel: NoteViewModel = hiltViewModel()
    val sharingViewModel: SharingViewModel = hiltViewModel()
    val templateViewModel: TemplateViewModel = hiltViewModel()
    val wsCreationViewModel: WsCreationViewModel = hiltViewModel()
    val wsProfileManagerViewModel: WsProfileManagerViewModel = hiltViewModel()
    val wsProfileViewModel: WsProfileViewModel = hiltViewModel()
    val wsSelectViewModel: WsSelectViewModel = hiltViewModel()

    // Handle navigation events from NavigationStateManager
    LaunchedEffect(navigationEvent) {
        handleNavigationEvent(
            navigationEvent,
            navController,
            navigationStateManager,
            authViewModel,
            chatViewModel = chatViewModel,
            copyViewModel = copyViewModel,
            fieldsViewModel = fieldsViewModel,
            filterViewModel = filterViewModel,
            inviteViewModel = inviteViewModel,
            membersManagerViewModel = membersManagerViewModel,
            membersViewModel = membersViewModel,
            noteViewModel = noteViewModel,
            sharingViewModel = sharingViewModel,
            templateViewModel = templateViewModel,
            wsCreationViewModel = wsCreationViewModel,
            wsProfileManagerViewModel = wsProfileManagerViewModel,
            wsProfileViewModel = wsProfileViewModel,
            wsSelectViewModel = wsSelectViewModel,
            mainViewModel = mainViewModel
        )
    }

    AppNavHost(
        navController = navController,
        authViewModel = authViewModel,
        profileViewModel = profileViewModel,
        mainViewModel = mainViewModel,
        chatViewModel = chatViewModel,
        copyViewModel = copyViewModel,
        fieldsViewModel = fieldsViewModel,
        filterViewModel = filterViewModel,
        inviteViewModel = inviteViewModel,
        membersManagerViewModel = membersManagerViewModel,
        membersViewModel = membersViewModel,
        noteViewModel = noteViewModel,
        sharingViewModel = sharingViewModel,
        templateViewModel = templateViewModel,
        wsCreationViewModel = wsCreationViewModel,
        wsProfileManagerViewModel = wsProfileManagerViewModel,
        wsProfileViewModel = wsProfileViewModel,
        wsSelectViewModel = wsSelectViewModel,
        navigationStateManager = navigationStateManager,
        featureActions = featureActions
    )
}

private fun handleNavigationEvent(
    navigationEvent: NavigationEvent,
    navController: NavHostController,
    navigationStateManager: NavigationStateManager,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    copyViewModel: CopyViewModel,
    fieldsViewModel: FieldsViewModel,
    filterViewModel: FilterViewModel,
    inviteViewModel: InviteViewModel,
    membersManagerViewModel: MembersManagerViewModel,
    membersViewModel: MembersViewModel,
    noteViewModel: NoteViewModel,
    sharingViewModel: SharingViewModel,
    templateViewModel: TemplateViewModel,
    wsCreationViewModel: WsCreationViewModel,
    wsProfileManagerViewModel: WsProfileManagerViewModel,
    wsProfileViewModel: WsProfileViewModel,
    wsSelectViewModel: WsSelectViewModel,
    mainViewModel: MainViewModel
) {
    when (navigationEvent) {
        is NavigationEvent.NavigateToAuth -> {
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToAuthWithMessage -> {
            authViewModel.setSuccessMessage(navigationEvent.message)
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMain -> {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
            runBlocking { mainViewModel.loadAllUserTags() }
        }

        is NavigationEvent.NavigateToMainWithMessage -> {
            mainViewModel.setSuccessMessage(navigationEvent.message) //NOTE: this is how to access context
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
            runBlocking { mainViewModel.loadAllUserTags() }
        }

        is NavigationEvent.NavigateToProfileCompletion -> {
            navController.navigate(NavRoutes.PROFILE_COMPLETION) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToProfile -> {
            Log.d("navigation", "navigationToProfileRegistered")
            navController.navigate(NavRoutes.PROFILE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToManageProfile -> {
            navController.navigate(NavRoutes.MANAGE_PROFILE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateBack -> {
            navController.popBackStack()
            navigationStateManager.clearNavigationEvent()
        }

        //feature screens navigation starts here
        is NavigationEvent.ClearBackStack -> {
            navController.popBackStack(navController.graph.startDestinationId, false)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NoNavigation -> {
            // Do nothing
        }

        //when cases for the feature events
        is NavigationEvent.NavigateToChat -> {
            navController.navigate(NavRoutes.CHAT) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToCopy -> {
            navController.navigate(NavRoutes.COPY)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToFields -> {
            navController.navigate(NavRoutes.FIELDS)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToFilter -> {
            navController.navigate(NavRoutes.FILTER)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToInvite -> {
            navController.navigate(NavRoutes.INVITE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMembersManager -> {
            navController.navigate(NavRoutes.MEMBERS_MANAGER)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMembers -> {
            navController.navigate(NavRoutes.MEMBERS)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToNote -> {
            navController.navigate(NavRoutes.NOTE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToOtherProfile -> {
            navController.navigate(NavRoutes.OTHER_PROFILE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToSharing -> {
            navController.navigate(NavRoutes.SHARING)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToTemplate -> {
            navController.navigate(NavRoutes.TEMPLATE) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsCreation -> {
            navController.navigate(NavRoutes.WS_CREATION)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsProfileManager -> {
            navController.navigate(NavRoutes.WS_PROFILE_MANAGER)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsProfile -> {
            navController.navigate(NavRoutes.WS_PROFILE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsSelect -> {
            navController.navigate(NavRoutes.WS_SELECT)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMainWithContext -> {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMainTagReset -> {
            navController.navigate(NavRoutes.MAIN){
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
            runBlocking { mainViewModel.loadAllUserTags() }
        }

        is NavigationEvent.NavigateToChatTagReset -> {
            navController.navigate(NavRoutes.CHAT){
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
            runBlocking { chatViewModel.loadAllUserTags() }
        }

        is NavigationEvent.NavigateToTemplateTagReset -> {
            navController.navigate(NavRoutes.TEMPLATE){
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
            runBlocking { chatViewModel.loadAllUserTags() }
        }
    }
}


class FeatureActions(private val navigationStateManager: NavigationStateManager) :
    IFeatureActions {

    // --- Getters delegation ---
    override fun getWorkspaceId(): String {
        return navigationStateManager.getWorkspaceId()
    }

    override fun getOtherUserId(): String {
        return navigationStateManager.getOtherUserId()
    }

    override fun getNoteType(): NoteType {
        return navigationStateManager.getNoteType()
    }

    override fun getNoteId(): String {
        return navigationStateManager.getNoteId()
    }

    override fun getSelectedTags(): List<String> {
        return navigationStateManager.getSelectedTags()
    }

    override fun getAllTagsSelected(): Boolean {
        return navigationStateManager.getAllTagsSelected()
    }

    override fun getSearchQuery(): String {
        return navigationStateManager.getSearchQuery()
    }

    override fun setSearchQuery(query: String){
        navigationStateManager.setSearchQuery(query)
    }

    override fun updateTagSelection(selectedTags: List<String>, allTagsSelected: Boolean) {
        navigationStateManager.updateTagSelection(selectedTags = selectedTags,
            allTagsSelected = allTagsSelected)
    }

    // --- Navigation delegation ---
    override fun navigateToChat(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean,
        searchQuery: String
    ) {
        navigationStateManager.navigateToChat(
            workspaceId,
            selectedTags,
            allTagsSelected,
            searchQuery
        )
    }

    override fun navigateToCopy() {
        navigationStateManager.navigateToCopy()
    }

    override fun navigateToFields() {
        navigationStateManager.navigateToFields()
    }

    override fun navigateToFilter(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean
    ) {
        navigationStateManager.navigateToFilter(
            workspaceId,
            selectedTags,
            allTagsSelected
        )
    }

    override fun navigateToInvite() {
        navigationStateManager.navigateToInvite()
    }

    override fun navigateToMembersManager() {
        navigationStateManager.navigateToMembersManager()
    }

    override fun navigateToMembers() {
        navigationStateManager.navigateToMembers()
    }

    override fun navigateToNote(noteId: String) {
        navigationStateManager.navigateToNote(noteId)
    }

    override fun navigateToOtherProfile(otherUserId: String) {
        navigationStateManager.navigateToOtherProfile(otherUserId)
    }

    override fun navigateToSharing() {
        navigationStateManager.navigateToSharing()
    }

    override fun navigateToTemplate(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean,
        searchQuery: String
    ) {
        navigationStateManager.navigateToTemplate(
            workspaceId,
            selectedTags,
            allTagsSelected,
            searchQuery
        )
    }

    override fun navigateToWsCreation() {
        navigationStateManager.navigateToWsCreation()
    }

    override fun navigateToWsProfileManager(workspaceId: String) {
        navigationStateManager.navigateToWsProfileManager(workspaceId)
    }

    override fun navigateToWsProfile(workspaceId: String) {
        navigationStateManager.navigateToWsProfile(workspaceId)
    }

    override fun navigateToMainWithContext(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean,
        searchQuery: String
    ) {
        navigationStateManager.navigateToMainWithContext(
            workspaceId,
            selectedTags,
            allTagsSelected,
            searchQuery
        )
    }

    override fun navigateToWsSelect() {
        navigationStateManager.navigateToWsSelect()
    }

    override fun navigateToChatTagReset(workspaceId: String) {
        navigationStateManager.navigateToChatTagReset(workspaceId)
    }

    override fun navigateToTemplateTagReset(workspaceId: String) {
        navigationStateManager.navigateToTemplateTagReset(workspaceId)
    }

    override fun navigateToMainTagReset(workspaceId: String) {
        navigationStateManager.navigateToMainTagReset(workspaceId)
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

        composable(NavRoutes.MAIN) {
            MainScreen(
                mainViewModel = mainViewModel,
                onProfileClick = { navigationStateManager.navigateToProfile() },
                //TODO: change 'personal' to user id once we have access to
                featureActions = featureActions
            )
        }

        composable(NavRoutes.TEMPLATE){
            TemplateScreen(
                templateViewModel = templateViewModel,
                onProfileClick = { navigationStateManager.navigateToProfile() },
                //TODO: change 'personal' to user id once we have access to
                featureActions = featureActions
            )
        }

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

        composable(NavRoutes.WS_SELECT) {
            WorkspacesScreen(
                workspacesViewModel = wsSelectViewModel,
                onBackClick = {navigationStateManager.navigateBack()},
                featureActions = featureActions,
                onPersonalProfileClick = { navigationStateManager.navigateToProfile() })
        }

        composable(NavRoutes.CHAT){
            ChatScreen(
                chatViewModel = chatViewModel,
                onProfileClick = { navigationStateManager.navigateToProfile() },
                //TODO: change 'personal' to user id once we have access to
                featureActions = featureActions
            )
        }

        composable(NavRoutes.FILTER){
            FilterScreen(
                filterViewModel = filterViewModel,
                //TODO: change 'personal' to user id once we have access to
                featureActions = featureActions,
                onBackClick = { navigationStateManager.navigateBack() }
            )
        }

        composable (NavRoutes.NOTE){
            NoteScreen(
                noteViewModel = noteViewModel,
                featureActions = featureActions,
                onBackClick = { navigationStateManager.navigateBack() }
            )
        }

        composable ( NavRoutes.OTHER_PROFILE ){
            Log.d("navigation", "navigating to other profile instead")
            OtherProfileScreen(
                profileViewModel = profileViewModel,
                onBackClick = { navigationStateManager.navigateBack() },
                otherProfileId = navigationStateManager.getOtherUserId()
            )
        }

        composable (route = NavRoutes.WS_PROFILE ){
            WsProfileScreen(wsProfileViewModel,
                onBackClick = { navigationStateManager.navigateBack() },
                featureActions = featureActions)
        }

        composable(route = NavRoutes.INVITE) {
            WsInviteScreen(
                wsInviteViewModel = inviteViewModel,
                featureActions = featureActions,
                onBackClick = { navigationStateManager.navigateBack() })
        }

        composable(route = NavRoutes.MEMBERS){
            WsMembersScreen(
                membersViewModel = membersViewModel,
                featureActions = featureActions,
                onBackClick = { navigationStateManager.navigateBack() },
                onPersonalProfileClick = { navigationStateManager.navigateToProfile() }
            )
        }

        composable(route = NavRoutes.WS_PROFILE_MANAGER){
            WsProfileManagerScreen(
                wsProfileManagerViewModel = wsProfileManagerViewModel,
                featureActions = featureActions
            )
        }

        composable (route = NavRoutes.MEMBERS_MANAGER){
            WsMembersManagerScreen(
                membersManagerViewModel = membersManagerViewModel,
                onBackClick = { navigationStateManager.navigateBack() },
                onPersonalProfileClick = { navigationStateManager.navigateToProfile() },
                featureActions = featureActions
            )
        }
        composable(NavRoutes.WS_CREATION){
            CreateWorkspaceScreen(
                wsCreationViewModel = wsCreationViewModel,
                onBackClick = { navigationStateManager.navigateBack() },
                featureActions = featureActions
            )
        }
    }
}