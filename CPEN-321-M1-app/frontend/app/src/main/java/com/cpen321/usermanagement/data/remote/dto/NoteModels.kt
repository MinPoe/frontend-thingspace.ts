package com.cpen321.usermanagement.data.remote.dto
import kotlinx.datetime.*;

/*
 * Note and Field data classes
 */

data class Note(
    val _id: String,
    val dateCreation: LocalDateTime,
    val dateLastEdit: LocalDateTime,
    val tags: ArrayList<String>,
    val noteType: NoteType,
    val fields: List<Field>,
)

// Field Types implemented here:
sealed class Field {
    abstract val _id: String
    abstract val label: String
    abstract val required: Boolean
}

// TextField, DateTimeField, NumberField extend the Field interface
// ? = null makes some parts nullable (can be empty)
data class TextField(
    override val _id: String,
    override val label: String,
    override val required: Boolean = false,
    val placeholder: String? = null,
    val maxLength: Int? = null
) : Field()

data class DateTimeField(
    override val _id: String,
    override val label: String,
    override val required: Boolean = false,
    val minDate: LocalDateTime? = null,
    val maxDate: LocalDateTime? = null
) : Field()

data class NumberField(
    override val _id: String,
    override val label: String,
    override val required: Boolean = false,
    val min: Int? = null,
    val max: Int? = null,
): Field()

// TODO: ADD MORE ENUMS FOR NOTETYPE LATER
enum class NoteType {
    CONTENT,
    CHAT,
    TEMPLATE
}