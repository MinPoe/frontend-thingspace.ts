package com.cpen321.usermanagement.ui.components

import Icon
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing

@Composable
fun ContentNoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String=""
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_notes,
            contentDescription = stringResource(R.string.content)+relatedWorkspace
        )
    }
}


@Composable
fun ChatActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String = ""
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.chat,
            contentDescription = stringResource(R.string.chat)+relatedWorkspace
        )
    }
}

@Composable
fun CreateNoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_edit,
            contentDescription = stringResource(R.string.create)
        )
    }
}

@Composable
fun TemplateActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace:String="",
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_templates,
            contentDescription = stringResource(R.string.templates)+relatedWorkspace
        )
    }
}

@Composable
fun WorkspaceActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_workspaces,
            contentDescription = stringResource(R.string.workspaces)
        )
    }
}

@Composable
fun EditActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relatedWorkspace: String = ""
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_edit,
            contentDescription = stringResource(R.string.edit)+relatedWorkspace
        )
    }
}

@Composable
fun MembersActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_account_circle,
            contentDescription = stringResource(R.string.members)
        )
    }
}


@Composable
fun InviteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_manage_profile,
            contentDescription = stringResource(R.string.invite)
        )
    }
}

@Composable
fun LeaveActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_sign_out,
            contentDescription = stringResource(R.string.leave)
        )
    }
}

@Composable
fun DeleteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_delete_forever,
            contentDescription = stringResource(R.string.delete)
        )
    }
}

@Composable
fun BanActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_delete_forever,
            contentDescription = stringResource(R.string.ban)
        )
    }
}

