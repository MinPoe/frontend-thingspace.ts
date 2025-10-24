package com.cpen321.usermanagement.data.repository

import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.CopyNoteRequest
import com.cpen321.usermanagement.data.remote.api.NoteInterface
import com.cpen321.usermanagement.data.remote.api.ShareNoteRequest
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import jakarta.inject.Inject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.cpen321.usermanagement.data.remote.dto.Field

class NoteRepositoryImpl @Inject constructor(
    private val noteApi: NoteInterface,
    private val tokenManager: TokenManager
) : NoteRepository {

    companion object {
        private const val TAG = "NoteRepository"
        private const val AUTH_HEADER_PLACEHOLDER = "" // Handled by Interceptor
    }

    override suspend fun getNote(noteId: String): Result<Note> {
        return try {
            val response = noteApi.getNote(AUTH_HEADER_PLACEHOLDER, noteId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.note)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch note."
                )
                Log.e(TAG, "getNote error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            handleException("getNote", e)
        }
    }

    override suspend fun createNote(
        workspaceId: String,
        authorId: String,
        tags: List<String>,
        fields: List<Field>,
        noteType: NoteType
    ): Result<Unit> {
        return try {
            // Transform fields to backend format (simple maps)
            val backendFields = fields.map { field ->
                mapOf(
                    "_id" to field._id,
                    "fieldType" to "textbox",
                    "content" to ""
                )
            }

            val requestMap = mapOf(
                "workspaceId" to workspaceId,
                "fields" to backendFields,
                "tags" to tags,
                "noteType" to noteType.name
            )

            val response = noteApi.createNote(AUTH_HEADER_PLACEHOLDER, requestMap)

            if (response.isSuccessful) {
                Log.d(TAG, "Note created successfully")
                Result.success(Unit)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to create note."
                )
                Log.e(TAG, "createNote error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            handleException("createNote", e)
        }
    }

    override suspend fun updateNote(
        noteId: String,
        tags: List<String>,
        fields: List<Field>
    ): Result<Unit> {
        // TODO: Implement when backend endpoint is ready
        return Result.success(Unit)
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        // TODO: Implement when backend endpoint is ready
        return Result.success(Unit)
    }

    override suspend fun findNotes(
        workspaceId: String,
        noteType: NoteType,
        tagsToInclude: List<String>,
        searchQuery: String,
        notesPerPage: Int
    ): Result<List<Note>> {
        return try {
            val response = noteApi.findNotes(
                authHeader = AUTH_HEADER_PLACEHOLDER,
                workspaceId = workspaceId,
                noteType = noteType.name,
                tags = tagsToInclude,
                query = searchQuery
            )

            if (response.isSuccessful && response.body()?.data != null) {
                Log.d(TAG, "Notes fetched successfully")
                Result.success(response.body()!!.data!!.notes)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch notes."
                )
                Log.e(TAG, "findNotes error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            handleException("findNotes", e)
        }
    }

    override suspend fun getAuthors(noteIds: List<String>): Result<List<User>> {
        // TODO: Implement when backend endpoint is ready
        return Result.success(emptyList())
    }

    override suspend fun getWorkspacesForNote(noteId: String): Result<Unit> {
        // TODO: Implement when backend endpoint is ready
        return Result.success(Unit)
    }

    override suspend fun shareNoteToWorkspace(noteId: String, workspaceId: String): Result<Unit> {
        return try {
            val request = ShareNoteRequest(workspaceId)
            val response = noteApi.shareNoteToWorkspace(AUTH_HEADER_PLACEHOLDER, noteId, request)
            if (response.isSuccessful) {
                Log.d(TAG, "Note shared successfully to workspace $workspaceId")
                Result.success(Unit)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to share note."
                )
                Log.e(TAG, "shareNoteToWorkspace error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            handleException("shareNoteToWorkspace", e)
        }
    }

    override suspend fun copyNoteToWorkspace(noteId: String, workspaceId: String): Result<Unit> {
        return try {
            val request = CopyNoteRequest(workspaceId)
            val response = noteApi.copyNoteToWorkspace(AUTH_HEADER_PLACEHOLDER, noteId, request)
            if (response.isSuccessful) {
                Log.d(TAG, "Note copied successfully to workspace $workspaceId")
                Result.success(Unit)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to copy note."
                )
                Log.e(TAG, "copyNoteToWorkspace error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            handleException("copyNoteToWorkspace", e)
        }
    }

    private fun <T> handleException(method: String, e: Exception): Result<T> {
        val errorMessage = when (e) {
            is UnknownHostException, is SocketTimeoutException ->
                "Network connection error. Please check your internet."
            is IOException ->
                "Network error. Please try again."
            else -> e.message ?: "An unexpected error occurred."
        }
        Log.e(TAG, "$method failed", e)
        return Result.failure(Exception(errorMessage))
    }
}