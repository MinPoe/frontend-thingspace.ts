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
import com.cpen321.usermanagement.ui.screens.LoadingScreen
import com.cpen321.usermanagement.ui.screens.MainScreen
//import com.cpen321.usermanagement.ui.screens.ManageHobbiesScreen
import com.cpen321.usermanagement.ui.screens.ManageProfileScreen
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
import com.cpen321.usermanagement.utils.FeatureContext
import com.cpen321.usermanagement.utils.IFeatureActions

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
    const val OTHER_PROFILE = "profile"
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
        navigationStateManager = navigationStateManager
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
        }

        is NavigationEvent.NavigateToMainWithMessage -> {
            mainViewModel.setSuccessMessage(navigationEvent.message) //NOTE: this is how to access context
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToProfileCompletion -> {
            navController.navigate(NavRoutes.PROFILE_COMPLETION) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToProfile -> {
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
            navController.navigate(NavRoutes.COPY) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToFields -> {
            navController.navigate(NavRoutes.FIELDS) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToFilter -> {
            navController.navigate(NavRoutes.FILTER) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToInvite -> {
            navController.navigate(NavRoutes.INVITE) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMembersManager -> {
            navController.navigate(NavRoutes.MEMBERS_MANAGER) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMembers -> {
            navController.navigate(NavRoutes.MEMBERS) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToNote -> {
            navController.navigate(NavRoutes.NOTE) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToOtherProfile -> {
            navController.navigate(NavRoutes.OTHER_PROFILE) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToSharing -> {
            navController.navigate(NavRoutes.SHARING) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToTemplate -> {
            navController.navigate(NavRoutes.TEMPLATE) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsCreation -> {
            navController.navigate(NavRoutes.WS_CREATION) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsProfileManager -> {
            navController.navigate(NavRoutes.WS_PROFILE_MANAGER) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsProfile -> {
            navController.navigate(NavRoutes.WS_PROFILE) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWsSelect -> {
            navController.navigate(NavRoutes.WS_SELECT) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMainWithContext -> {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }
    }
}


class FeatureActions(private val navigationStateManager: NavigationStateManager) :
    IFeatureActions {

    override fun navigateToChat(context: FeatureContext) {
        navigationStateManager.navigateToChat(context)
    }

    override fun navigateToCopy(context: FeatureContext) {
        navigationStateManager.navigateToCopy(context)
    }

    override fun navigateToFields(context: FeatureContext) {
        navigationStateManager.navigateToFields(context)
    }

    override fun navigateToFilter(context: FeatureContext) {
        navigationStateManager.navigateToFilter(context)
    }

    override fun navigateToInvite(context: FeatureContext) {
        navigationStateManager.navigateToInvite(context)
    }

    override fun navigateToMembersManager(context: FeatureContext) {
        navigationStateManager.navigateToMembersManager(context)
    }

    override fun navigateToMembers(context: FeatureContext) {
        navigationStateManager.navigateToMembers(context)
    }

    override fun navigateToMainWithContext(context: FeatureContext) {
        navigationStateManager.navigateToMainWithContext(context)
    }

    override fun navigateToNote(context: FeatureContext) {
        navigationStateManager.navigateToNote(context)
    }

    override fun navigateToOtherProfile(context: FeatureContext) {
        navigationStateManager.navigateToOtherProfile(context)
    }

    override fun navigateToSharing(context: FeatureContext) {
        navigationStateManager.navigateToSharing(context)
    }

    override fun navigateToTemplate(context: FeatureContext) {
        navigationStateManager.navigateToTemplate(context)
    }

    override fun navigateToWsCreation(context: FeatureContext) {
        navigationStateManager.navigateToWsCreation(context)
    }

    override fun navigateToWsProfileManager(context: FeatureContext) {
        navigationStateManager.navigateToWsProfileManager(context)
    }

    override fun navigateToWsProfile(context: FeatureContext) {
        navigationStateManager.navigateToWsProfile(context)
    }

    override fun navigateToWsSelect(context: FeatureContext) {
        navigationStateManager.navigateToWsSelect(context)
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
    navigationStateManager: NavigationStateManager
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
                featureActions = FeatureActions(navigationStateManager)
            )
        }

        composable(NavRoutes.TEMPLATE){
            TemplateScreen(
                templateViewModel = templateViewModel,
                onProfileClick = { navigationStateManager.navigateToProfile() },
                //TODO: change 'personal' to user id once we have access to
                featureActions = FeatureActions(navigationStateManager)
            )
        }

        composable(NavRoutes.PROFILE) {
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
                featureActions = FeatureActions(navigationStateManager))
        }

    }
}
