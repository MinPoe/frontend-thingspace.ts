package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.FieldUpdate
import com.cpen321.usermanagement.R
import androidx.compose.ui.res.stringResource

object TitleTrim{
    const val MAX_LEN = 24

    fun trim(title:String):String{
        return if(title.length<= MAX_LEN) title else title.take(MAX_LEN)+"..."
    }
}

@Composable
fun WsProfileBar(
    onMembersClick: ()->Unit,
    onInviteClick: ()->Unit,
    onLeaveClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
            MembersActionButton(onClick = onMembersClick, showLabel = true)
            InviteActionButton(onClick = onInviteClick, showLabel = true)
            LeaveActionButton(onClick = onLeaveClick, showLabel = true)
            }
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
                MembersActionButton(onClick = onMembersClick, showLabel = true)
                InviteActionButton(onClick = onInviteClick, showLabel = true)
                DeleteActionButton(onClick = onDeleteClick, showLabel = true, labelText = stringResource(R.string.delete_workspace))
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
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(.9f)
    ){
        Button(
            onClick=onProfileClick,
            modifier=modifier.fillMaxWidth(.8f)) {
            Text(TitleTrim.trim(profileName))}
        BanActionButton(onClick = onBanClick, showLabel = true, labelText = stringResource(R.string.ban_member),
            modifier = modifier)
    }
}

@Composable
fun TemplateRow(
    title:String,
    onTitleClick:()->Unit,
    onEditClick:()->Unit,
    modifier:Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        val spacing = LocalSpacing.current
        Card(
            onClick = onTitleClick,
            modifier = modifier
                .fillMaxWidth(.85f)
                .padding(spacing.small),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = spacing.extraSmall
            )) { Text(TitleTrim.trim(title),
                modifier = modifier.padding(spacing.medium)) }
        EditActionButton(onEditClick, showLabel = true)
    }
}

@Composable
fun WorkspaceMembersRow(
    profileName:String,
    onProfileClick:()->Unit,
    modifier:Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(.9f)
    ){
        Button(
            onClick=onProfileClick,
            modifier=modifier.fillMaxWidth()) {
            Text(TitleTrim.trim(profileName))}
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
            onClick = onProfileClick,
            modifier = modifier.weight(1f, fill = false).testTag(workspaceName).fillMaxWidth(.9f)
        ) {
            Text(
                text = TitleTrim.trim(workspaceName),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        ContentNoteActionButton(
            onClick = onContentClick,
            modifier=modifier,
            relatedWorkspace = workspaceName,
            showLabel = true
        )
        TemplateActionButton(
            onClick = onTemplatesClick,
            modifier=modifier,
            relatedWorkspace = workspaceName,
            showLabel = true
        )
        ChatActionButton(
            onClick = onChatClick,
            modifier=modifier,
            relatedWorkspace = workspaceName,
            showLabel = true
        )
    }
}

@Composable
fun MainBottomBar(
    onCreateNoteClick: ()->Unit,
    onWorkspacesClick: ()-> Unit,
    onChatClick:()->Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true  // Set to true to show text labels with icons
){
    if (showLabels) {
        // Use Surface when labels are shown - it can expand to fit content
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CreateNoteActionButton(onClick = onCreateNoteClick, showLabel = true)
                ChatActionButton(onClick = onChatClick, showLabel = true)
                WorkspaceActionButton(onClick = onWorkspacesClick, showLabel = true)
            }
        }
    } else {
        // Use BottomAppBar when no labels - more compact
        BottomAppBar(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CreateNoteActionButton(onClick = onCreateNoteClick, showLabel = false)
                ChatActionButton(onClick = onChatClick, showLabel = false)
                WorkspaceActionButton(onClick = onWorkspacesClick, showLabel = false)
            }
        }
    }
}

@Composable
fun TemplateBottomBar(
    onWorkspacesClick: ()-> Unit,
    onChatClick: ()->Unit,
    onContentClick: ()->Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true  // Set to true to show text labels with icons
){
    if (showLabels) {
        // Use Surface when labels are shown - it can expand to fit content
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContentNoteActionButton(onClick = onContentClick, showLabel = true)
                ChatActionButton(onClick = onChatClick, showLabel = true)
                WorkspaceActionButton(onClick = onWorkspacesClick, showLabel = true)
            }
        }
    } else {
        // Use BottomAppBar when no labels - more compact
        BottomAppBar(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContentNoteActionButton(onClick = onContentClick, showLabel = false)
                ChatActionButton(onClick = onChatClick, showLabel = false)
                WorkspaceActionButton(onClick = onWorkspacesClick, showLabel = false)
            }
        }
    }
}

