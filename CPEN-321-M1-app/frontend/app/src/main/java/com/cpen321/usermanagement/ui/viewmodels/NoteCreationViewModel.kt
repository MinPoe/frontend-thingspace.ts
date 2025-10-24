package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.NoteRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import android.util.Log
enum class FieldType {
    TEXT,
    NUMBER,
    DATETIME
}

data class FieldCreationData(
    val id: String = UUID.randomUUID().toString(),
    val type: FieldType,
    val label: String = "",
    val required: Boolean = false,
    val placeholder: String? = null,
    val maxLength: Int? = null,
    val min: Int? = null,
    val max: Int? = null
)

sealed class FieldUpdate {
    data class Label(val value: String) : FieldUpdate()
    data class Required(val value: Boolean) : FieldUpdate()
    data class Placeholder(val value: String) : FieldUpdate()
    data class MaxLength(val value: Int?) : FieldUpdate()
    data class Min(val value: Int?) : FieldUpdate()
    data class Max(val value: Int?) : FieldUpdate()
}

data class NoteCreationState(
    val noteType: NoteType = NoteType.CONTENT,
    val tags: List<String> = emptyList(),
    val fields: List<FieldCreationData> = emptyList(),
    val isCreating: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class NoteCreationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    private val _creationState = MutableStateFlow(NoteCreationState())
    val creationState: StateFlow<NoteCreationState> = _creationState.asStateFlow()

    fun setNoteType(noteType: NoteType) {
        _creationState.value = _creationState.value.copy(noteType = noteType)
    }

    fun addTag(tag: String) {
        val currentTags = _creationState.value.tags
        if (tag.isNotBlank() && tag !in currentTags) {
            _creationState.value = _creationState.value.copy(
                tags = currentTags + tag
            )
        }
    }

    fun removeTag(tag: String) {
        _creationState.value = _creationState.value.copy(
            tags = _creationState.value.tags - tag
        )
    }

    fun addField(fieldType: FieldType) {
        val newField = FieldCreationData(
            type = fieldType,
            label = "New ${fieldType.name.lowercase()} field"
        )
        _creationState.value = _creationState.value.copy(
            fields = _creationState.value.fields + newField
        )
    }

    fun removeField(fieldId: String) {
        _creationState.value = _creationState.value.copy(
            fields = _creationState.value.fields.filter { it.id != fieldId }
        )
    }

    fun updateField(fieldId: String, update: FieldUpdate) {
        _creationState.value = _creationState.value.copy(
            fields = _creationState.value.fields.map { field ->
                if (field.id == fieldId) {
                    when (update) {
                        is FieldUpdate.Label -> field.copy(label = update.value)
                        is FieldUpdate.Required -> field.copy(required = update.value)
                        is FieldUpdate.Placeholder -> field.copy(placeholder = update.value)
                        is FieldUpdate.MaxLength -> field.copy(maxLength = update.value)
                        is FieldUpdate.Min -> field.copy(min = update.value)
                        is FieldUpdate.Max -> field.copy(max = update.value)
                    }
                } else {
                    field
                }
            }
        )
    }

    fun createNote(workspaceId: String) {
        viewModelScope.launch {
            Log.d("NoteCreation", "Creating note with workspaceId: '$workspaceId'")

            _creationState.value = _creationState.value.copy(
                isCreating = true,
                error = null
            )

            // Validate
            if (_creationState.value.fields.isEmpty()) {
                _creationState.value = _creationState.value.copy(
                    isCreating = false,
                    error = "Please add at least one field"
                )
                return@launch
            }

            // Check if all fields have labels
            val hasEmptyLabel = _creationState.value.fields.any { it.label.isBlank() }
            if (hasEmptyLabel) {
                _creationState.value = _creationState.value.copy(
                    isCreating = false,
                    error = "All fields must have a label"
                )
                return@launch
            }

            // Convert FieldCreationData to Field
            val fields = _creationState.value.fields.map { fieldData ->
                when (fieldData.type) {
                    FieldType.TEXT -> TextField(
                        _id = fieldData.id,
                        label = fieldData.label,
                        required = fieldData.required,
                        placeholder = fieldData.placeholder,
                        maxLength = fieldData.maxLength
                    )
                    FieldType.NUMBER -> NumberField(
                        _id = fieldData.id,
                        label = fieldData.label,
                        required = fieldData.required,
                        min = fieldData.min,
                        max = fieldData.max
                    )
                    FieldType.DATETIME -> DateTimeField(
                        _id = fieldData.id,
                        label = fieldData.label,
                        required = fieldData.required,
                        minDate = null,
                        maxDate = null
                    )
                }
            }

            // Get current user ID
            val user = authRepository.getCurrentUser()
            val userId = user?._id ?: run {
                _creationState.value = _creationState.value.copy(
                    isCreating = false,
                    error = "User not authenticated"
                )
                return@launch
            }

            // Create note
            val result = noteRepository.createNote(
                workspaceId = workspaceId,
                authorId = userId,
                tags = _creationState.value.tags,
                fields = fields,
                noteType = _creationState.value.noteType
            )

            result.fold(
                onSuccess = {
                    _creationState.value = _creationState.value.copy(
                        isCreating = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _creationState.value = _creationState.value.copy(
                        isCreating = false,
                        error = exception.message ?: "Failed to create note"
                    )
                }
            )
        }
    }

    fun reset() {
        _creationState.value = NoteCreationState()
    }
}