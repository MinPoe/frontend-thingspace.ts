package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource

@Composable
fun WsProfileBar(
    onMembersClick: ()->Unit,
    onInviteClick: ()->Unit,
    onLeaveClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {
            MembersActionButton(onClick = onMembersClick)
            InviteActionButton(onClick = onInviteClick)
            LeaveActionButton(onClick = onLeaveClick)
                  },
        modifier = modifier
    )
}

@Composable
fun WsProfileManagerBar(
    onMembersClick: ()->Unit,
    onInviteClick: ()->Unit,
    onDeleteClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MembersActionButton(onClick = onMembersClick)
                InviteActionButton(onClick = onInviteClick)
                DeleteActionButton(onClick = onDeleteClick)
            }
        },
        modifier = modifier
    )
}


@Composable
fun WorkspaceMembersManagerRow(
    profileName:String,
    onProfileClick: ()->Unit,
    onBanClick: ()-> Unit,
    modifier:Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            onClick=onProfileClick,
            modifier=modifier) {
            Text(
                if(profileName.length<11) profileName else profileName.take(10)+"...")}
        BanActionButton(onClick = onBanClick)
    }
}

@Composable
fun WorkspaceMembersRow(
    profileName:String,
    onProfileClick: ()->Unit,
    modifier:Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            onClick=onProfileClick,
            modifier=modifier) {
            Text(
                if(profileName.length<11) profileName else profileName.take(10)+"...")}
    }
}

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
            onClick = {},
            modifier = modifier.weight(1f, fill = false)
        ) {
            Text(
                text = if(workspaceName.length < 25) workspaceName else workspaceName.take(24) + "...",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
        EditActionButton(
            onClick = onProfileClick,
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
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContentNoteActionButton(onClick = onContentClick)
            TemplateActionButton(onClick = onTemplatesClick)
            ChatActionButton(onClick = onChatClick)
            WorkspaceActionButton(onClick = onWorkspacesClick)
            CreateNoteActionButton(onClick = onCreateNoteClick)
        }
    }
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
        name = R.drawable.ic_notes,
        contentDescription = stringResource(R.string.content),
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
        name = R.drawable.chat,
        contentDescription = stringResource(R.string.chat)
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
        contentDescription = stringResource(R.string.create)
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
        name = R.drawable.ic_templates,
        contentDescription = stringResource(R.string.templates)
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
        name = R.drawable.ic_workspaces,
        contentDescription = stringResource(R.string.workspaces)
    )
}

@Composable
private fun EditActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        EditIcon()
    }
}

@Composable
private fun EditIcon() {
    Icon(
        name = R.drawable.ic_edit,
        contentDescription = stringResource(R.string.edit)
    )
}


@Composable
private fun MembersActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        MembersIcon()
    }
}

@Composable
private fun MembersIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
        contentDescription = stringResource(R.string.members)
    )
}


@Composable
private fun InviteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        InviteIcon()
    }
}

@Composable
private fun InviteIcon() {
    Icon(
        name = R.drawable.ic_manage_profile,
        contentDescription = stringResource(R.string.invite)
    )
}


@Composable
private fun LeaveActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        LeaveIcon()
    }
}

@Composable
private fun LeaveIcon() {
    Icon(
        name = R.drawable.ic_sign_out,
        contentDescription = stringResource(R.string.leave)
    )
}

@Composable
private fun DeleteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        DeleteIcon()
    }
}

@Composable
private fun DeleteIcon() {
    Icon(
        name = R.drawable.ic_delete_forever,
        contentDescription = stringResource(R.string.delete)
    )
}

@Composable
private fun BanActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        BanIcon()
    }
}

@Composable
private fun BanIcon() {
    Icon(
        name = R.drawable.ic_delete_forever,
        contentDescription = stringResource(R.string.ban)
    )
}