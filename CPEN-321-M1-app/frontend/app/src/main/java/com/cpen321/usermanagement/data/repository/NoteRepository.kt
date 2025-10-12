package com.cpen321.usermanagement.data.repository
import com.cpen321.usermanagement.data.remote.dto.Field
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.NoteType

interface NoteRepository {
    fun getNote(noteId: String): Result<Note>
    fun createNote(authorId: String, tags: List<String>, fields: List<Field>, noteType: NoteType):
            Result<Unit>
    fun updateNote(noteId: String, tags: List<String>, fields: List<Field>): Result<Unit>
    fun deleteNote(noteId: String): Result<Unit>

    //TODO: will have to handle returning pages at different times for the MVP
    fun findNotes(workspaceId: String, //if userId of the user is given here also has to work
                  noteType:NoteType,
                  tagsToInclude: List<String>,
                  searchQuery: String,
                  notesPerPage: Int): Result<List<Note>>
    fun getAuthors(noteIds: List<String>): Result<List<User>>
    fun getWorkspacesForNote(noteId:String): Result<Unit>
    fun shareNoteToWorkspace(noteId:String, workspaceId: String): Result<Unit>
}