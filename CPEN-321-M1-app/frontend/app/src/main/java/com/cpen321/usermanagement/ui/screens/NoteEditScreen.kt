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
import com.cpen321.usermanagement.utils.IFeatureActions
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp

@Composable
fun NoteEditScreen(
    noteEditViewModel: NoteEditViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
) {
    val editState by noteEditViewModel.editState.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        noteEditViewModel.loadNote(featureActions.getNoteId())
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
            val onSaveClick = { noteEditViewModel.saveNote(featureActions.getNoteId()) }
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
                noteId = featureActions.getNoteId(),
                onDismiss = { showShareDialog = false },
                onShare = { workspaceId ->
                    noteEditViewModel.shareNote(featureActions.getNoteId(), workspaceId)
                }
            )

            CopyNoteDialog(
                showDialog = showCopyDialog,
                editState = editState,
                noteId = featureActions.getNoteId(),
                onDismiss = { showCopyDialog = false },
                onCopy = { workspaceId ->
                    noteEditViewModel.copyNote(featureActions.getNoteId(), workspaceId)
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

@Composable
private fun TagsEditSection(
    tags: List<String>,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var tagInput by remember { mutableStateOf("") }

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
                text = stringResource(R.string.tags),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.small))

            TagInputField(
                tagInput = tagInput,
                onTagInputChange = { tagInput = it },
                onTagAdded = { tag ->
                    onTagAdded(tag)
                    tagInput = ""
                }
            )

            TagsList(tags = tags, onTagRemoved = onTagRemoved, spacing = spacing)
        }
    }
}

@Composable
private fun TagInputField(
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    onTagAdded: (String) -> Unit
) {
    OutlinedTextField(
        value = tagInput,
        onValueChange = onTagInputChange,
        label = { Text(stringResource(R.string.add_tag)) },
        placeholder = { Text(stringResource(R.string.enter_tag_name)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    if (tagInput.isNotBlank()) {
                        onTagAdded(tagInput.trim())
                    }
                },
                enabled = tagInput.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.add),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (tagInput.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun TagsList(
    tags: List<String>,
    onTagRemoved: (String) -> Unit,
    spacing: com.cpen321.usermanagement.ui.theme.Spacing
) {
    if (tags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(spacing.small))
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { onTagRemoved(tag) },
                    label = { Text(tag) }
                )
            }
        }
    }
}

@Composable
private fun FieldsEditSection(
    fields: List<FieldCreationData>,
    noteType: NoteType,
    onFieldAdded: (FieldType) -> Unit,
    onFieldRemoved: (String) -> Unit,
    onFieldUpdated: (String, FieldUpdate) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var showFieldTypeDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.fields),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showFieldTypeDialog = true },
                    modifier = Modifier.height(spacing.extraLarge)
                ) {
                    Text(stringResource(R.string.add_field))
                }
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            FieldsList(
                fields = fields,
                noteType = noteType,
                onFieldRemoved = onFieldRemoved,
                onFieldUpdated = onFieldUpdated,
                spacing = spacing
            )
        }
    }

    if (showFieldTypeDialog) {
        FieldTypeDialog(
            onDismiss = { showFieldTypeDialog = false },
            onTypeSelected = { type ->
                onFieldAdded(type)
                showFieldTypeDialog = false
            }
        )
    }
}

@Composable
private fun FieldEditCard(
    field: FieldCreationData,
    noteType: NoteType,
    onFieldRemoved: () -> Unit,
    onFieldUpdated: (FieldUpdate) -> Unit,
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
            modifier = Modifier.padding(spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = field.type.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onFieldRemoved) {
                    Icon(name = R.drawable.ic_arrow_back)
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            OutlinedTextField(
                value = field.label,
                onValueChange = { onFieldUpdated(FieldUpdate.Label(it)) },
                label = { Text(stringResource(R.string.label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.small))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = field.required,
                    onCheckedChange = { onFieldUpdated(FieldUpdate.Required(it)) }
                )
                Text(stringResource(R.string.required))
            }

            FieldContentInputSection(
                field = field,
                noteType = noteType,
                onFieldUpdated = onFieldUpdated,
                spacing = spacing
            )

            FieldConfigurationSection(
                field = field,
                onFieldUpdated = onFieldUpdated,
                spacing = spacing
            )
        }
    }
}

@Composable
private fun FieldContentInputSection(
    field: FieldCreationData,
    noteType: NoteType,
    onFieldUpdated: (FieldUpdate) -> Unit,
    spacing: com.cpen321.usermanagement.ui.theme.Spacing
) {
    if (noteType != NoteType.TEMPLATE) {
        Spacer(modifier = Modifier.height(spacing.medium))
        Text(
            text = stringResource(R.string.field_content),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(spacing.small))
        
        when (field.type) {
            FieldType.TEXT -> {
                OutlinedTextField(
                    value = (field.content as? String) ?: "",
                    onValueChange = { onFieldUpdated(FieldUpdate.Content(it)) },
                    label = { Text(stringResource(R.string.text_content)) },
                    placeholder = { Text(stringResource(R.string.enter_text_content)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
            FieldType.NUMBER -> {
                OutlinedTextField(
                    value = (field.content as? Int)?.toString() ?: "",
                    onValueChange = { 
                        val value = it.toIntOrNull()
                        onFieldUpdated(FieldUpdate.Content(value))
                    },
                    label = { Text(stringResource(R.string.number_content)) },
                    placeholder = { Text(stringResource(R.string.enter_number)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FieldType.DATETIME -> {
                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }
                val currentDateTime = (field.content as? java.time.LocalDateTime) ?: java.time.LocalDateTime.now()
                
                Column {
                    OutlinedTextField(
                        value = currentDateTime.toString(),
                        onValueChange = { 
                            try {
                                val dateTime = java.time.LocalDateTime.parse(it)
                                onFieldUpdated(FieldUpdate.Content(dateTime))
                            } catch (e: java.time.format.DateTimeParseException) {
                                // Invalid format, don't update
                            }
                        },
                        label = { Text(stringResource(R.string.datetime_content)) },
                        placeholder = { Text(stringResource(R.string.datetime_format)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    
                    Spacer(modifier = Modifier.height(spacing.small))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.pick_date))
                        }
                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.pick_time))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldConfigurationSection(
    field: FieldCreationData,
    onFieldUpdated: (FieldUpdate) -> Unit,
    spacing: com.cpen321.usermanagement.ui.theme.Spacing
) {
    Spacer(modifier = Modifier.height(spacing.medium))
    Text(
        text = stringResource(R.string.field_configuration),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(spacing.small))
    
    when (field.type) {
        FieldType.TEXT -> {
            OutlinedTextField(
                value = field.placeholder ?: "",
                onValueChange = { onFieldUpdated(FieldUpdate.Placeholder(it)) },
                label = { Text(stringResource(R.string.placeholder_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.small))
            OutlinedTextField(
                value = field.maxLength?.toString() ?: "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    onFieldUpdated(FieldUpdate.MaxLength(value))
                },
                label = { Text(stringResource(R.string.max_length_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        FieldType.NUMBER -> {
            OutlinedTextField(
                value = field.min?.toString() ?: "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    onFieldUpdated(FieldUpdate.Min(value))
                },
                label = { Text(stringResource(R.string.min_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.small))
            OutlinedTextField(
                value = field.max?.toString() ?: "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    onFieldUpdated(FieldUpdate.Max(value))
                },
                label = { Text(stringResource(R.string.max_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        FieldType.DATETIME -> {
            Text(
                text = stringResource(R.string.datetime_config_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FieldsList(
    fields: List<FieldCreationData>,
    noteType: NoteType,
    onFieldRemoved: (String) -> Unit,
    onFieldUpdated: (String, FieldUpdate) -> Unit,
    spacing: com.cpen321.usermanagement.ui.theme.Spacing
) {
    if (fields.isEmpty()) {
        Text(
            text = stringResource(R.string.no_fields_added_yet),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = spacing.medium)
        )
    } else {
        fields.forEach { field ->
            FieldEditCard(
                field = field,
                noteType = noteType,
                onFieldRemoved = { onFieldRemoved(field.id) },
                onFieldUpdated = { update -> onFieldUpdated(field.id, update) }
            )
            Spacer(modifier = Modifier.height(spacing.small))
        }
    }
}

@Composable
private fun FieldTypeDialog(
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