package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import MenuButtonItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.ui.components.MessageSnackbar
import com.cpen321.usermanagement.ui.components.MessageSnackbarState
import com.cpen321.usermanagement.ui.components.WsProfileBar
import com.cpen321.usermanagement.ui.navigation.FeatureActions
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.ProfileUiState
import com.cpen321.usermanagement.ui.viewmodels.ProfileViewModel
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.WsProfileViewModel


@Composable
fun WsProfileScreen(
    wsProfileViewModel: WsProfileViewModel,
    featureActions: FeatureActions,
    onBackClick: () -> Unit,
) {
    val uiState by wsProfileViewModel.uiState.collectAsState()

//    // Load the profile only once
//    LaunchedEffect(Unit) {
//        if (uiState.workspace == null) {
//            wsProfileViewModel.loadProfile() // Or loadOtherUserProfile(id) if needed
//        }
//    }

    //events - 4 now leave empty
    val onMembersClick = {featureActions.navigateToMembers()}
    val onInviteClick = {featureActions.navigateToInvite()}
    val onLeaveClick = {val result:Boolean = wsProfileViewModel.leaveWorkspace()
    onBackClick()}

    Scaffold(
        topBar = { ViewProfileTopBar(onBackClick = onBackClick) },
        bottomBar = { WsProfileBar(onMembersClick, onInviteClick, onLeaveClick) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoadingProfile -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.workspace != null -> {
                    ViewWsProfileContent(workspace = uiState.workspace!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewProfileTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.view_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(name = R.drawable.ic_arrow_back)
            }
        }
    )
}

@Composable
private fun ViewWsProfileContent(
    workspace: Workspace,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.large)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        // --- Profile Picture ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = RetrofitClient.getPictureUri(
                        workspace.profile.imagePath ?: ""), //TODO: 4 now
                    contentDescription = stringResource(R.string.profile_picture),
                    modifier = Modifier
                        .size(spacing.extraLarge5)
                        .clip(CircleShape)
                )
            }
        }

        // --- Profile Info (Read-only) ---
        OutlinedTextField(
            value = workspace.profile.name,
            onValueChange = { },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false
        )
        OutlinedTextField(
            value = workspace.profile.description ?: "",
            onValueChange = { },
            label = { Text(stringResource(R.string.bio)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            minLines = 3,
            maxLines = 5
        )
    }
}
