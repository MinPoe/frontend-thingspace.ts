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
import com.cpen321.usermanagement.ui.screens.LoadingScreen
import com.cpen321.usermanagement.ui.screens.MainScreen
//import com.cpen321.usermanagement.ui.screens.ManageHobbiesScreen
import com.cpen321.usermanagement.ui.screens.ManageProfileScreen
import com.cpen321.usermanagement.ui.screens.ProfileScreenActions
import com.cpen321.usermanagement.ui.screens.ProfileCompletionScreen
import com.cpen321.usermanagement.ui.screens.ProfileScreen
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
    const val CHAT = "chat"
    const val COPY = "copy"
    const val FIELDS = "fields"
    const val FILTER = "filter"
    const val INVITE = "invite"
    const val MEMBERS_MANAGER = "members_manager"
    const val MEMBERS = "members"
    const val NAVIGATION = "navigation"
    const val NOTE = "note"
    const val OTHER_PROFILE = "profile"
    const val SHARING = "sharing"
    const val TEMPLATE = "template"
    const val WS_CREATION = "ws_creation"
    const val WS_PROFILE_MANAGER = "ws_profile_manager"
    const val WS_PROFILE = "ws_profile"
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

        is NavigationEvent.NavigateToNavigation -> {
            navController.navigate(NavRoutes.NAVIGATION) {
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
                onNoteClick = {},
                onTemplateClick = {},
                onWorkspaceClick = {},
                onFilterClick = {}
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


    }
}
