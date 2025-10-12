package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.data.remote.dto.TextField
import com.cpen321.usermanagement.data.remote.dto.Field

/**
 * !!! MOCK IMPL 4 NOW !!!
 * **/
import java.time.LocalDateTime

class NoteRepositoryImpl : NoteRepository {

    override fun getNote(noteId: String): Result<Note> {
        val note = Note(
            _id = noteId,
            dateCreation = LocalDateTime.now().minusDays(noteId.length.toLong()),
            dateLastEdit = LocalDateTime.now(),
            tags = arrayListOf("mock", "note_$noteId"),
            noteType = NoteType.CONTENT,
            fields = listOf(TextField(
                _id = "field_$noteId",
                label = "Title of $noteId",
                placeholder = "Placeholder for $noteId"
            ))
        )
        return Result.success(note)
    }

    override fun createNote(
        authorId: String,
        tags: List<String>,
        fields: List<Field>,
        noteType: NoteType
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override fun updateNote(noteId: String, tags: List<String>, fields: List<Field>): Result<Unit> {
        return Result.success(Unit)
    }

    override fun deleteNote(noteId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun findNotes(
        workspaceId: String,
        noteType: NoteType,
        tagsToInclude: List<String>,
        searchQuery: String,
        notesPerPage: Int
    ): Result<List<Note>> {
        val notes = (1..notesPerPage).map {
            val id = "note_${workspaceId}_$it"
            Note(
                _id = id,
                dateCreation = LocalDateTime.now().minusDays(it.toLong()),
                dateLastEdit = LocalDateTime.now(),
                tags = arrayListOf("tag_$it", "workspace_$workspaceId"),
                noteType = noteType,
                fields = listOf(TextField(
                    _id = "field_$id",
                    label = "Field for $id",
                    placeholder = "Generated for query '$searchQuery'"
                ))
            )
        }
        return Result.success(notes)
    }

    override fun getAuthors(noteIds: List<String>): Result<List<User>> {
        val authors = noteIds.mapIndexed { index, id ->
            User(
                _id = "author_$id",
                email = "author${index + 1}@${id}.com",
                name = "Author of $id",
                bio = "Bio for author of note $id",
                profilePicture = "author_pic_$id.png"
            )
        }
        return Result.success(authors)
    }

    override fun getWorkspacesForNote(noteId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun shareNoteToWorkspace(noteId: String, workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }
}