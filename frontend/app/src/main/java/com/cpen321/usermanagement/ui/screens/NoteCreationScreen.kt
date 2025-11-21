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
import com.cpen321.usermanagement.ui.viewmodels.NoteCreationState
import com.cpen321.usermanagement.ui.viewmodels.NoteCreationViewModel
import com.cpen321.usermanagement.utils.FeatureActions

data class NoteCreationCallbacks(
    val onBackClick: () -> Unit,
    val onTagAdded: (String) -> Unit,
    val onTagRemoved: (String) -> Unit,
    val onFieldAdded: (FieldType) -> Unit,
    val onFieldRemoved: (String) -> Unit,
    val onFieldUpdated: (String, FieldUpdate) -> Unit,
    val onCreateNote: () -> Unit
)

@Composable
fun NoteCreationScreen(
    noteCreationViewModel: NoteCreationViewModel,
    onBackClick: () -> Unit,
    featureActions: FeatureActions
) {
    val creationState by noteCreationViewModel.creationState.collectAsState()

    LaunchedEffect(creationState.isSuccess) {
        if (creationState.isSuccess) {
            onBackClick()
            if (creationState.noteType == NoteType.CONTENT){
                featureActions.navs.navigateToMainWithContext(
                    workspaceId = featureActions.state.getWorkspaceId(),
                    searchQuery = featureActions.state.getSearchQuery(),
                    selectedTags = featureActions.state.getSelectedTags(),
                    allTagsSelected = featureActions.state.getAllTagsSelected())
            }
            else{
                featureActions.navs.navigateToTemplate(
                    workspaceId = featureActions.state.getWorkspaceId(),
                    searchQuery = featureActions.state.getSearchQuery(),
                    selectedTags = featureActions.state.getSelectedTags(),
                    allTagsSelected = featureActions.state.getAllTagsSelected())
            }
        }
    }

    if(creationState.isLoadingTemplate) CircularProgressIndicator(
        modifier = Modifier.size(LocalSpacing.current.medium),
        color = MaterialTheme.colorScheme.onPrimary
    )
    else NoteCreationContent(
        creationState = creationState,
        callbacks = NoteCreationCallbacks(
            onBackClick = onBackClick,
            onTagAdded = noteCreationViewModel::addTag,
            onTagRemoved = noteCreationViewModel::removeTag,
            onFieldAdded = noteCreationViewModel::addField,
            onFieldRemoved = noteCreationViewModel::removeField,
            onFieldUpdated = noteCreationViewModel::updateField,
            onCreateNote = { noteCreationViewModel.createNote(featureActions.state.getWorkspaceId()) }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationContent(
    creationState: NoteCreationState,
    callbacks: NoteCreationCallbacks,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { NoteCreationTopBar(onBackClick = callbacks.onBackClick) },
        bottomBar = { NoteCreationBottomBar(
            onBackClick = callbacks.onBackClick,
            onCreateNote = callbacks.onCreateNote,
            isCreating = creationState.isCreating
        ) }
    ) { paddingValues ->
        NoteCreationBody(
            creationState = creationState,
            paddingValues = paddingValues,
            callbacks = callbacks
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCreationTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.create_note),
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
}

@Composable
private fun NoteCreationBottomBar(
    onBackClick: () -> Unit,
    onCreateNote: () -> Unit,
    isCreating: Boolean
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
                onClick = onCreateNote,
                modifier = Modifier.weight(1f),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(spacing.medium),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.create))
                }
            }
        }
    }
}

@Composable
fun NoteCreationBody(
    creationState: NoteCreationState,
    paddingValues: PaddingValues,
    callbacks: NoteCreationCallbacks,
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
        )

        Spacer(modifier = Modifier.height(spacing.large))

        TagsInputSection(
            tags = creationState.tags,
            onTagAdded = callbacks.onTagAdded,
            onTagRemoved = callbacks.onTagRemoved
        )

        Spacer(modifier = Modifier.height(spacing.large))

        // Fields Section
        FieldsEditSection(
            fields = creationState.fields,
            onFieldAdded = callbacks.onFieldAdded,
            onFieldRemoved = callbacks.onFieldRemoved,
            onFieldUpdated = callbacks.onFieldUpdated
        )
    }
}

@Composable
private fun NoteTypeSection(
    selectedType: NoteType,
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
                        enabled = false,
                        onClick = { },
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