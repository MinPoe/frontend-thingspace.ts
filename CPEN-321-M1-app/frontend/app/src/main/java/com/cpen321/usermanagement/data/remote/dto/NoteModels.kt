package com.cpen321.usermanagement.data.remote.dto
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/*
 * Note and Field data classes
 */

data class Note(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    val tags: List<String> = emptyList(),
    val noteType: NoteType,
    val fields: List<Field> = emptyList(),
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