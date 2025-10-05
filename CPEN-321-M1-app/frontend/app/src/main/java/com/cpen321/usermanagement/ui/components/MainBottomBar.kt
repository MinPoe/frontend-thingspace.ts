package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing

@Composable
fun MainBottomBar(
    onCreateNoteClick: ()->Unit,
    onWorkspacesClick: ()-> Unit,
    onProfileClick: ()->Unit,
    onTemplatesClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {
            NoteActionButton(onClick = onCreateNoteClick)
            TemplateActionButton(onClick = onTemplatesClick)
            WorkspaceActionButton(onClick = onWorkspacesClick)
            ProfileActionButton(onClick = onProfileClick)},
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
private fun NoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        NoteIcon()
    }
}

@Composable
private fun NoteIcon() {
    Icon(
        name = R.drawable.ic_edit,
    )
}

@Composable
private fun TemplateActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        TemplateIcon()
    }
}

@Composable
private fun TemplateIcon() {
    Icon(
        name = R.drawable.ic_check, //TODO: change the icon to sth more meaningful
    )
}

@Composable
private fun WorkspaceActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        WorkspaceIcon()
    }
}

@Composable
private fun WorkspaceIcon() {
    Icon(
        name = R.drawable.ic_heart_smile, //TODO: change the icon to sth more meaningful
    )
}