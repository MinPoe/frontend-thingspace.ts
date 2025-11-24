package com.cpen321.usermanagement.ui.screens

import Button
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.FieldCreationData
import com.cpen321.usermanagement.ui.viewmodels.FieldType
import com.cpen321.usermanagement.ui.viewmodels.FieldUpdate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TagsEditSection(
    tags: List<String>,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit
) {
    val spacing = LocalSpacing.current
    var showDialog by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }

    Column {
        Text(
            text = stringResource(R.string.tags),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(spacing.small))
        
        // Display tags
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { onTagRemoved(tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.small))
        Button(onClick = { showDialog = true }) {
            Text(stringResource(R.string.add_tag))
        }
    }

    AddTagDialog(
        showDialog = showDialog,
        tagInput = tagInput,
        onTagInputChange = { tagInput = it },
        onDismiss = { showDialog = false },
        onConfirm = {
            if (tagInput.isNotBlank()) {
                onTagAdded(tagInput.trim())
                tagInput = ""
                showDialog = false
            }
        }
    )
}

@Composable
private fun AddTagDialog(
    showDialog: Boolean,
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.add_tag)) },
            text = {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = onTagInputChange,
                    label = { Text(stringResource(R.string.enter_tag_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text(stringResource(R.string.add))
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

@Composable
fun FieldsEditSection(
    fields: List<FieldCreationData>,
    currentUser: User?,
    onFieldAdded: (FieldType) -> Unit,
    onFieldRemoved: (String) -> Unit,
    onFieldUpdated: (String, FieldUpdate) -> Unit
) {
    val spacing = LocalSpacing.current
    var showFieldDialog by remember { mutableStateOf(false) }

    Column {
        Text(
            text = stringResource(R.string.fields),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(spacing.small))

        if (fields.isEmpty()) {
            Text(
                text = stringResource(R.string.no_fields_added),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            fields.forEach { field ->
                FieldEditor(
                    field = field,
                    onFieldRemoved = { onFieldRemoved(field.id) },
                    onFieldUpdated = { update -> onFieldUpdated(field.id, update) },
                    currentUser = currentUser
                )
                Spacer(modifier = Modifier.height(spacing.medium))
            }
        }

        Spacer(modifier = Modifier.height(spacing.small))
        Button(onClick = { showFieldDialog = true }) {
            Text(stringResource(R.string.add_field))
        }
    }

    if (showFieldDialog) {
        FieldTypeSelectionDialog(
            onDismiss = { showFieldDialog = false },
            onFieldTypeSelected = { fieldType ->
                onFieldAdded(fieldType)
                showFieldDialog = false
            }
        )
    }
}

@Composable
private fun FieldEditor(
    field: FieldCreationData,
    currentUser: User?,
    onFieldRemoved: () -> Unit,
    onFieldUpdated: (FieldUpdate) -> Unit
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = field.label.ifEmpty { stringResource(R.string.field_content) },
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onFieldRemoved) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            when (field.type) {
                FieldType.TEXT -> TextFieldInput(field, onFieldUpdated)
                FieldType.DATETIME -> DateTimeFieldInput(field, onFieldUpdated, spacing)
                FieldType.SIGNATURE -> SignatureFieldInput(field, currentUser, onFieldUpdated) //TODO: update later
            }

            Spacer(modifier = Modifier.height(spacing.small))

            OutlinedTextField(
                value = field.label,
                onValueChange = { onFieldUpdated(FieldUpdate.Label(it)) },
                label = { Text(stringResource(R.string.label)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TextFieldInput(
    field: FieldCreationData,
    onFieldUpdated: (FieldUpdate) -> Unit
) {
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

@Composable
private fun SignatureFieldInput(
    field: FieldCreationData,
    currentUser: User?,
    onFieldUpdated: (FieldUpdate) -> Unit
) {
    var isFieldChecked by remember(field.userId) { mutableStateOf(field.userId != null) }

    /* Mechanics of the field:
    * 1) If the field is not checked everyone can check it
    * 2) If they do so, their name appears next to the checkbox and only they can un-check the field
    * */

    val onCheckedChange = {checked: Boolean ->
        Log.d("signature", "user: ${currentUser?.profile?.name}")
        isFieldChecked = checked
        if (checked) {
            onFieldUpdated(
                FieldUpdate.Signature(
                    userId = currentUser?._id,
                    placeholder = currentUser?.profile?.name
                )
            )
        }
        else{
            onFieldUpdated(
                FieldUpdate.Signature(
                    userId = null,
                    placeholder = null
                )
            )
        }
    }
    SignatureCheckbox(
        isChecked = isFieldChecked,
        enabled = (field.userId == currentUser?._id) || (!isFieldChecked),
        text = field.placeholder ?: stringResource(R.string.signature),
        onCheckedChange = onCheckedChange
    )
    Log.d("signature", "isFieldChecked: $isFieldChecked")
}

@Composable
private fun SignatureCheckbox(
    isChecked: Boolean,
    enabled: Boolean,
    text: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Log.d("signature", "enabled: $enabled")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag(stringResource(R.string.all))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}


@Composable
private fun DateTimeFieldInput(
    field: FieldCreationData,
    onFieldUpdated: (FieldUpdate) -> Unit,
    spacing: com.cpen321.usermanagement.ui.theme.Spacing
) {
    val context = LocalContext.current
    val currentDateTime = (field.content as? LocalDateTime) ?: LocalDateTime.now()
    
    // Track the selected datetime - sync with field content
    var selectedDateTime by remember(field.content) {
        mutableStateOf(currentDateTime)
    }
    
    // Update selectedDateTime when field content changes externally
    LaunchedEffect(field.content) {
        if (field.content is LocalDateTime) {
            selectedDateTime = field.content
        }
    }
    
    // Format the datetime for display
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val displayText = selectedDateTime.format(dateTimeFormatter)
    
    // Date picker dialog - create new instance when showing to ensure current values
    val showDatePicker: () -> Unit = {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDateTime.year)
            set(Calendar.MONTH, selectedDateTime.monthValue - 1) // Calendar months are 0-indexed
            set(Calendar.DAY_OF_MONTH, selectedDateTime.dayOfMonth)
        }
        
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Update the selected datetime with new date, keeping the time
                val newDateTime = LocalDateTime.of(
                    year,
                    month + 1, // LocalDateTime months are 1-indexed
                    dayOfMonth,
                    selectedDateTime.hour,
                    selectedDateTime.minute
                )
                selectedDateTime = newDateTime
                onFieldUpdated(FieldUpdate.Content(newDateTime))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    // Time picker dialog - create new instance when showing to ensure current values
    val showTimePicker: () -> Unit = {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedDateTime.hour)
            set(Calendar.MINUTE, selectedDateTime.minute)
        }
        
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Update the selected datetime with new time, keeping the date
                val newDateTime = LocalDateTime.of(
                    selectedDateTime.year,
                    selectedDateTime.monthValue,
                    selectedDateTime.dayOfMonth,
                    hourOfDay,
                    minute
                )
                selectedDateTime = newDateTime
                onFieldUpdated(FieldUpdate.Content(newDateTime))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // 24-hour format
        ).show()
    }

    Column {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(stringResource(R.string.datetime_content)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        Spacer(modifier = Modifier.height(spacing.small))
        Row {
            Button(
                onClick = showDatePicker,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pick_date))
            }
            Spacer(modifier = Modifier.width(spacing.small))
            Button(
                onClick = showTimePicker,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pick_time))
            }
        }
    }
}

@Composable
private fun FieldTypeSelectionDialog(
    onDismiss: () -> Unit,
    onFieldTypeSelected: (FieldType) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_field_type)) },
        text = {
            Column {
                FieldType.values().forEach { type ->
                    TextButton(
                        onClick = { onFieldTypeSelected(type) },
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
    when {
        isLoadingWorkspaces -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        workspacePairs.isEmpty() -> {
            Text(stringResource(R.string.create_new_workspace))
        }
        else -> {
            Column {
                Text(stringResource(R.string.select_workspace))
                Spacer(modifier = Modifier.height(8.dp))
                workspacePairs.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isProcessing) { onWorkspaceSelected(id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedWorkspaceId == id,
                            onClick = { onWorkspaceSelected(id) },
                            enabled = !isProcessing
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceSelectionConfirmButton(
    onConfirm: () -> Unit,
    confirmText: String,
    isProcessing: Boolean,
    isEnabled: Boolean
) {
    val spacing = LocalSpacing.current
    
    Button(
        onClick = onConfirm,
        enabled = isEnabled
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.medium),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(confirmText)
        }
    }
}