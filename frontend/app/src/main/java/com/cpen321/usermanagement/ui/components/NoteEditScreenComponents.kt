package com.cpen321.usermanagement.ui.components

import Button
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.theme.Spacing
import com.cpen321.usermanagement.ui.viewmodels.FieldCreationData
import com.cpen321.usermanagement.ui.viewmodels.FieldType
import com.cpen321.usermanagement.ui.viewmodels.FieldUpdate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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
        Spacer(modifier = Modifier.Companion.height(spacing.small))

        // Display tags
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.Companion.fillMaxWidth(),
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

        Spacer(modifier = Modifier.Companion.height(spacing.small))
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
                    modifier = Modifier.Companion.fillMaxWidth()
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
    noteType: NoteType,
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
        Spacer(modifier = Modifier.Companion.height(spacing.small))

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
                    currentUser = currentUser,
                    noteType = noteType
                )
                Spacer(modifier = Modifier.Companion.height(spacing.medium))
            }
        }

        Spacer(modifier = Modifier.Companion.height(spacing.small))
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
    noteType: NoteType,
    onFieldRemoved: () -> Unit,
    onFieldUpdated: (FieldUpdate) -> Unit
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.Companion.padding(spacing.medium)) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                OutlinedTextField(
                    value = field.label,
                    onValueChange = { onFieldUpdated(FieldUpdate.Label(it)) },
                    label = { Text(stringResource(R.string.label)) },
                    modifier = Modifier.Companion.fillMaxWidth(.8f)
                )
                IconButton(onClick = onFieldRemoved) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            Spacer(modifier = Modifier.Companion.height(spacing.small))

            if (noteType == NoteType.CONTENT) {
                when (field.type) {
                    FieldType.TEXT -> TextFieldInput(field, onFieldUpdated)
                    FieldType.DATETIME -> DateTimeFieldInput(field, onFieldUpdated, spacing)
                    FieldType.SIGNATURE -> SignatureFieldInput(field, currentUser, onFieldUpdated)
                }
            } else {
                when (field.type) {
                    FieldType.TEXT -> Text(stringResource(R.string.text_template))
                    FieldType.DATETIME -> Text(stringResource(R.string.datetime_template))
                    FieldType.SIGNATURE -> Text(stringResource(R.string.signature_template))
                }
            }
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
        modifier = Modifier.Companion.fillMaxWidth(),
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
        verticalAlignment = Alignment.Companion.CenterVertically,
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.Companion.testTag(stringResource(R.string.all))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.Companion.padding(start = 8.dp)
        )
    }
}


@Composable
private fun DateTimeFieldInput(
    field: FieldCreationData,
    onFieldUpdated: (FieldUpdate) -> Unit,
    spacing: Spacing
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
        val calendar = CalendarDateHelper(selectedDateTime)

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

    DateTimeFieldLayout(displayText, spacing, showDatePicker, showTimePicker)
}

private fun CalendarDateHelper(selectedDateTime:LocalDateTime): Calendar {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedDateTime.year)
        set(Calendar.MONTH, selectedDateTime.monthValue - 1) // Calendar months are 0-indexed
        set(Calendar.DAY_OF_MONTH, selectedDateTime.dayOfMonth)
    }
}

@Composable
private fun DateTimeFieldLayout(
    displayText:String,
    spacing: Spacing,
    showDatePicker:()->Unit,
    showTimePicker:()->Unit){
    Column {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(stringResource(R.string.datetime_content)) },
            modifier = Modifier.Companion.fillMaxWidth(),
            readOnly = true
        )
        Spacer(modifier = Modifier.Companion.height(spacing.small))
        Row {
            Button(
                onClick = showDatePicker,
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(stringResource(R.string.pick_date))
            }
            Spacer(modifier = Modifier.Companion.width(spacing.small))
            Button(
                onClick = showTimePicker,
                modifier = Modifier.Companion.weight(1f)
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
    modifier: Modifier = Modifier.Companion
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_field_type)) },
        text = {
            Column {
                FieldType.values().forEach { type ->
                    TextButton(
                        onClick = { onFieldTypeSelected(type) },
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text(
                            text = type.name,
                            modifier = Modifier.Companion.fillMaxWidth()
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