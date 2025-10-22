package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.data.repository.NoteRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditState(
    val noteType: NoteType = NoteType.CONTENT,
    val tags: List<String> = emptyList(),
    val fields: List<FieldCreationData> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val loadError: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    private val _editState = MutableStateFlow(NoteEditState())
    val editState: StateFlow<NoteEditState> = _editState.asStateFlow()

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _editState.value = _editState.value.copy(isLoading = true, loadError = null)

            val result = noteRepository.getNote(noteId)

            result.fold(
                onSuccess = { note ->
                    // Convert Note fields to FieldCreationData
                    val fieldCreationData = note.fields.map { field ->
                        when (field) {
                            is TextField -> FieldCreationData(
                                id = field._id,
                                type = FieldType.TEXT,
                                label = field.label,
                                required = field.required,
                                placeholder = field.placeholder,
                                maxLength = field.maxLength
                            )
                            is NumberField -> FieldCreationData(
                                id = field._id,
                                type = FieldType.NUMBER,
                                label = field.label,
                                required = field.required,
                                min = field.min,
                                max = field.max
                            )
                            is DateTimeField -> FieldCreationData(
                                id = field._id,
                                type = FieldType.DATETIME,
                                label = field.label,
                                required = field.required
                            )
                        }
                    }

                    _editState.value = NoteEditState(
                        noteType = note.noteType,
                        tags = note.tags,
                        fields = fieldCreationData,
                        isLoading = false,
                        loadError = null
                    )
                },
                onFailure = { exception ->
                    _editState.value = _editState.value.copy(
                        isLoading = false,
                        loadError = exception.message ?: "Failed to load note"
                    )
                }
            )
        }
    }

    fun setNoteType(noteType: NoteType) {
        _editState.value = _editState.value.copy(noteType = noteType)
    }

    fun addTag(tag: String) {
        val currentTags = _editState.value.tags
        if (tag.isNotBlank() && tag !in currentTags) {
            _editState.value = _editState.value.copy(
                tags = currentTags + tag
            )
        }
    }

    fun removeTag(tag: String) {
        _editState.value = _editState.value.copy(
            tags = _editState.value.tags - tag
        )
    }

    fun addField(fieldType: FieldType) {
        val newField = FieldCreationData(
            type = fieldType,
            label = "New ${fieldType.name.lowercase()} field"
        )
        _editState.value = _editState.value.copy(
            fields = _editState.value.fields + newField
        )
    }

    fun removeField(fieldId: String) {
        _editState.value = _editState.value.copy(
            fields = _editState.value.fields.filter { it.id != fieldId }
        )
    }

    fun updateField(fieldId: String, update: FieldUpdate) {
        _editState.value = _editState.value.copy(
            fields = _editState.value.fields.map { field ->
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

    fun saveNote(noteId: String) {
        viewModelScope.launch {
            _editState.value = _editState.value.copy(
                isSaving = true,
                error = null
            )

            // Validate
            if (_editState.value.fields.isEmpty()) {
                _editState.value = _editState.value.copy(
                    isSaving = false,
                    error = "Please add at least one field"
                )
                return@launch
            }

            // Check if all fields have labels
            val hasEmptyLabel = _editState.value.fields.any { it.label.isBlank() }
            if (hasEmptyLabel) {
                _editState.value = _editState.value.copy(
                    isSaving = false,
                    error = "All fields must have a label"
                )
                return@launch
            }

            // Convert FieldCreationData to Field
            val fields = _editState.value.fields.map { fieldData ->
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

            // Update note
            val result = noteRepository.updateNote(
                noteId = noteId,
                tags = _editState.value.tags,
                fields = fields
            )

            result.fold(
                onSuccess = {
                    _editState.value = _editState.value.copy(
                        isSaving = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _editState.value = _editState.value.copy(
                        isSaving = false,
                        error = exception.message ?: "Failed to save note"
                    )
                }
            )
        }
    }

    fun reset() {
        _editState.value = NoteEditState()
    }
}