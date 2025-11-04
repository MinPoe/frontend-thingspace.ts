package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.FieldCreationData
import com.cpen321.usermanagement.ui.viewmodels.FieldType
import com.cpen321.usermanagement.ui.viewmodels.FieldUpdate
import com.cpen321.usermanagement.ui.viewmodels.NoteEditViewModel
import com.cpen321.usermanagement.ui.viewmodels.NoteEditState
import com.cpen321.usermanagement.utils.FeatureActions
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.ui.screens.TagsEditSection
import com.cpen321.usermanagement.ui.screens.FieldsEditSection
import com.cpen321.usermanagement.ui.screens.WorkspaceSelectionDialog

@Composable
fun NoteEditScreen(
    noteEditViewModel: NoteEditViewModel,
    onBackClick: () -> Unit,
    featureActions: FeatureActions
) {
    val editState by noteEditViewModel.editState.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        noteEditViewModel.loadNote(featureActions.state.getNoteId())
    }

    LaunchedEffect(editState.isSuccess) {
        if (editState.isSuccess) {
            onBackClick()
        }
    }

    LaunchedEffect(editState.shareSuccess) {
        if (editState.shareSuccess) {
            showShareDialog = false
            noteEditViewModel.resetActionStates()
            onBackClick()
        }
    }

    LaunchedEffect(editState.copySuccess) {
        if (editState.copySuccess) {
            showCopyDialog = false
            noteEditViewModel.resetActionStates()
            onBackClick()
        }
    }

    when {
        editState.isLoading -> LoadingEditContent()
        editState.loadError != null -> ErrorEditContent(
            error = editState.loadError!!,
            onBackClick = onBackClick
        )
        else -> {
            val onSaveClick = { noteEditViewModel.saveNote(featureActions.state.getNoteId()) }
            val onShareClick = { showShareDialog = true }
            val onCopyClick = { showCopyDialog = true }

            NoteEditContent(
                editState = editState,
                onBackClick = onBackClick,
                onSaveClick = onSaveClick,
                onShareClick = onShareClick,
                onCopyClick = onCopyClick,
                onTagAdded = noteEditViewModel::addTag,
                onTagRemoved = noteEditViewModel::removeTag,
                onFieldAdded = noteEditViewModel::addField,
                onFieldRemoved = noteEditViewModel::removeField,
                onFieldUpdated = noteEditViewModel::updateField,
                onNoteTypeChanged = noteEditViewModel::updateNoteType
            )

            ShareNoteDialog(
                showDialog = showShareDialog,
                editState = editState,
                noteId = featureActions.state.getNoteId(),
                onDismiss = { showShareDialog = false },
                onShare = { workspaceId ->
                    noteEditViewModel.shareNote(featureActions.state.getNoteId(), workspaceId)
                }
            )

            CopyNoteDialog(
                showDialog = showCopyDialog,
                editState = editState,
                noteId = featureActions.state.getNoteId(),
                onDismiss = { showCopyDialog = false },
                onCopy = { workspaceId ->
                    noteEditViewModel.copyNote(featureActions.state.getNoteId(), workspaceId)
                }
            )
        }
    }
}

@Composable
private fun ShareNoteDialog(
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
private fun CopyNoteDialog(
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
private fun LoadingEditContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorEditContent(
    error: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_loading_note),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(spacing.small))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(spacing.large))
        Button(onClick = onBackClick) {
            Text(stringResource(R.string.go_back))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditContent(
    editState: NoteEditState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onFieldAdded: (FieldType) -> Unit,
    onFieldRemoved: (String) -> Unit,
    onFieldUpdated: (String, FieldUpdate) -> Unit,
    onNoteTypeChanged: (NoteType) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { NoteEditTopBar(onBackClick = onBackClick, onShareClick = onShareClick, onCopyClick = onCopyClick) },
        bottomBar = { NoteEditBottomBar(onBackClick = onBackClick, onSaveClick = onSaveClick, isSaving = editState.isSaving) }
    ) { paddingValues ->
        NoteEditBody(
            editState = editState,
            paddingValues = paddingValues,
            onTagAdded = onTagAdded,
            onTagRemoved = onTagRemoved,
            onFieldAdded = onFieldAdded,
            onFieldRemoved = onFieldRemoved,
            onFieldUpdated = onFieldUpdated,
            onNoteTypeChanged = onNoteTypeChanged
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditTopBar(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.edit_note),
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
                Icon(name = R.drawable.ic_share_note)
            }
            IconButton(onClick = onCopyClick) {
                Icon(name = R.drawable.ic_copy)
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
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onFieldAdded: (FieldType) -> Unit,
    onFieldRemoved: (String) -> Unit,
    onFieldUpdated: (String, FieldUpdate) -> Unit,
    onNoteTypeChanged: (NoteType) -> Unit,
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
        NoteTypeEditSection(
            selectedType = editState.noteType,
            onTypeChanged = onNoteTypeChanged
        )

        Spacer(modifier = Modifier.height(spacing.large))

        // Tags Section
        TagsEditSection(
            tags = editState.tags,
            onTagAdded = onTagAdded,
            onTagRemoved = onTagRemoved
        )

        Spacer(modifier = Modifier.height(spacing.large))

        // Fields Section
        FieldsEditSection(
            fields = editState.fields,
            noteType = editState.noteType,
            onFieldAdded = onFieldAdded,
            onFieldRemoved = onFieldRemoved,
            onFieldUpdated = onFieldUpdated
        )
    }
}

@Composable
private fun NoteTypeEditSection(
    selectedType: NoteType,
    onTypeChanged: (NoteType) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.note_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                NoteType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeChanged(type) },
                        label = { Text(type.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
