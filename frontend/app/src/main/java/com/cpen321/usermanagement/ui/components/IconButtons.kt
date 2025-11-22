package com.cpen321.usermanagement.ui.components

import Icon
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing

/**
 * Reusable composable for an icon button with optional text label below it.
 * This is great for bottom navigation bars and action buttons.
 */
@Composable
fun IconWithLabel(
    iconRes: Int,
    label: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = label ?: ""
) {
    val spacing = LocalSpacing.current
    
    if (label != null) {
        // Icon with text label below
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .clickable(onClick = onClick)
                .padding(8.dp)
        ) {
            Icon(
                name = iconRes,
                contentDescription = contentDescription,
                modifier = Modifier.size(spacing.large)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    } else {
        // Just icon (original behavior)
        IconButton(
            onClick = onClick,
            modifier = modifier.size(spacing.extraLarge2)
        ) {
            Icon(
                name = iconRes,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
fun ContentNoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String = "",
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.content) else null

    IconWithLabel(
        iconRes = R.drawable.ic_notes,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.content) + relatedWorkspace
    )
}


@Composable
fun ChatActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String = "",
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.chat) else null

    IconWithLabel(
        iconRes = R.drawable.chat,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.chat) + relatedWorkspace
    )
}

@Composable
fun CreateNoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.create) else null

    IconWithLabel(
        iconRes = R.drawable.ic_edit,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.create)
    )
}

@Composable
fun TemplateActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String = "",
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.templates) else null

    IconWithLabel(
        iconRes = R.drawable.ic_templates,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.templates) + relatedWorkspace
    )
}

@Composable
fun WorkspaceActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.workspaces) else null

    IconWithLabel(
        iconRes = R.drawable.ic_workspaces,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.workspaces)
    )
}

@Composable
fun EditActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String = "",
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.edit) else null

    IconWithLabel(
        iconRes = R.drawable.ic_edit,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.edit) + relatedWorkspace
    )
}

@Composable
fun MembersActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.members) else null

    IconWithLabel(
        iconRes = R.drawable.ic_account_circle,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.members)
    )
}


@Composable
fun InviteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.invite) else null

    IconWithLabel(
        iconRes = R.drawable.ic_manage_profile,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.invite)
    )
}

@Composable
fun LeaveActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.leave) else null

    IconWithLabel(
        iconRes = R.drawable.ic_sign_out,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.leave)
    )
}

@Composable
fun DeleteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.delete) else null

    IconWithLabel(
        iconRes = R.drawable.ic_delete_forever,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.delete)
    )
}

@Composable
fun BanActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val label = if (showLabel) stringResource(R.string.ban) else null

    IconWithLabel(
        iconRes = R.drawable.ic_delete_forever,
        label = label,
        onClick = onClick,
        modifier = modifier,
        contentDescription = stringResource(R.string.ban)
    )
}

