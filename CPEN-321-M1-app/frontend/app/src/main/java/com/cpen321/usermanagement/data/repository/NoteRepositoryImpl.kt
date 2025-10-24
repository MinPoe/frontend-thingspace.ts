package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.api.CreateNoteRequest
import com.cpen321.usermanagement.data.remote.api.NoteInterface
import com.cpen321.usermanagement.data.remote.api.UpdateNoteRequest
import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.data.remote.dto.Field
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import com.cpen321.usermanagement.data.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteInterface: NoteInterface,
) : NoteRepository {

    companion object {
        private const val TAG = "NoteRepository"
    }

    override suspend fun getNote(noteId: String): Result<Note> {
        return try {
            val response = noteInterface.getNote("", noteId)
            if (response.isSuccessful && response.body()?.data?.note != null) {
                Result.success(response.body()!!.data!!.note)
            } else {
                handleError(response.errorBody()?.string(), "Failed to fetch note.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun createNote(
        authorId: String,
        tags: List<String>,
        fields: List<Field>,
        noteType: NoteType
    ): Result<Unit> {
        return try {
            val request = CreateNoteRequest(
                tags = tags,
                fields = fields,
                noteType = noteType,
                workspaceId = authorId // or adjust if workspaceId ≠ authorId
            )
            val response = noteInterface.createNote("", request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleError(response.errorBody()?.string(), "Failed to create note.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun updateNote(
        noteId: String,
        tags: List<String>,
        fields: List<Field>
    ): Result<Unit> {
        return try {
            val request = UpdateNoteRequest(tags = tags.joinToString(","), fields = fields, workspaceId = "")
            val response = noteInterface.updateNote("", noteId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleError(response.errorBody()?.string(), "Failed to update note.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            val response = noteInterface.deleteNote("", noteId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleError(response.errorBody()?.string(), "Failed to delete note.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun findNotes(
        workspaceId: String,
        noteType: NoteType,
        tagsToInclude: List<String>,
        searchQuery: String,
        notesPerPage: Int
    ): Result<List<Note>> {
        return try {
            val response = noteInterface.findNotes("", searchQuery)
            if (response.isSuccessful && response.body()?.data?.notes != null) {
                Result.success(response.body()!!.data!!.notes)
            } else {
                handleError(response.errorBody()?.string(), "Failed to fetch notes.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun getAuthors(noteIds: List<String>): Result<List<User>> {
        return Result.failure(Exception("Not implemented on backend yet"))
    }

    override suspend fun getWorkspacesForNote(noteId: String): Result<Workspace> {
        return try {
            val response = noteInterface.getWorkspacesForNote("", noteId)
            if (response.isSuccessful && response.body()?.data?.workspace != null) {
                Result.success(response.body()!!.data!!.workspace)
            } else {
                handleError(response.errorBody()?.string(), "Failed to get workspaces for note.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun shareNoteToWorkspace(
        noteId: String,
        workspaceId: String
    ): Result<Unit> {
        return try {
            val response = noteInterface.shareNoteToWorkspace("", noteId, workspaceId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleError(response.errorBody()?.string(), "Failed to share note to workspace.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun copyNoteToWorkspace(noteId: String, workspaceId: String): Result<Unit> {
        return try {
            val response = noteInterface.copyNoteToWorkspace("", noteId, workspaceId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleError(response.errorBody()?.string(), "Failed to copy note to workspace.")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    /** ------------------ Error and Exception Helpers ------------------ **/

    private fun handleError(errorBody: String?, fallbackMessage: String): Result<Nothing> {
        val errorMessage = parseErrorMessage(errorBody, fallbackMessage)
        Log.e(TAG, errorMessage)
        return Result.failure(Exception(errorMessage))
    }

    private fun handleException(e: Exception): Result<Nothing> {
        Log.e(TAG, "Network/HTTP error in Notes API", e)
        return Result.failure(e)
    }
}