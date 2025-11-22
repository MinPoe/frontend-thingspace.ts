package com.cpen321.usermanagement.ui.components

import Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.ui.viewmodels.FieldType
import com.cpen321.usermanagement.ui.viewmodels.NoteEditState

@Composable
fun FieldTypeDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (FieldType) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_field_type)) },
        text = {
            Column {
                FieldType.values().forEach { type ->
                    TextButton(
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = type.name,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        modifier = modifier
    )
}

@Composable
fun WorkspaceSelectionDialog(
    title: String,
    confirmText: String,
    isProcessing: Boolean,
    workspaces: List<Workspace>,
    isLoadingWorkspaces: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedWorkspaceId by remember { mutableStateOf("") }
    val workspacePairs = remember { workspaces.map{Pair(it._id, it.profile.name)}}

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = { Text(title) },
        text = {
            WorkspaceSelectionContent(
                isLoadingWorkspaces = isLoadingWorkspaces,
                workspacePairs = workspacePairs,
                selectedWorkspaceId = selectedWorkspaceId,
                isProcessing = isProcessing,
                onWorkspaceSelected = { selectedWorkspaceId = it }
            )
        },
        confirmButton = {
            WorkspaceSelectionConfirmButton(
                onConfirm = { onConfirm(selectedWorkspaceId) },
                confirmText = confirmText,
                isProcessing = isProcessing,
                isEnabled = selectedWorkspaceId.isNotEmpty() && !isProcessing
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun WorkspaceSelectionContent(
    isLoadingWorkspaces: Boolean,
    workspacePairs: List<Pair<String, String>>,
    selectedWorkspaceId: String,
    isProcessing: Boolean,
    onWorkspaceSelected: (String) -> Unit
) {
    if (isLoadingWorkspaces) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        Column {
            Text(stringResource(R.string.select_workspace))
            Spacer(modifier = Modifier.height(8.dp))
            workspacePairs.forEach { (id, name) ->
                WorkspaceSelectionItem(
                    id = id,
                    name = name,
                    isSelected = selectedWorkspaceId == id,
                    isProcessing = isProcessing,
                    onSelected = onWorkspaceSelected
                )
            }
        }
    }
}

@Composable
private fun WorkspaceSelectionItem(
    id: String,
    name: String,
    isSelected: Boolean,
    isProcessing: Boolean,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isProcessing) { onSelected(id) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelected(id) },
            enabled = !isProcessing
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(name)
    }
}

@Composable
private fun WorkspaceSelectionConfirmButton(
    onConfirm: () -> Unit,
    confirmText: String,
    isProcessing: Boolean,
    isEnabled: Boolean
) {
    Button(
        onClick = onConfirm,
        enabled = isEnabled
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(confirmText)
        }
    }
}


@Composable
fun CopyNoteDialog(
    showDialog: Boolean,
    editState: NoteEditState,
    noteId: String,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    if (showDialog) {
        WorkspaceSelectionDialog(
            title = stringResource(R.string.copy_note),
            confirmText = stringResource(R.string.copy),
            isProcessing = editState.isCopying,
            workspaces = editState.workspaces,
            isLoadingWorkspaces = editState.isLoadingWorkspaces,
            onDismiss = onDismiss,
            onConfirm = onCopy
        )
    }
}

@Composable
fun ShareNoteDialog(
    showDialog: Boolean,
    editState: NoteEditState,
    noteId: String,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    if (showDialog) {
        WorkspaceSelectionDialog(
            title = stringResource(R.string.share_note),
            confirmText = stringResource(R.string.share),
            isProcessing = editState.isSharing,
            workspaces = editState.workspaces,
            isLoadingWorkspaces = editState.isLoadingWorkspaces,
            onDismiss = onDismiss,
            onConfirm = onShare
        )
    }
}

@Composable
fun DeleteNoteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.delete_note)) },
            text = { Text(stringResource(R.string.delete_note_confirmation)) },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
