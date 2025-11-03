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
import androidx.compose.ui.res.stringResource
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.NoteViewModel
import com.cpen321.usermanagement.ui.viewmodels.NoteState
import com.cpen321.usermanagement.utils.IFeatureActions
import java.time.format.DateTimeFormatter

@Composable
fun NoteScreen(
    noteViewModel: NoteViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
){
    val noteState by noteViewModel.noteState.collectAsState()

    LaunchedEffect(Unit) {
        noteViewModel.loadNote(featureActions.getNoteId())
    }

    // Handle successful deletion
    LaunchedEffect(noteState.isDeleted) {
        if (noteState.isDeleted) {
            onBackClick()
        }
    }


    NoteContent(
        noteState = noteState,
        onBackClick = onBackClick,
        onEditClick = { featureActions.navigateToNoteEdit(featureActions.getNoteId()) },
        onDeleteClick = { noteViewModel.deleteNote(featureActions.getNoteId()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteContent(
    noteState: NoteState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.note_details),
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
                    IconButton(onClick = onEditClick) {
                        Icon(name = R.drawable.ic_edit)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(name = R.drawable.ic_delete_forever)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            noteState.isLoading || noteState.isDeleting -> {
                LoadingContent(paddingValues)
            }
            noteState.error != null -> {
                ErrorContent(
                    error = noteState.error,
                    paddingValues = paddingValues,
                    onBackClick = onBackClick
                )
            }
            noteState.note != null -> {
                NoteBody(
                    note = noteState.note,
                    paddingValues = paddingValues
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_note)) },
            text = { Text(stringResource(R.string.delete_note_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun LoadingContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
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

@Composable
fun NoteBody(
    note: Note,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val fontSizes = LocalFontSizes.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(spacing.medium)
            .verticalScroll(scrollState)
    ) {
        // Note Type Badge
        NoteTypeBadge(noteType = note.noteType)

        Spacer(modifier = Modifier.height(spacing.medium))

        // Note ID
        InfoCard(
            title = stringResource(R.string.note_id),
            content = note._id
        )

        Spacer(modifier = Modifier.height(spacing.small))

        // Creation Date
        InfoCard(
            title = stringResource(R.string.created),
            content = try {
                java.time.Instant.parse(note.createdAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
            } catch (e: java.time.format.DateTimeParseException) {
                note.createdAt
            }
        )

        Spacer(modifier = Modifier.height(spacing.small))

        // Last Edit Date
        InfoCard(
            title = stringResource(R.string.last_edited),
            content = try {
                java.time.Instant.parse(note.updatedAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
            } catch (e: java.time.format.DateTimeParseException) {
                note.updatedAt
            }
        )

        Spacer(modifier = Modifier.height(spacing.small))

        // Tags
        if (note.tags.isNotEmpty()) {
            TagsSection(tags = note.tags)
            Spacer(modifier = Modifier.height(spacing.small))
        }

        // Fields
        if (note.fields.isNotEmpty()) {
            Spacer(modifier = Modifier.height(spacing.medium))
            Text(
                text = stringResource(R.string.fields),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.small))

            note.fields.forEach { field ->
                FieldCard(field = field)
                Spacer(modifier = Modifier.height(spacing.small))
            }
        }
    }
}

@Composable
private fun NoteTypeBadge(
    noteType: NoteType,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = when (noteType) {
            NoteType.CONTENT -> MaterialTheme.colorScheme.primaryContainer
            NoteType.CHAT -> MaterialTheme.colorScheme.secondaryContainer
            NoteType.TEMPLATE -> MaterialTheme.colorScheme.tertiaryContainer
        }
    ) {
        Text(
            text = noteType.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = when (noteType) {
                NoteType.CONTENT -> MaterialTheme.colorScheme.onPrimaryContainer
                NoteType.CHAT -> MaterialTheme.colorScheme.onSecondaryContainer
                NoteType.TEMPLATE -> MaterialTheme.colorScheme.onTertiaryContainer
            },
            modifier = Modifier.padding(
                horizontal = spacing.medium,
                vertical = spacing.small
            )
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: String,
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
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TagsSection(
    tags: List<String>,
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
        Text(
            text = stringResource(R.string.tags),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(spacing.small))
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                tags.forEach { tag ->
                    TagChip(tag = tag)
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(
                horizontal = spacing.small,
                vertical = spacing.extraSmall
            )
        )
    }
}

@Composable
private fun FieldCard(
    field: Field,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = spacing.extraSmall)
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (field.required) {
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text(
                        text = "*",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            // Display field-specific information
            when (field) {
                is TextField -> {
                    TextFieldInfo(field = field)
                }
                is DateTimeField -> {
                    DateTimeFieldInfo(field = field)
                }
                is NumberField -> {
                    NumberFieldInfo(field = field)
                }
            }
        }
    }
}

@Composable
private fun TextFieldInfo(
    field: TextField,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(modifier = modifier) {
        // Display content if available
        if (field.content != null && field.content.isNotBlank()) {
            Text(
                text = field.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = stringResource(R.string.no_content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = stringResource(R.string.type_text_field),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        field.placeholder?.let { placeholder ->
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = stringResource(R.string.placeholder_label, placeholder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        field.maxLength?.let { maxLength ->
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = stringResource(R.string.max_length_label, maxLength),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DateTimeFieldInfo(
    field: DateTimeField,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")

    Column(modifier = modifier) {
        // Display content if available
        field.content?.let { content ->
            Text(
                text = content.format(formatter),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } ?: run {
            Text(
                text = stringResource(R.string.no_date_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = stringResource(R.string.type_datetime_field),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        field.minDate?.let { minDate ->
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = stringResource(R.string.min_date_label, minDate.format(formatter)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        field.maxDate?.let { maxDate ->
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = stringResource(R.string.max_date_label, maxDate.format(formatter)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NumberFieldInfo(
    field: NumberField,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(modifier = modifier) {
        // Display content if available
        field.content?.let { content ->
            Text(
                text = content.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        } ?: run {
            Text(
                text = stringResource(R.string.no_value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = stringResource(R.string.type_number_field),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        field.min?.let { min ->
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = stringResource(R.string.min_optional, min),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        field.max?.let { max ->
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = stringResource(R.string.max_optional, max),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}