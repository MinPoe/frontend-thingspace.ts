package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.ui.text.style.TextOverflow

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
            modifier=modifier,
            relatedWorkspace = workspaceName,
        )
        TemplateActionButton(
            onClick = onTemplatesClick,
            modifier=modifier,
            relatedWorkspace = workspaceName,
        )
        ChatActionButton(
            onClick = onChatClick,
            modifier=modifier,
            relatedWorkspace = workspaceName,
        )
        EditActionButton(
            onClick = onProfileClick,
            modifier=modifier,
            relatedWorkspace = workspaceName,
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
    modifier: Modifier = Modifier,
    showLabels: Boolean = true  // Set to true to show text labels with icons
){
    BottomAppBar(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContentNoteActionButton(onClick = onContentClick, showLabel = showLabels)
            TemplateActionButton(onClick = onTemplatesClick, showLabel = showLabels)
            ChatActionButton(onClick = onChatClick, showLabel = showLabels)
            WorkspaceActionButton(onClick = onWorkspacesClick, showLabel = showLabels)
            CreateNoteActionButton(onClick = onCreateNoteClick, showLabel = showLabels)
        }
    }
}

