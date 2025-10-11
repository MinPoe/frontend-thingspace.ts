package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button

@Composable
fun WorkspaceRow(
    workspaceName:String,
    onProfileClick: ()->Unit,
    onChatClick:()->Unit,
    onContentClick:()->Unit,
    onTemplatesClick: ()->Unit,
    modifier:Modifier = Modifier
){
    Row(
       verticalAlignment = Alignment.CenterVertically,
       horizontalArrangement = Arrangement.Center
    ){
        Button(
            onClick=onProfileClick,
            modifier=modifier) {
            Text(workspaceName) }
        ContentNoteActionButton(
            onClick = onContentClick,
            modifier=modifier
        )
        TemplateActionButton(
            onClick = onTemplatesClick,
            modifier=modifier
        )
        ChatActionButton(
            onClick = onChatClick,
            modifier=modifier
        )
    }
}

@Composable
fun MainBottomBar(
    onCreateNoteClick: ()->Unit,
    onWorkspacesClick: ()-> Unit,
    onChatClick:()->Unit,
    onContentClick:()->Unit,
    onTemplatesClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {
            ContentNoteActionButton(onClick = onContentClick)
            TemplateActionButton(onClick = onTemplatesClick)
            ChatActionButton(onClick = onChatClick)
            WorkspaceActionButton(onClick = onWorkspacesClick)
            CreateNoteActionButton(onClick = onCreateNoteClick)},
        modifier = modifier
    )
}

@Composable
private fun ContentNoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ContentIcon()
    }
}

@Composable
private fun ContentIcon() {
    Icon(
        name = R.drawable.ic_google,
    )
}

@Composable
private fun ChatActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ChatIcon()
    }
}

@Composable
private fun ChatIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
    )
}

@Composable
private fun CreateNoteActionButton(
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