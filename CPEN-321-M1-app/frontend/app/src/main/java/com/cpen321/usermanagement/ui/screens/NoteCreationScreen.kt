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
import androidx.compose.ui.text.font.FontWeight
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.FieldCreationData
import com.cpen321.usermanagement.ui.viewmodels.FieldType
import com.cpen321.usermanagement.ui.viewmodels.FieldUpdate
import com.cpen321.usermanagement.ui.viewmodels.NoteCreationState
import com.cpen321.usermanagement.ui.viewmodels.NoteCreationViewModel
import com.cpen321.usermanagement.utils.IFeatureActions

@Composable
fun NoteCreationScreen(
    noteCreationViewModel: NoteCreationViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
) {
    val creationState by noteCreationViewModel.creationState.collectAsState()

    LaunchedEffect(creationState.isSuccess) {
        if (creationState.isSuccess) {
            noteCreationViewModel.reset()
            onBackClick()
        }
    }

    NoteCreationContent(
        creationState = creationState,
        onBackClick = onBackClick,
        onTagAdded = noteCreationViewModel::addTag,
        onTagRemoved = noteCreationViewModel::removeTag,
        onFieldAdded = noteCreationViewModel::addField,
        onFieldRemoved = noteCreationViewModel::removeField,
        onFieldUpdated = noteCreationViewModel::updateField,
        onNoteTypeChanged = noteCreationViewModel::setNoteType,
        onCreateNote = { noteCreationViewModel.createNote(featureActions.getWorkspaceId()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationContent(
    creationState: NoteCreationState,
    onBackClick: () -> Unit,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onFieldAdded: (FieldType) -> Unit,
    onFieldRemoved: (String) -> Unit,
    onFieldUpdated: (String, FieldUpdate) -> Unit,
    onNoteTypeChanged: (NoteType) -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Note",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(name = R.drawable.ic_arrow_back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
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
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(spacing.medium))
                    Button(
                        onClick = onCreateNote,
                        modifier = Modifier.weight(1f),
                        enabled = !creationState.isCreating
                    ) {
                        if (creationState.isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(spacing.medium),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NoteCreationBody(
            creationState = creationState,
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
fun NoteCreationBody(
    creationState: NoteCreationState,
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
        if (creationState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = creationState.error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(spacing.medium)
                )
            }
            Spacer(modifier = Modifier.height(spacing.medium))
        }

        // Note Type Selection
        NoteTypeSection(
            selectedType = creationState.noteType,
            onTypeChanged = onNoteTypeChanged
        )

        Spacer(modifier = Modifier.height(spacing.large))

        // Tags Section
        TagsInputSection(
            tags = creationState.tags,
            onTagAdded = onTagAdded,
            onTagRemoved = onTagRemoved
        )

        Spacer(modifier = Modifier.height(spacing.large))

        // Fields Section
        FieldsSection(
            fields = creationState.fields,
            noteType = creationState.noteType,
            onFieldAdded = onFieldAdded,
            onFieldRemoved = onFieldRemoved,
            onFieldUpdated = onFieldUpdated
        )
    }
}

@Composable
private fun NoteTypeSection(
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
                text = "Note Type",
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
private fun TagsInputSection(
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
                text = "Tags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.small))

            OutlinedTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                label = { Text("Add tag") },
                placeholder = { Text("Enter tag name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (tagInput.isNotBlank()) {
                                onTagAdded(tagInput.trim())
                                tagInput = ""
                            }
                        },
                        enabled = tagInput.isNotBlank()
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (tagInput.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

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
    }
}

@Composable
private fun FieldsSection(
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
                    text = "Fields",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showFieldTypeDialog = true },
                    modifier = Modifier.height(spacing.extraLarge)
                ) {
                    Text("Add Field")
                }
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            if (fields.isEmpty()) {
                Text(
                    text = "No fields added yet. Click 'Add Field' to create one.",
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
                label = { Text("Label") },
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
                Text("Required")
            }

            // Content input section - only show for CONTENT and CHAT note types
            if (noteType != NoteType.TEMPLATE) {
                Spacer(modifier = Modifier.height(spacing.medium))
                Text(
                    text = "Field Content",
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
                            label = { Text("Text Content") },
                            placeholder = { Text("Enter text content...") },
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
                            label = { Text("Number Content") },
                            placeholder = { Text("Enter number...") },
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
                                    } catch (e: Exception) {
                                        // Invalid format, don't update
                                    }
                                },
                                label = { Text("Date/Time Content") },
                                placeholder = { Text("YYYY-MM-DDTHH:mm:ss") },
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
                                    Text("Pick Date")
                                }
                                Button(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pick Time")
                                }
                            }
                        }
                    }
                }
            }

            // Type-specific configuration fields
            Spacer(modifier = Modifier.height(spacing.medium))
            Text(
                text = "Field Configuration",
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
                        label = { Text("Placeholder (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(spacing.small))
                    OutlinedTextField(
                        value = field.maxLength?.toString() ?: "",
                        onValueChange = {
                            val value = it.toIntOrNull()
                            onFieldUpdated(FieldUpdate.MaxLength(value))
                        },
                        label = { Text("Max Length (optional)") },
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
                        label = { Text("Min (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(spacing.small))
                    OutlinedTextField(
                        value = field.max?.toString() ?: "",
                        onValueChange = {
                            val value = it.toIntOrNull()
                            onFieldUpdated(FieldUpdate.Max(value))
                        },
                        label = { Text("Max (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FieldType.DATETIME -> {
                    Text(
                        text = "Date/Time field configuration options coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
        title = { Text("Select Field Type") },
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
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}