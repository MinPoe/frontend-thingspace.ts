package com.cpen321.usermanagement.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.screens.AuthScreen
import com.cpen321.usermanagement.ui.screens.FilterScreen
import com.cpen321.usermanagement.ui.screens.LoadingScreen
import com.cpen321.usermanagement.ui.screens.MainScreen
//import com.cpen321.usermanagement.ui.screens.ManageHobbiesScreen
import com.cpen321.usermanagement.ui.screens.ManageProfileScreen
import com.cpen321.usermanagement.ui.screens.NoteScreen
import com.cpen321.usermanagement.ui.screens.ProfileScreenActions
import com.cpen321.usermanagement.ui.screens.ProfileCompletionScreen
import com.cpen321.usermanagement.ui.screens.ProfileScreen
import com.cpen321.usermanagement.ui.screens.TemplateScreen
import com.cpen321.usermanagement.ui.screens.WorkspaceChatScreen
import com.cpen321.usermanagement.ui.screens.WorkspaceInteriorScreen
import com.cpen321.usermanagement.ui.screens.WorkspaceListScreen
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.ui.viewmodels.NavigationViewModel
import com.cpen321.usermanagement.ui.viewmodels.ProfileViewModel

object NavRoutes {
    const val LOADING = "loading"
    const val AUTH = "auth"
    const val MAIN = "main"
    const val PROFILE = "profile"
    const val MANAGE_PROFILE = "manage_profile"
    const val PROFILE_COMPLETION = "profile_completion"
    const val NOTE = "note"
    const val TEMPLATE = "template"
    const val WORKSPACE_LIST = "workspace_list"
    const val WORKSPACE_INTERIOR = "workspace_interior"
    const val WORKSPACE_CHAT = "workspace_chat"
    const val FILTER = "filter"
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

    // Handle navigation events from NavigationStateManager
    LaunchedEffect(navigationEvent) {
        handleNavigationEvent(
            navigationEvent,
            navController,
            navigationStateManager,
            authViewModel,
            mainViewModel
        )
    }

    AppNavHost(
        navController = navController,
        authViewModel = authViewModel,
        profileViewModel = profileViewModel,
        mainViewModel = mainViewModel,
        navigationStateManager = navigationStateManager
    )
}

private fun handleNavigationEvent(
    navigationEvent: NavigationEvent,
    navController: NavHostController,
    navigationStateManager: NavigationStateManager,
    authViewModel: AuthViewModel,
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
            mainViewModel.setSuccessMessage(navigationEvent.message)
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

        is NavigationEvent.ClearBackStack -> {
            navController.popBackStack(navController.graph.startDestinationId, false)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToNote -> {
            navController.navigate(route = NavRoutes.NOTE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToTemplate -> {
            navController.navigate(route = NavRoutes.TEMPLATE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWorkspaceList ->{
            navController.navigate(route = NavRoutes.WORKSPACE_LIST)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWorkspaceInterior -> {
            navController.navigate(route = NavRoutes.WORKSPACE_INTERIOR)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToWorkspaceChat -> {
            navController.navigate(route = NavRoutes.WORKSPACE_CHAT)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToFilter -> {
            navController.navigate(route = NavRoutes.FILTER)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NoNavigation -> {
            // Do nothing
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    mainViewModel: MainViewModel,
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
                onNoteClick = {navigationStateManager.navigateToNote("personal") },
                onTemplateClick = {navigationStateManager.navigateToTemplate("personal")},
                onWorkspaceClick = {navigationStateManager.navigateToWorkspaceList()},
                onFilterClick = {navigationStateManager.navigateToFilter("personal")}
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

        composable(NavRoutes.NOTE){
            NoteScreen(
                onBackClick = {navigationStateManager.navigateBack()},
                context_workspace = navigationStateManager.getContextWorkspace()
            )
        }

        composable(NavRoutes.TEMPLATE){
            TemplateScreen(
                onBackClick = {navigationStateManager.navigateBack()},
                context_workspace = navigationStateManager.getContextWorkspace()
            )
        }

        composable(NavRoutes.WORKSPACE_LIST){
            WorkspaceListScreen(
                onBackClick = {navigationStateManager.navigateBack()},
                onWorkspaceClick = {
                    workspaceName -> navigationStateManager.navigateToWorkspaceInterior(workspaceName)
                },
                onBackToMainClick = {navigationStateManager.navigateToMain()}
            )
        }

        composable(NavRoutes.WORKSPACE_INTERIOR){
            val context_workspace:String? = navigationStateManager.getContextWorkspace()
            WorkspaceInteriorScreen(
                context_workspace = context_workspace,
                onProfileClick = {navigationStateManager.navigateToProfile()},
                onNoteClick = {navigationStateManager.navigateToNote(
                    //TODO: implement the default null value or raise error
                    context_workspace?.toString() ?: "no_workspace_info"
                )},
                onTemplateClick = {navigationStateManager.navigateToTemplate(
                    //TODO: implement the default null value or raise error
                    context_workspace?.toString() ?: "no_workspace_info"
                )
                },
                onWorkspaceClick = {navigationStateManager.navigateToWorkspaceList()},
                onChatClick ={navigationStateManager.navigateToWorkspaceChat(
                    //TODO: implement the default null value or raise error
                    context_workspace?.toString() ?: "no_workspace_info"
                )}
            )
        }

        composable(NavRoutes.WORKSPACE_CHAT){
            val context_workspace:String? = navigationStateManager.getContextWorkspace()
            WorkspaceChatScreen(
                context_workspace = context_workspace,
                onProfileClick = {navigationStateManager.navigateToProfile()},
                onNoteClick = {navigationStateManager.navigateToNote(
                    //TODO: implement the default null value or raise error
                    context_workspace?.toString() ?: "no_workspace_info"
                )},
                onTemplateClick = {navigationStateManager.navigateToTemplate(
                    //TODO: implement the default null value or raise error
                    context_workspace?.toString() ?: "no_workspace_info"
                )
                },
                onWorkspaceClick = {navigationStateManager.navigateToWorkspaceList()},
                onMainContentClick = {navigationStateManager.navigateToWorkspaceInterior(
                    //TODO: implement the default null value or raise error
                    context_workspace?.toString() ?: "no_workspace_info"
                )}
            )
        }

        composable(NavRoutes.FILTER){
            FilterScreen(
                onBackClick = {navigationStateManager.navigateBack()},
                context_workspace = navigationStateManager.getContextWorkspace()
            )
        }
    }
}
