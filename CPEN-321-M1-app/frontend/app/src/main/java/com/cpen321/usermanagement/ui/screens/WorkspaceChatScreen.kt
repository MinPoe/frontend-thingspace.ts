package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.components.MainBottomBar
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing

@Composable
fun WorkspaceChatScreen(
    context_workspace: String?,
    onProfileClick: () -> Unit,
    onNoteClick: () -> Unit,
    onTemplateClick: ()-> Unit,
    onWorkspaceClick: () -> Unit
) {
    WorkspaceChatContent(
        context_workspace = context_workspace,
        onProfileClick = onProfileClick,
        onNoteClick = onNoteClick,
        onTemplateClick = onTemplateClick,
        onWorkspaceClick = onWorkspaceClick
    )
}

@Composable
private fun WorkspaceChatContent(
    context_workspace: String?,
    onProfileClick: () -> Unit,
    onNoteClick: () -> Unit,
    onTemplateClick: ()-> Unit,
    onWorkspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            //TODO: change attributes later
            MainBottomBar(
                onProfileClick = onProfileClick,
                onCreateNoteClick = onNoteClick,
                onTemplatesClick = onTemplateClick,
                onWorkspacesClick = onWorkspaceClick
            )
        }
    ) { paddingValues ->
        WorkspaceChatScreenBody(paddingValues = paddingValues, context_workspace = context_workspace)
    }
}

@Composable
private fun WorkspaceChatScreenBody( //TODO:for now copy of main, change to actual note adding
    paddingValues: PaddingValues,
    context_workspace: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        WorkspaceMessage(context_workspace)
    }
}

@Composable //TODO: Replace with actual content
private fun WorkspaceMessage(
    context_workspace: String?,
    modifier: Modifier = Modifier
) {
    val fontSizes = LocalFontSizes.current

    Text(
        text = context_workspace?.toString() ?: "No workspace info",
        style = MaterialTheme.typography.bodyLarge,
        fontSize = fontSizes.extraLarge3,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

