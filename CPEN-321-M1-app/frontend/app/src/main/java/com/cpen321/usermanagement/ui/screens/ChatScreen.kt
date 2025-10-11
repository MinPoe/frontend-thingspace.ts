package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.components.MessageSnackbar
import com.cpen321.usermanagement.ui.components.MessageSnackbarState
import com.cpen321.usermanagement.ui.viewmodels.MainUiState
import com.cpen321.usermanagement.ui.viewmodels.ChatViewModel
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.components.MainBottomBar
import com.cpen321.usermanagement.ui.navigation.FeatureActions
import com.cpen321.usermanagement.utils.FeatureContext
import com.cpen321.usermanagement.utils.IFeatureActions

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    onProfileClick: () -> Unit,
    featureActions: IFeatureActions
) {
    val snackBarHostState = remember { SnackbarHostState() }

    MainContent(
        snackBarHostState = snackBarHostState,
        onProfileClick = onProfileClick,
        onNoteClick = { }, //TODO: for now
        onContentClick = {  featureActions.navigateToMainWithContext(
            FeatureContext(workspaceId = chatViewModel.getWorkspaceName()))},
        onWorkspaceClick = { featureActions.navigateToWsSelect(
            FeatureContext(workspaceId = chatViewModel.getWorkspaceName())) },
        onFilterClick = {  },
        onTemplateClick={featureActions.navigateToTemplate(
            context = FeatureContext(workspaceId = chatViewModel.getWorkspaceName())
        ) }
    )
}

@Composable
private fun MainContent(
    snackBarHostState: SnackbarHostState,
    onProfileClick: () -> Unit,
    onNoteClick: ()-> Unit,
    onContentClick: ()->Unit,
    onTemplateClick: ()-> Unit,
    onWorkspaceClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(onProfileClick = onProfileClick)
        },
        bottomBar = {
            MainBottomBar(
                onCreateNoteClick = onNoteClick,
                onWorkspacesClick = onWorkspaceClick,
                onChatClick = {  },
                onContentClick = onContentClick,
                onTemplatesClick = onTemplateClick,
                modifier = modifier)
        }
    ) { paddingValues ->
        MainBody(
            paddingValues = paddingValues,
            onFilterClick = onFilterClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            AppTitle()
        },
        actions = {
            ProfileActionButton(onClick = onProfileClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AppTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun ProfileActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ProfileIcon()
    }
}

@Composable
private fun ProfileIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
    )
}

@Composable
private fun MainSnackbarHost(
    hostState: SnackbarHostState,
    successMessage: String?,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageSnackbar(
        hostState = hostState,
        messageState = MessageSnackbarState(
            successMessage = successMessage,
            errorMessage = null,
            onSuccessMessageShown = onSuccessMessageShown,
            onErrorMessageShown = { }
        ),
        modifier = modifier
    )
}

@Composable
private fun MainBody(
    paddingValues: PaddingValues,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WelcomeMessage()
        Button(
            fullWidth = true,
            enabled = true,
            //TODO: Make Nicer Later, the point is we need a way to return to main somehow
            onClick = { onFilterClick() },
        ) {
            val fontSizes = LocalFontSizes.current
            Text(
                text = "filter",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
        }
    }
}
@Composable
private fun WelcomeMessage(
    modifier: Modifier = Modifier
) {
    val fontSizes = LocalFontSizes.current

    Text(
        text = stringResource(R.string.welcome),
        style = MaterialTheme.typography.bodyLarge,
        fontSize = fontSizes.extraLarge3,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

