package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.FieldType
import com.cpen321.usermanagement.ui.viewmodels.FieldUpdate
import com.cpen321.usermanagement.ui.viewmodels.NoteEditViewModel
import com.cpen321.usermanagement.ui.viewmodels.NoteEditState
import com.cpen321.usermanagement.utils.FeatureActions
import com.cpen321.usermanagement.ui.components.LoadingEditContent
import com.cpen321.usermanagement.ui.components.ErrorEditContent
import com.cpen321.usermanagement.ui.components.CopyNoteDialog
import com.cpen321.usermanagement.ui.components.DeleteNoteDialog
import com.cpen321.usermanagement.ui.components.NoteInfoRow
import com.cpen321.usermanagement.ui.components.ShareNoteDialog

data class NoteEditCallbacks(
    val onBackClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onShareClick: () -> Unit,
    val onCopyClick: () -> Unit,
    val onDeleteClick: () -> Unit,
    val onTagAdded: (String) -> Unit,
    val onTagRemoved: (String) -> Unit,
    val onFieldAdded: (FieldType) -> Unit,
    val onFieldRemoved: (String) -> Unit,
    val onFieldUpdated: (String, FieldUpdate) -> Unit,
)

@Composable
fun NoteEditScreen(
    noteEditViewModel: NoteEditViewModel,
    onBackClick: () -> Unit,
    featureActions: FeatureActions
) {
    val editState by noteEditViewModel.editState.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    NoteEditScreenLaunchedEffects(
        noteEditViewModel = noteEditViewModel,
        editState = editState,
        featureActions = featureActions,
        onBackClick = onBackClick,
        onShareDialogDismiss = { showShareDialog = false },
        onCopyDialogDismiss = { showCopyDialog = false },
        onDeleteDialogDismiss = { showDeleteDialog = false }
    )

    when {
        editState.isLoading -> LoadingEditContent()
        editState.loadError != null -> ErrorEditContent(
            error = editState.loadError!!,
            onBackClick = onBackClick
        )
        else -> {
            NoteEditScreenContent(
                editState = editState,
                noteEditViewModel = noteEditViewModel,
                featureActions = featureActions,
                onBackClick = onBackClick,
                dialogState = DialogState(
                    showShareDialog = showShareDialog,
                    showCopyDialog = showCopyDialog,
                    onShareDialogChange = { showShareDialog = it },
                    onCopyDialogChange = { showCopyDialog = it },
                    showDeleteDialog = showDeleteDialog,
                    onDeleteDialogChange = { showDeleteDialog = it }
                )
            )
        }
    }
}

@Composable
private fun NoteEditScreenLaunchedEffects(
    noteEditViewModel: NoteEditViewModel,
    editState: NoteEditState,
    featureActions: FeatureActions,
    onBackClick: () -> Unit,
    onShareDialogDismiss: () -> Unit,
    onCopyDialogDismiss: () -> Unit,
    onDeleteDialogDismiss: ()->Unit
) {
    LaunchedEffect(Unit) {
        noteEditViewModel.loadNote(featureActions.state.getNoteId())
    }

    LaunchedEffect(editState.isSuccess) {
        if (editState.isSuccess) {
            noteEditViewModel.resetActionStates()
            onBackClick()
        }
    }

    LaunchedEffect(editState.shareSuccess) {
        if (editState.shareSuccess) {
            onShareDialogDismiss()
            noteEditViewModel.resetActionStates()
            onBackClick()
        }
    }

    LaunchedEffect(editState.copySuccess) {
        if (editState.copySuccess) {
            onCopyDialogDismiss()
            noteEditViewModel.resetActionStates()
            onBackClick()
        }
    }

    // Handle successful deletion
    LaunchedEffect(editState.isDeleted) {
        if (editState.isDeleted) {
            onDeleteDialogDismiss()
            noteEditViewModel.resetActionStates()
            onBackClick()
        }
    }
}

private data class DialogState(
    val showShareDialog: Boolean,
    val showCopyDialog: Boolean,
    val onShareDialogChange: (Boolean) -> Unit,
    val onCopyDialogChange: (Boolean) -> Unit,
    val showDeleteDialog: Boolean,
    val onDeleteDialogChange: (Boolean) -> Unit
)

@Composable
private fun NoteEditScreenContent(
    editState: NoteEditState,
    noteEditViewModel: NoteEditViewModel,
    featureActions: FeatureActions,
    onBackClick: () -> Unit,
    dialogState: DialogState
) {
    val onSaveClick = { noteEditViewModel.saveNote(featureActions.state.getNoteId()) }
    val onShareClick = { dialogState.onShareDialogChange(true) }
    val onCopyClick = { dialogState.onCopyDialogChange(true) }
    val onDeleteClick = { dialogState.onDeleteDialogChange(true) }

    NoteEditContent(
        editState = editState,
        callbacks = NoteEditCallbacks(
            onBackClick = onBackClick,
            onSaveClick = onSaveClick,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onDeleteClick = onDeleteClick,
            onTagAdded = noteEditViewModel::addTag,
            onTagRemoved = noteEditViewModel::removeTag,
            onFieldAdded = noteEditViewModel::addField,
            onFieldRemoved = noteEditViewModel::removeField,
            onFieldUpdated = noteEditViewModel::updateField
        )
    )

    ShareNoteDialog(
        showDialog = dialogState.showShareDialog,
        editState = editState,
        noteId = featureActions.state.getNoteId(),
        onDismiss = { dialogState.onShareDialogChange(false) },
        onShare = { workspaceId ->
            noteEditViewModel.shareNote(featureActions.state.getNoteId(), workspaceId)
        }
    )

    CopyNoteDialog(
        showDialog = dialogState.showCopyDialog,
        editState = editState,
        noteId = featureActions.state.getNoteId(),
        onDismiss = { dialogState.onCopyDialogChange(false) },
        onCopy = { workspaceId ->
            noteEditViewModel.copyNote(featureActions.state.getNoteId(), workspaceId)
        }
    )

    DeleteNoteDialog(
        showDialog = dialogState.showDeleteDialog,
        onDismiss = { dialogState.onDeleteDialogChange(false) },
        onConfirm = { noteEditViewModel.deleteNote(featureActions.state.getNoteId()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditContent(
    editState: NoteEditState,
    callbacks: NoteEditCallbacks,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { NoteEditTopBar(
            onBackClick = callbacks.onBackClick,
            onShareClick = callbacks.onShareClick,
            onCopyClick = callbacks.onCopyClick,
            onDeleteClick = callbacks.onDeleteClick,
            noteType = editState.noteType
        ) },
        bottomBar = { NoteEditBottomBar(
            onBackClick = callbacks.onBackClick,
            onSaveClick = callbacks.onSaveClick,
            isSaving = editState.isSaving
        ) }
    ) { paddingValues ->
        NoteEditBody(
            editState = editState,
            paddingValues = paddingValues,
            callbacks = callbacks
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditTopBar(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    noteType: NoteType
) {
    val shareDesc = stringResource(R.string.share)
    val copyDesc = stringResource(R.string.copy)

    TopAppBar(
        title = {
            Text(
                text = if(noteType== NoteType.CONTENT) stringResource(R.string.edit_note)
                else stringResource(R.string.edit_template),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(name = R.drawable.ic_arrow_back)
            }
        },
        actions = {
            IconButton(onClick = onShareClick) {
                Icon(
                    name = R.drawable.ic_share_note,
                    contentDescription = shareDesc
                )
            }
            IconButton(onClick = onCopyClick) {
                Icon(
                    name = R.drawable.ic_copy,
                    contentDescription = copyDesc
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    name = R.drawable.ic_delete_forever,
                    contentDescription = copyDesc
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun NoteEditBottomBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean
) {
    val spacing = LocalSpacing.current

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.medium),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(spacing.medium))
            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(spacing.medium),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
fun NoteEditBody(
    editState: NoteEditState,
    paddingValues: PaddingValues,
    callbacks: NoteEditCallbacks,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(spacing.medium)
            .verticalScroll(scrollState)
    ) {
        // Error message
        if (editState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = editState.error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(spacing.medium)
                )
            }
            Spacer(modifier = Modifier.height(spacing.medium))
        }

        // Note Type Selection
        NoteInfoRow(editState.createdAtString, editState.lastEditString)

        Spacer(modifier = Modifier.height(spacing.large))

        // Tags Section
        TagsEditSection(
            tags = editState.tags,
            onTagAdded = callbacks.onTagAdded,
            onTagRemoved = callbacks.onTagRemoved
        )

        Spacer(modifier = Modifier.height(spacing.large))

        // Fields Section
        FieldsEditSection(
            fields = editState.fields,
            onFieldAdded = callbacks.onFieldAdded,
            onFieldRemoved = callbacks.onFieldRemoved,
            onFieldUpdated = callbacks.onFieldUpdated,
            currentUser = editState.user
        )
    }
}
